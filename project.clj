(defproject decloj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [org.bytedeco/qt-platform "5.14.2-1.5.3"]]
  :main ^:skip-aot decloj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
