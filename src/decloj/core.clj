(ns decloj.core
  (:require [clojure.java.io :as io])
  (:import [org.bytedeco.javacpp PointerPointer IntPointer]
           [org.bytedeco.javacpp Loader]
           [org.bytedeco.qt.global Qt5Core]
           [org.bytedeco.qt.Qt5Widgets QApplication QTextEdit]
           )
  (:gen-class))

(set! *warn-on-reflection* true)

(defn init! []
  (let [native-image?
        (and (= "Substrate VM" (System/getProperty "java.vm.name"))
             (= "runtime" (System/getProperty "org.graalvm.nativeimage.imagecode")))]
    (when native-image?
      (println "setting up native image")
      (System/setProperty "java.library.path" "./"))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (init!)

  (println ">>> loadLibrary")
  (println ">>>>" (-> (.getClassLoader org.bytedeco.javacpp.Loader)
                      (.loadClass "org.bytedeco.javacpp.Loader")))
  (println ">>>>" (-> (.getClassLoader org.bytedeco.javacpp.Pointer)
                      (.loadClass "org.bytedeco.javacpp.Pointer")))

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
