#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
HEADER="${MODULE_DIR}/native/jolt_c_api.h"
OUT_DIR="${MODULE_DIR}/src/panama/generated"
PACKAGE="org.dynamisfx.physics.jolt.panama.generated"

if ! command -v jextract >/dev/null 2>&1; then
  echo "jextract not found on PATH. Install a JDK distribution that includes jextract."
  exit 1
fi

rm -rf "${OUT_DIR}"
mkdir -p "${OUT_DIR}"

jextract \
  --source \
  --target-package "${PACKAGE}" \
  --output "${OUT_DIR}" \
  "${HEADER}"

echo "Generated Panama sources in ${OUT_DIR}"
