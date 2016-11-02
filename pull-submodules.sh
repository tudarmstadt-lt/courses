#!/bin/bash

p=$(pwd); cat .gitmodules  | \
grep path | \
cut -f2 -d"=" | \
tr -d [:blank:] | \
while read d; do \
  cd $d; \
  git remote -v | head -n 1; \
  git pull origin master; \
  cd $p; \
done