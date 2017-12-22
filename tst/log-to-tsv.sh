#!/bin/bash

grep "^~ " $1 \
    | cut -d\  -f2,6 \
    | tr -d \) \
    | tr -d , \
    | sed -e "s/\([0-9]*\) \([0-9]*\)/\2,\1/"
