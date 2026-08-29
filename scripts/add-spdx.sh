#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 l5z12
#
# SPDX-License-Identifier: GPL-3.0-or-later
#
# Idempotently prepend SPDX headers to every source / build file in the repo.
# Re-runnable: any file that already contains an SPDX-License-Identifier is left
# untouched, so run it again whenever new files are added.
#
#   bash scripts/add-spdx.sh
#
set -euo pipefail

# repo root = parent of this script's directory
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

COPYRIGHT="2026 l5z12"
LICENSE_ID="GPL-3.0-or-later"

added=0
skipped=0

prepend() {  # $1 = file, $2 = comment prefix
  local f="$1" c="$2"
  [ -f "$f" ] || return 0
  if grep -q "SPDX-License-Identifier" "$f" 2>/dev/null; then
    skipped=$((skipped + 1)); return 0
  fi
  local tmp; tmp="$(mktemp)"
  {
    printf '%s SPDX-FileCopyrightText: %s\n' "$c" "$COPYRIGHT"
    printf '%s\n' "$c"
    printf '%s SPDX-License-Identifier: %s\n' "$c" "$LICENSE_ID"
    printf '\n'
    cat "$f"
  } > "$tmp"
  mv "$tmp" "$f"
  added=$((added + 1))
  echo "  + $f"
}

# Java sources (never the generated Stonecutter output under versions/*/build/)
while IFS= read -r -d '' f; do prepend "$f" "//"; done < <(
  find src/main/java common/src/main/java -name '*.java' -print0 2>/dev/null)

# Gradle build scripts — // line comments are valid in both Kotlin and Groovy DSLs
for f in build.gradle.kts settings.gradle.kts stonecutter.gradle.kts build.fabric26.gradle; do
  prepend "$f" "//"
done

# Shell scripts
while IFS= read -r -d '' f; do prepend "$f" "#"; done < <(
  find scripts -name '*.sh' -print0 2>/dev/null)

# GitHub Actions workflows
while IFS= read -r -d '' f; do prepend "$f" "#"; done < <(
  find .github/workflows -type f \( -name '*.yml' -o -name '*.yaml' \) -print0 2>/dev/null)

# Properties: root + per-node pins (excludes the generated gradle/wrapper properties)
prepend gradle.properties "#"
while IFS= read -r -d '' f; do prepend "$f" "#"; done < <(
  find versions -maxdepth 2 -name 'gradle.properties' -print0 2>/dev/null)

echo "SPDX: added=$added skipped=$skipped"
