#!/bin/bash

# Create the 'report' folder if it doesn't exist
mkdir -p report

# Get the current directory name
current_dir=$(basename "$PWD")

# Add the current directory's site folder
if [ -d "./target/site" ]; then
    mkdir -p "report/$current_dir"
    cp -r "./target/site"/* "report/$current_dir/"
fi

# Find all other 'target/site' folders and copy their contents to 'report', renaming them with the module name
find . -type d -path './*/target/site' | while read -r site_dir; do
    module=$(basename "$(dirname "$(dirname "$site_dir")")")
    mkdir -p "report/$module"
    cp -r "$site_dir"/* "report/$module/"
done

echo "All site folders have been copied to the 'report' folder with module names."
