# Is it weird to have a Makefile in a project that is built with Maven?
# Probably.

.PHONY: all build check publish

# Remove make's default rules.
.SUFFIXES:

SHELL := bash -O globstar

google_java_format := google-java-format-1.22.0-all-deps.jar
format_java :=	java -jar $(google_java_format) --skip-javadoc-formatting --replace

export CLASSPATH = target/bhs-cs-1.0-SNAPSHOT.jar:$(shell cat cp.txt)

published := /Users/peter/hacks/jobe/jars/bhs-cs.jar

test_runner := com.gigamonkeys.bhs.TestRunner

smoke_test :=  com.gigamonkeys.bhs.TestClass com.gigamonkeys.bhs.ReferenceClass cases.json

all: build check

build:
	mvn package

fmt: $(google_java_format)
	$(format_java) src/main/java/**/*.java

$(google_java_format):
	$(error Download $@ from https://github.com/google/google-java-format/releases)

cp.txt:
	mvn dependency:build-classpath -Dmdep.outputFile=cp.txt

check: cp.txt
	java $(test_runner) $(smoke_test)

check_refactored:
	cd tests && $(MAKE) check

publish: $(published)

$(published): target/bhs-cs-1.0-SNAPSHOT.jar
	cp $< $@
