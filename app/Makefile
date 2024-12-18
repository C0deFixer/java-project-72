# Makefile
.DEFAULT_GOAL := build-run

setup:
	gradle wrapper --gradle-version 8.4

clean:
	./gradlew clean

.PHONY: build
build:
	./gradlew clean build

install:
	./gradlew clean install

run-dist:
	./build/install/app/bin/app

run:
	./gradlew run

.PHONY: test
test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain

check-deps:
	./gradlew dependencyUpdates -Drevision=release


build-run: build run