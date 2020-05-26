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
