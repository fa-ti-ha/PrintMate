#!/usr/bin/env bash
# Configure local git to use repository hooks directory
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$REPO_ROOT"

echo "Configuring Git hooks path to .githooks..."
git config core.hooksPath .githooks
chmod +x .githooks/commit-msg 2>/dev/null || true

echo "✓ Git hooks successfully configured!"
echo "All future commits will be validated against Conventional Commits."

