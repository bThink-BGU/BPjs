#!/bin/bash

echo Single test case with $1 philosophers.

java -Xmx8g -cp BPjs-0.8.6-SNAPSHOT.jar:BPjs-0.8.6-SNAPSHOT-tests.jar:rhino-1.7.7.1.jar\
     il.ac.bgu.cs.bp.bpjs.diningphil.DiningPhilMain $1 $1
