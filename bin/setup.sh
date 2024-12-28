#!/bin/bash

set -eo pipefail

if [[ -z "$1" ]]; then
    echo "usage: setup.sh <day>"
    exit 1
fi

DAY="${1}"
REPO_BASE="$(git rev-parse --show-toplevel | tr -d '\n')"
SRC_PATH="${REPO_BASE}/app/src/main/kotlin/org/aoc"
echo $SRC_PATH

# 1. Download the input data
aoc.py download "${DAY}"

# 2. Create a new folder for the day
PREVIOUS_DAY="$(($DAY - 1))"
PREVIOUS_DAY_FOLDER_NAME=$(printf "day%02d" "${PREVIOUS_DAY}")
PREVIOUS_DAY_PATH="${SRC_PATH}/${PREVIOUS_DAY_FOLDER_NAME}/"

TARGET_DAY_FOLDER_NAME=$(printf "day%02d" "${DAY}")
TARGET_DAY_PATH="${SRC_PATH}/${TARGET_DAY_FOLDER_NAME}/"

if [[ -d "${TARGET_DAY_PATH}" ]]; then
    echo "error: ${TARGET_DAY_PATH} already exists"
    exit 1
fi

echo "Creating new code file: ${TARGET_DAY_PATH}"
cp -r "${PREVIOUS_DAY_PATH}" "${TARGET_DAY_PATH}"

# 3. Update the main file to use the new day's code
SOLUTION_PATH="${TARGET_DAY_PATH}Solve.kt"
sed -i '' -e "s/${PREVIOUS_DAY_FOLDER_NAME}/${TARGET_DAY_FOLDER_NAME}/g" "${SOLUTION_PATH}"
 
# 4. Update the main file to use the new day's code
# FIXME(alvaro): This does not handle properly the left side of the "when" in the cases
# where the left side is a single digit number, since the search and replace looks for the 
# 0 padded number always (distinguishing between 1 digit and more makes this complex)
MAIN_PATH="${SRC_PATH}/App.kt"
TARGET_DAY_PADDED=$(printf "%02d" "${DAY}")
PREVIOUS_DAY_PADDED=$(printf "%02d" "${PREVIOUS_DAY}")
echo "Running command:" "/${PREVIOUS_DAY_PADDED}/ {p; s/${PREVIOUS_DAY_PADDED}/${TARGET_DAY_PADDED}/g;}" 
sed -i '' -e "/${PREVIOUS_DAY_PADDED}/ {p; s/${PREVIOUS_DAY_PADDED}/${TARGET_DAY_PADDED}/g;}" "${MAIN_PATH}"
