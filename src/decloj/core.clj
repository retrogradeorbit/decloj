(ns decloj.core
  (:require [clojure.java.io :as io]
            [clojure.java.classpath]
            [clojure.string :as string]
            [cheshire.core :as cheshire]
            )
  (:import [org.bytedeco.javacpp PointerPointer IntPointer]
           [org.bytedeco.javacpp Loader]
           [org.bytedeco.qt.global Qt5Core]
           [org.bytedeco.qt.Qt5Widgets QApplication QTextEdit]
           )
  (:gen-class))

(set! *warn-on-reflection* true)

(defn get-cache-dir []
  (->> [(System/getProperty "org.bytedeco.javacpp.cachedir")
        (System/getProperty "org.bytedeco.javacpp.cacheDir")
        (str (System/getProperty "user.home") "/.javacpp/cache/")
        (str (System/getProperty "org.bytedeco.javacpp.cachedir")
             "/.javacpp-"
             (System/getProperty "user.name")
             "/cache/")]
       (filter identity)
       (map io/file)
       (filter (fn [^java.io.File f]
                 (try (and (or (.exists f) (.mkdirs f))
                           (.canRead f)
                           (.canWrite f)
                           (.canExecute f))
                      (catch SecurityException _))))
       first)

  )

#_ (get-cache-dir)

(defn get-property [property]
  (-> (Loader/loadProperties)
      (.getProperty property)))

(def property-platform (get-property "platform"))
(def property-library-prefix (get-property "platform.library.prefix"))
(def property-library-suffix (get-property "platform.library.suffix"))

(defn get-jar-name [basename]
  (str basename "-" property-platform ".jar"))

#_ (get-jar-name "javacpp-1.5.3")

#_

(io/resource "javacpp-platform-1.5.3.jar")

#_
(clojure.java.classpath/filenames-in-jar
 (first (clojure.java.classpath/classpath-jarfiles)))

#_ (map clojure.java.classpath/jar-file? (clojure.java.classpath/classpath))

(defn init! []
  (let [native-image?
        (and (= "Substrate VM" (System/getProperty "java.vm.name"))
             (= "runtime" (System/getProperty "org.graalvm.nativeimage.imagecode")))]
    (when native-image?
      (println "setting up native image")
      (System/setProperty "java.library.path" "./"))))


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

(defn make-resources-config-hashmap []
  {"resources"
   (->> resource-libs
        (map second)
        (map (fn [{:keys [path names]}]
               (for [n names]
                 (let [[lib suffix] (string/split n #"@")]
                   (str path property-platform "/"
                        property-library-prefix
                        lib
                        property-library-suffix
                        suffix)))))
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

#_ (write-resources-config-hashmap "graal-configs/resource-config.json")


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (init!)

  (println "classpath")
  (println "=========")
  (doseq [f (clojure.java.classpath/classpath)]
    (println (.getName ^java.io.File f)))
  (println)

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

  (doall
   (for [name ["jnijavacpp" "Qt5Core" "jniQt5Core" "jniQt5Widgets" "Qt5Gui"
               "Qt5DBus" "Qt5XcbQpa" "Qt5Widgets" "Qt5PrintSupport"

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
               "jniQt5Gui"


               ]]
     (do
       (println "loading:" name)
       (clojure.lang.RT/loadLibrary name))))

  (println ">>> Setup")
  (let [lib-path (Loader/load Qt5Core)
        app (QApplication.
             (IntPointer. (int-array [3]))
             (PointerPointer.
              (into-array
               String
               ["gettingstarted"
                "-platformpluginpath"
                (-> lib-path io/file .getParent)])
              ))
        text-edit (QTextEdit.)
        ]
    (.show text-edit)
    (let [result (org.bytedeco.qt.Qt5Widgets.QApplication/exec)]
      (println ">>> Exiting" result)
      (System/exit result)))

  )

#_ (Loader/loadProperties)
#_ {"platform.compiler.output"
    "-Wl,-rpath,$ORIGIN/ -Wl,-z,noexecstack -Wl,-Bsymbolic -Wall -fPIC -pthread -shared -o "
    "platform.executable.prefix" ""
    "platform.compiler.noexceptions" "-fno-exceptions -fno-rtti"
    "platform" "linux-x86_64"
    "platform.includepath" ""
    "platform.compiler.cpp11" "-std=c++11"
    "platform.link" ""
    "platform.library.prefix" "lib"
    "platform.compiler.cpp98" "-std=c++98"
    "platform.link.suffix" ""
    "platform.compiler.cpp14" "-std=c++14"
    "platform.compiler.cpp17" "-std=c++17"
    "platform.framework" ""
    "platform.compiler.debug" "-O0 -g"
    "platform.framework.suffix" ""
    "platform.compiler" "g++"
    "platform.linkpath.prefix" "-L"
    "platform.compiler.nodeprecated" "-Wno-deprecated-declarations"
    "platform.compiler.nowarnings" "-w"
    "platform.linkpath" ""
    "platform.framework.prefix" "-F"
    "platform.includepath.prefix" "-I"
    "platform.library.suffix" ".so"
    "platform.source.suffix" ".cpp"
    "platform.compiler.fastfpu" "-msse3 -ffast-math"
    "platform.compiler.cpp03" "-std=c++03"
    "platform.link.prefix" "-l"
    "platform.executable.suffix" ""
    "platform.compiler.default" "-march=x86-64 -m64 -O3 -s"
    "platform.path.separator" ":"
    "platform.linkpath.prefix2" "-Wl,-rpath,"}
