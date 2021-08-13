#!/usr/bin/env bash

if [ -n "$1" ]; then
  mvn versions:set -DnewVersion=$1
  mvn versions:commit

  echo "$1" > version.txt

else
  echo "Необходимо задать параметр с новой версией для установки"

fi
