#!/bin/bash

# Check if a filename is provided as an argument
if [ $# -ne 1 ]; then
    echo "Usage: $0 <filename>"
    exit 1
fi

filename=$1

# Check if the file exists
if [ ! -f "$filename" ]; then
    echo "File '$filename' not found."
    exit 1
fi

# Directories for doing and done tasks
doing_dir="doing"
done_dir="done"

# Create 'doing' and 'done' directories if they don't exist
mkdir -p "$doing_dir"
mkdir -p "$done_dir"

# Change directory to 'doing' folder
cd "$doing_dir" || exit

# Read the file line by line and process each line
while IFS= read -r line || [ -n "$line" ]; do
    yt-dlp $line
    mv *.mp4 "../$done_dir/"

    echo "Processed: $line"
done < "../$filename"

# Change directory back to the original directory
cd ..

