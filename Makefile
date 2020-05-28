GRAALVM_HOME = $(HOME)/graalvm-ce-java11-20.1.0
JAVA_HOME = $(GRAALVM_HOME)
PATH = $(GRAALVM_HOME)/bin:$(shell echo $$PATH)
SRC = src/decloj/core.clj
VERSION = 0.1.0-SNAPSHOT

all: build/decloj

clean:
	-rm -rf build target
	lein clean

target/uberjar/decloj-$(VERSION)-standalone.jar: $(SRC)
	GRAALVM_HOME=$(GRAALVM_HOME) lein uberjar

analyse:
	$(GRAALVM_HOME)/bin/java -agentlib:native-image-agent=config-output-dir=config-dir \
		-Dorg.bytedeco.javacpp.logger.debug=true \
		-Djava.library.path=./ \
		-jar target/uberjar/decloj-$(VERSION)-standalone.jar

build/decloj: target/uberjar/decloj-$(VERSION)-standalone.jar
	-mkdir build
	export
	$(GRAALVM_HOME)/bin/native-image \
		-jar target/uberjar/decloj-$(VERSION)-standalone.jar \
		-H:Name=build/decloj \
		-H:+ReportExceptionStackTraces \
		-J-Dclojure.spec.skip-macros=true \
		-J-Dclojure.compiler.direct-linking=true \
		-H:ConfigurationFileDirectories=graal-configs/ \
		--initialize-at-build-time \
		-H:Log=registerResource: \
		-H:EnableURLProtocols=http,https \
		--verbose \
		--allow-incomplete-classpath \
		--no-fallback \
		--no-server \
		"-J-Xmx6g"

run: all
	build/decloj

package-linux-amd64: all
	-rm -rf build/linux-package
	-mkdir -p build/linux-package
	cp decloj build/linux-package
	cd build/linux-package && GZIP=-9 tar cvzf ../decloj-$(VERSION)-linux-amd64.tgz decloj
	cp target/uberjar/decloj-$(VERSION)-standalone.jar build/decloj-$(VERSION)-linux-amd64.jar
	du -sh decloj build/decloj-$(VERSION)-linux-amd64.tgz build/decloj-$(VERSION)-linux-amd64.jar

package-darwin-amd64: all
	-rm -rf build/darwin-package
	-mkdir -p build/darwin-package
	cp decloj build/darwin-package
	cd build/darwin-package && zip ../decloj-$(VERSION)-darwin-amd64.zip decloj
	cp target/uberjar/decloj-$(VERSION)-standalone.jar build/decloj-$(VERSION)-darwin-amd64.jar
	du -sh decloj build/decloj-$(VERSION)-darwin-amd64.zip build/decloj-$(VERSION)-darwin-amd64.jar
