#!/bin/sh

# Moves the terminal execution context to the folder where this script lives
cd "$(dirname "$0")"

# Launches your GUI application invisibly without leaving an orphaned terminal window open
java -jar tb2_swing.jar
