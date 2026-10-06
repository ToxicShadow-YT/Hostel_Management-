#!/usr/bin/env bash
# =============================================================================
# Hostel Management System - Execution Script (macOS / Linux)
# =============================================================================

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

echo "=========================================================="
echo "    Compiling Hostel Management System...                "
echo "=========================================================="

mkdir -p bin

# Compile all source files
javac -cp "lib/*:src" -d bin \
    src/database/*.java \
    src/model/*.java \
    src/dao/*.java \
    src/ui/*.java \
    src/Main.java

echo "Compilation successful!"
echo "----------------------------------------------------------"
echo "Starting Application..."
echo "=========================================================="

# Run Main class
java -cp "bin:lib/*" Main
