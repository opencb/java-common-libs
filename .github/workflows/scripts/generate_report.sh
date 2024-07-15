#!/bin/bash

# Create the 'report' folder if it doesn't exist
mkdir -p report

# Create a list of modules
modules=()

# Get the current directory name
current_dir=$(basename "$PWD")

# Add the current directory's report
if [ -f "./target/site/surefire-report.html" ]; then
    modules+=("$current_dir")
    mkdir -p "report/$current_dir"
    cp -r "./target/site"/* "report/$current_dir/"
fi

# Find all other 'target/site' folders and copy their contents to 'report', renaming them with the module name
find . -type f -path './*/target/site/surefire-report.html' | while read -r report_file; do
    module=$(basename $(dirname $(dirname $(dirname "$report_file"))))
    modules+=("$module")
    mkdir -p "report/$module"
    cp -r "$(dirname "$report_file")"/* "report/$module/"
done

cd "$(dirname "$0")" || exit

# Read the template and prepare to replace the placeholder with actual menu entries
template=$(<index.template)
menu_entries=""

# Create menu entries
for module in "${modules[@]}"; do
    menu_entries+="<li><a href=\"#\" onclick=\"loadReport('$module')\">$module</a></li>\n"
done

# Replace the placeholder in the template with actual menu entries
output="${template//<!-- MENU_ENTRIES -->/$menu_entries}"

# Write the output to index.html in the report folder
echo -e "$output" > report/index.html

echo "All files have been copied and the index.html file has been created in the 'report' folder."
