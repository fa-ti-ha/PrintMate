#!/usr/bin/env bash
# PrintMate CI Commit Message Validator
# Used in GitHub Actions to validate all commits in a Pull Request
set -e

BASE_REF="${1:-origin/main}"
HEAD_REF="${2:-HEAD}"
PATTERN="^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)\([a-z0-9\-]+\): .+$"

echo "Validating commits between $BASE_REF and $HEAD_REF..."

FAILED=0

# Loop through commit SHAs
for SHA in $(git rev-list "$BASE_REF..$HEAD_REF" 2>/dev/null || git rev-list -n 1 "$HEAD_REF"); do
  MSG=$(git log --format=%s -n 1 "$SHA")
  
  # Allow standard merge commits
  if echo "$MSG" | grep -qE '^Merge '; then
    continue
  fi

  if ! echo "$MSG" | grep -qE "$PATTERN"; then
    echo "❌ Commit $SHA has invalid format:"
    echo "   \"$MSG\""
    FAILED=1
  else
    echo "✓ Commit $SHA valid: \"$MSG\""
  fi
done

if [ $FAILED -ne 0 ]; then
  echo ""
  echo "===================================================================="
  echo "Commit message validation failed! Please reword your commit(s):"
  echo "Format: type(scope): description"
  echo "===================================================================="
  exit 1
fi

echo "All commit messages comply with Conventional Commits!"
exit 0

