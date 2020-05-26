(ns decloj.core
  (:require [clojure.java.io :as io])
  (:import [org.bytedeco.javacpp PointerPointer IntPointer]
           [org.bytedeco.javacpp Loader]
           [org.bytedeco.qt.global Qt5Core]
           [org.bytedeco.qt.Qt5Widgets QApplication QTextEdit]
           )
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Setup")
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
      (println "Exiting" result)
      (System/exit result)))

  )
