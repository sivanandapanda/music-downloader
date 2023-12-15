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

# Read the file line by line and echo each line
while IFS= read -r line || [ -n "$line" ]; do
    yt-dlp $line	
    echo "downloaded $line"
done < "$filename"

