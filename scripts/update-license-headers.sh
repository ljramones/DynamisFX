#!/bin/bash
#
# Updates license headers from BSD-3 (FXyz) to Apache-2.0 with BSD-3 attribution
#

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# The new Apache 2.0 header with FXyz attribution
read -r -d '' NEW_HEADER << 'HEADER_EOF'
/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */
HEADER_EOF

# Count files
total=$(find "$PROJECT_ROOT" -name "*.java" -path "*/src/main/java/*" ! -path "*/target/*" | wc -l | tr -d ' ')
count=0

echo "Updating license headers in $total Java files..."

find "$PROJECT_ROOT" -name "*.java" -path "*/src/main/java/*" ! -path "*/target/*" | while read -r file; do
    count=$((count + 1))

    # Check if file has old FXyz header
    if head -10 "$file" | grep -q "F(X)yz\|Copyright (c) 2013"; then
        # Create temp file
        tmpfile=$(mktemp)

        # Extract everything after the old header (find closing */ and take rest)
        # The old headers end with " */" followed by blank line and "package"
        awk '
        BEGIN { in_header = 1; found_end = 0 }
        /^ \*\/$/ && in_header { found_end = 1; next }
        found_end && /^$/ { found_end = 0; in_header = 0; next }
        !in_header { print }
        ' "$file" > "$tmpfile"

        # Write new header + rest of file
        echo "$NEW_HEADER" > "$file"
        echo "" >> "$file"
        cat "$tmpfile" >> "$file"

        rm "$tmpfile"
        echo "  Updated: $file"
    else
        echo "  Skipped (no FXyz header): $file"
    fi
done

echo "Done!"
