#!/bin/bash

target_file=secrets.properties
target_directory=./app/.config

target_path=$target_directory/$target_file

if [ -f $target_path ]; then
  echo "'$target_path' already exists, skipping file creation"; exit 1;
else
  mkdir -vp $target_directory

  touch $target_path
  {
    echo "CLIENT_ID=\"CLIENT_ID\""
    echo "CLIENT_SECRET=\"CLIENT_SECRET\""
    echo "GIPHY_KEY=\"GIPHY_KEY\""
  } >> $target_path

  echo "Make sure to update the contents of '$target_path' according to the setup docs"
fi