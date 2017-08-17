#!/bin/bash

# Runs all tests from 4 to $MAX.

if (($# < 2)); then
  echo Usage: $0 MIN MAX
  echo where MIN and MAX are the start and end number of philosophers \(inclusive\).
  exit 1
fi

MIN=$1
MAX=$2

DATE_STR=`date "+%d%m%y-%H%M"`
RESULT_FOLDER=results/$DATE_STR
mkdir -p $RESULT_FOLDER


for i in `seq $MIN $MAX`
do
  echo Running with $i philosophers
  date "+%H:%M:%S"
  ./run-test.sh $i > $RESULT_FOLDER/$i-out.log 2>&1
  date "+%H:%M:%S"
  echo DONE
done
