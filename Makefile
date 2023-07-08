# Is it weird to have a Makefile in a project that is built with Maven?
# Probably.

.PHONY: all build check publish

# Remove make's default rules.
.SUFFIXES:

SHELL := bash -O globstar

published := /Users/peter/hacks/jobe/jars/bhs-cs.jar

all: build check

build:
	mvn package

cp.txt:
	mvn dependency:build-classpath -Dmdep.outputFile=cp.txt

check: cp.txt
	java -cp "target/bhs-cs-1.0-SNAPSHOT.jar:$(shell cat cp.txt)" com.gigamonkeys.bhs.TestRunner com.gigamonkeys.bhs.TestClass com.gigamonkeys.bhs.ReferenceClass cases.json

publish: $(published)

$(published): target/bhs-cs-1.0-SNAPSHOT.jar
	cp $< $@
