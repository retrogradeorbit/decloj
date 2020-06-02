(defproject decloj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [org.clojure/java.classpath "1.0.0"]
                 #_[org.bytedeco/qt-platform "5.14.2-1.5.4-SNAPSHOT"]
                 [org.bytedeco/qt-platform "5.15.0-1.5.4-SNAPSHOT"]
                 [cheshire "5.10.0"]]
  :repositories [["sonatype" {:url "https://oss.sonatype.org/content/repositories/snapshots"}]]
  :main ^:skip-aot decloj.core
  :java-source-paths ["src/java"]
  :jvm-opts ["-Djava.library.path=/home/crispin/.decloj/libs/"
             "-Dorg.bytedeco.javacpp.logger.debug=true"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  ;;"-Dorg.bytedeco.javacpp.logger.debug=true"
                                  "-Djava.library.path=./"]
                       }})
