(ns decloj.core
  (:require [clojure.java.io :as io]
            [clojure.java.classpath]
            [clojure.string :as string]
            [cheshire.core :as cheshire]
            )
  (:import [org.bytedeco.javacpp PointerPointer IntPointer Loader]
           ;;[org.bytedeco.qt.global Qt5Core]
           [org.bytedeco.qt.Qt5Widgets QApplication QTextEdit]
           [java.nio.file Paths Files LinkOption]
           [java.nio.file.attribute FileAttribute]
           )
  (:gen-class))

(set! *warn-on-reflection* true)

(def config-dir ".decloj")

(defn get-property [property]
  (-> (Loader/loadProperties)
      (.getProperty property)))

(def property-platform (get-property "platform"))
(def property-library-prefix (get-property "platform.library.prefix"))
(def property-library-suffix (get-property "platform.library.suffix"))

(defn get-jar-name [basename]
  (str basename "-" property-platform ".jar"))

#_ (get-jar-name "javacpp-1.5.3")

(def resource-libs
  {"javacpp-1.5.3-linux-x86_64.jar"
   {:path "org/bytedeco/javacpp/"
    :names ["jnijavacpp"]}

   ;; https://github.com/bytedeco/javacpp-presets/blob/master/qt/src/main/java/org/bytedeco/qt/presets/Qt5Gui.java#L47-L52
   ;; TODO: how to read straight from properties
   "qt-5.14.2-1.5.3-linux-x86_64.jar"
   {:path "org/bytedeco/qt/"
    :names [
            "Qt5DBus@.5", "Qt5Gui@.5", "Qt5XcbQpa@.5", "Qt5Widgets@.5", "Qt5PrintSupport@.5",
            "Qt5Core@.5" "jniQt5Core" "jniQt5Widgets"

            ;; macos
            ;;"qmacstyle" "qcocoa" "cocoaprintersupport"

            ;; linux
            ;;"qgtk3"

            ;; all platforms?
            "qxdgdesktopportal"
            "qxcb"
            "qlinuxfb"
            "qminimalegl"
            "qminimal"
            "qoffscreen"
            "composeplatforminputcontextplugin"
            "ibusplatforminputcontextplugin"
            "qxcb-egl-integration"
            "qxcb-glx-integration"
            "qgif"
            "qico"
            "qjpeg"
            "qevdevkeyboardplugin"
            "qevdevmouseplugin"
            "qevdevtabletplugin"
            "qevdevtouchplugin"
            "jniQt5Gui"]}})

