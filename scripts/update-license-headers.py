#!/usr/bin/env python3
"""
Updates license headers from BSD-3 (FXyz) to Apache-2.0 with BSD-3 attribution.
"""

import os
import re
import sys

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# New Apache 2.0 header with FXyz attribution
NEW_HEADER_FXYZ = '''/*
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

'''

# New Apache 2.0 header for new files (no FXyz attribution)
NEW_HEADER_CLEAN = '''/*
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
 */

'''

# Pattern to match old FXyz BSD-3 header
OLD_HEADER_PATTERN = re.compile(
    r'^/\*\*?\s*\n'  # Start of comment
    r'(?:\s*\*.*\n)*?'  # Comment lines
    r'.*(?:F\(X\)yz|Copyright \(c\) 2013).*\n'  # FXyz copyright line
    r'(?:\s*\*.*\n)*?'  # More comment lines
    r'\s*\*/\s*\n'  # End of comment
    r'\s*\n?',  # Optional blank line
    re.MULTILINE
)

def find_java_files(root):
    """Find all Java source files."""
    java_files = []
    for dirpath, dirnames, filenames in os.walk(root):
        # Skip target directories
        dirnames[:] = [d for d in dirnames if d != 'target']

        if 'src/main/java' in dirpath or 'src/test/java' in dirpath:
            for filename in filenames:
                if filename.endswith('.java'):
                    java_files.append(os.path.join(dirpath, filename))
    return java_files

def has_fxyz_header(content):
    """Check if file has FXyz header."""
    first_500 = content[:500]
    return 'F(X)yz' in first_500 or ('Copyright (c) 2013' in first_500 and 'All rights reserved' in first_500)

def has_dynamisfx_header(content):
    """Check if file already has DynamisFX Apache header."""
    return 'DynamisFX Contributors' in content[:500] and 'Apache License' in content[:500]

def remove_old_header(content):
    """Remove the old BSD-3 header."""
    # Find the end of the first block comment
    match = re.match(r'^/\*\*?\s*\n(?:.*\n)*?\s*\*/\s*\n\s*\n?', content)
    if match:
        return content[match.end():]
    return content

def update_file(filepath, dry_run=False):
    """Update a single file's header."""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Skip if already updated
    if has_dynamisfx_header(content):
        return 'skipped (already updated)'

    # Determine which header to use
    if has_fxyz_header(content):
        new_header = NEW_HEADER_FXYZ
        header_type = 'FXyz attribution'
    else:
        new_header = NEW_HEADER_CLEAN
        header_type = 'clean Apache'

    # Remove old header
    new_content = remove_old_header(content)

    # Add new header
    new_content = new_header + new_content

    if not dry_run:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)

    return f'updated ({header_type})'

def main():
    dry_run = '--dry-run' in sys.argv

    if dry_run:
        print("DRY RUN - no files will be modified\n")

    java_files = find_java_files(PROJECT_ROOT)
    print(f"Found {len(java_files)} Java files\n")

    stats = {'updated': 0, 'skipped': 0}

    for filepath in sorted(java_files):
        rel_path = os.path.relpath(filepath, PROJECT_ROOT)
        result = update_file(filepath, dry_run)

        if 'updated' in result:
            stats['updated'] += 1
            print(f"  {result}: {rel_path}")
        else:
            stats['skipped'] += 1

    print(f"\nDone! Updated: {stats['updated']}, Skipped: {stats['skipped']}")

if __name__ == '__main__':
    main()
