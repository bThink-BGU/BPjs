#!/bin/bash

echo updating model and test jars.

cd ..
mvn package
mvn jar:test-jar
cp target/*.jar tst/
cd tst

echo Updated model and test jars.