(defn make-lib-resource-path [path name]
  (let [[lib suffix] (string/split name #"@")]
    (case property-platform
      "macosx-x86_64"
      (str path property-platform "/"
           property-library-prefix
           lib
           suffix
           property-library-suffix)

      "linux-x86_64"
      (str path property-platform "/"
           property-library-prefix
           lib
           property-library-suffix
           suffix)

      (str path property-platform "/"
           property-library-prefix
           lib
           property-library-suffix
           suffix)
      )))

(defn make-lib-link-name [name]
  (let [[lib _] (string/split name #"@")]
    (str property-library-prefix
         lib
         property-library-suffix)))

(defn make-lib-file-name [name]
  (let [[lib suffix] (string/split name #"@")]
    (str property-library-prefix
         lib
         property-library-suffix
         suffix)))

(defn make-resources-config-hashmap []
  {"resources"
   (->> resource-libs
        (map second)
        (map (fn [{:keys [path names]}]
               (for [n names]
                 (make-lib-resource-path path n))))
        (flatten)
        (map (fn [fname]
               {"pattern" (str (string/replace fname #"\." "\\\\.") "$")}))
        (into []))
   "bundles" []})

(defn write-resources-config-hashmap [filename]
  (spit filename
        (cheshire/generate-string
         (make-resources-config-hashmap)
         {:pretty true})))

(defonce _write-resources
  (write-resources-config-hashmap "graal-configs/resource-config.json"))

(def library-load-list
  (->> resource-libs
       vals
       (map :names)
       flatten
       (map #(first (string/split % #"@")))))

(defn path-split
  "give a full path filename, return a tuple of
  [path basename]

  eg \"blog/posts/1/main.yml\" -> [\"blog/posts/1\" \"main.yml\"]
  "
  [filename]
  (let [file (io/file filename)]
    [(.getParent file) (.getName file)]))

(defn path-join
  "given multiple file path parts, join them all together with the
  file separator"
  [& parts]
  (.getPath ^java.io.File (apply io/file parts)))

(def empty-string-array
  (make-array String 0))

(def empty-file-attribute-array
  (make-array FileAttribute 0))

(def empty-link-options
  (make-array LinkOption 0))

(def no-follow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn symlink [link target]
  (let [link-path (Paths/get link empty-string-array)]
    (when (Files/exists link-path no-follow-links)
      ;; link path exists
      (if (Files/isSymbolicLink link-path)
        ;; and its a symlink
        (Files/delete link-path)

        ;; its something else
        (throw (ex-info (str link " already exists and is not a symlink")
                        {:path link}))))

    (.toString
     (Files/createSymbolicLink
      link-path
      (Paths/get target empty-string-array)
      empty-file-attribute-array))))

(defn setup
  "Copy any of the bundled dynamic libs from resources to the
  run time lib directory"
  [libs-dir]
  (doseq [{:keys [filename
                  linkname
                  resource-file]} (->> resource-libs
                                  (map second)
                                  (map (fn [{:keys [path names]}]
                                         (for [n names]
                                           (let [resource-file (make-lib-resource-path path n)
                                                 link-name (make-lib-link-name n)
                                                 filename (make-lib-file-name n)]
                                             (if (not= link-name filename)
                                               {:resource-file resource-file
                                                :filename filename
                                                :linkname link-name}
                                               {:resource-file resource-file
                                                :filename filename})))))
                                  (flatten))]
    (when-let [file (io/resource resource-file)]
      ;; write out the filename if needed
      (let [[_ name] (path-split (.getFile file))
            dest-path (path-join libs-dir name)
            resource-size (with-open [out (java.io.ByteArrayOutputStream.)]
                            (io/copy (io/input-stream file) out)
                            (count (.toByteArray out)))]
        ;; writing to a library while running its code can result in segfault
        ;; only write if filesize is different or it doesnt exist
        (when (or (not (.exists (io/file dest-path)))
                  (not= (.length (io/file dest-path)) resource-size))
          (io/copy (io/input-stream file) (io/file dest-path))))

      ;; if a symlink is needed, make it
      (when linkname
        (symlink (path-join libs-dir linkname) filename)
        )
      )))


(defn init! []
  (let [native-image?
        (and (= "Substrate VM" (System/getProperty "java.vm.name"))
             (= "runtime" (System/getProperty "org.graalvm.nativeimage.imagecode")))
        home-dir (System/getenv "HOME")
        config-dir (path-join home-dir config-dir)
        libs-dir (path-join config-dir "libs")]
    (.mkdirs (io/as-file libs-dir))

    (when native-image?
      (setup libs-dir)
      (System/setProperty "java.library.path" libs-dir))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (init!)
  (when (= args '("--load"))
    (println "resources")
    (println "=========")
    (doseq [res-file (->> resource-libs
                          (map second)
                          (map (fn [{:keys [path names]}]
                                 (for [n names]
                                   (let [[lib suffix] (string/split n #"@")]
                                     (str path property-platform "/"
                                          property-library-prefix
                                          lib
                                          property-library-suffix
                                          suffix)))))
                          (flatten))]
      (prn res-file (io/resource res-file)))

    (println)

    (doseq [name library-load-list]
      (println "loading:" name)
      (clojure.lang.RT/loadLibrary name))

    (println)
    (println "done")
    (System/exit 0))

  (doseq [name library-load-list]
    (clojure.lang.RT/loadLibrary name))

  (let [home-dir (System/getenv "HOME")
        config-dir (path-join home-dir config-dir)
        libs-dir (path-join config-dir "libs")
        app (QApplication.
             (IntPointer. (int-array [3]))
             (PointerPointer.
              (into-array
               String
               ["gettingstarted"
                "-platformpluginpath"
                libs-dir])
              ))
        text-edit (QTextEdit.)
        ]
    (.show text-edit)
    (let [result (org.bytedeco.qt.Qt5Widgets.QApplication/exec)]
      (System/exit result))))
