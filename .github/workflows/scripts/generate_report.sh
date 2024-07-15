#!/bin/bash

# Create the 'report' folder if it doesn't exist
mkdir -p report

# Create a list of modules
modules=()

# Find all 'target/site' folders and copy their contents to 'report', renaming them with the module name
find . -type f -path '*/target/site/surefire-report.html' | while read -r report_file; do
    module=$(basename $(dirname $(dirname $(dirname "$report_file"))))
    modules+=("$module")
    mkdir -p "report/$module"
    cp -r "$(dirname "$report_file")"/* "report/$module/"
done

# Create the index.html file in 'report'
cat <<EOL > report/index.html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Surefire Reports</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }
        #menu {
            float: left;
            width: 20%;
            background: #f4f4f4;
            padding: 15px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        #content {
            margin-left: 22%;
            padding: 15px;
        }
        a {
            text-decoration: none;
            display: block;
            padding: 8px 16px;
            color: #333;
        }
        a:hover {
            background: #ddd;
        }
    </style>
    <script>
        function loadReport(module) {
            fetch(module + '/surefire-report.html')
                .then(response => response.text())
                .then(html => {
                    document.getElementById('content').innerHTML = html;
                });
        }
    </script>
</head>
<body>
    <div id="menu">
        <h2>Surefire Reports</h2>
        <ul>
EOL

# Add menu entries to the index.html file
for module in "${modules[@]}"; do
    echo "            <li><a href=\"#\" onclick=\"loadReport('$module')\">$module</a></li>" >> report/index.html
done

# Finalize the index.html file
cat <<EOL >> report/index.html
        </ul>
    </div>
    <div id="content">
        <h2>Select a module from the menu to view the report.</h2>
    </div>
</body>
</html>
EOL

echo "All files have been copied and the index.html file has been created in the 'report' folder."