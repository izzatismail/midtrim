#!/bin/bash
# setup-hooks.sh — Install pre-commit hooks for MidTrim
# Run this from the repo root after cloning.

set -euo pipefail

HOOKS_DIR=".git/hooks"
HOOK_FILE="${HOOKS_DIR}/pre-commit"

cat > "$HOOK_FILE" << 'HOOK'
#!/bin/bash
# Pre-commit hook: lint → format → test (staged changes only)
# Installed by scripts/setup-hooks.sh — do not edit directly.

set -euo pipefail

echo "=== Pre-commit checks ==="

STAGED_ANDROID=$(git diff --cached --name-only -- android/ | head -c 1)
STAGED_IOS=$(git diff --cached --name-only -- ios/ | head -c 1)

if [ -n "$STAGED_ANDROID" ]; then
  echo "[Android] Running ktlintCheck..."
  (cd android && ./gradlew ktlintCheck --daemon) || exit 1
  echo "[Android] Running detekt..."
  (cd android && ./gradlew detekt --daemon) || exit 1
  echo "[Android] Running unit tests..."
  (cd android && ./gradlew test --daemon) || exit 1
fi

if [ -n "$STAGED_IOS" ]; then
  echo "[iOS] Running swiftlint..."
  (cd ios && swiftlint --strict) || exit 1
  echo "[iOS] Running unit tests..."
  xcodebuild test \
    -project ios/MidTrim/MidTrim.xcodeproj \
    -scheme MidTrim \
    -destination 'platform=iOS Simulator,name=iPhone 15' \
    -only-testing:MidTrimTests || exit 1
fi

echo "=== All checks passed ==="
HOOK

chmod +x "$HOOK_FILE"
echo "Pre-commit hook installed at $HOOK_FILE"