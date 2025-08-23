#!/bin/bash

# Setup script for AniTrend pre-commit hooks
# Run this script to install pre-commit validation hooks

echo "🔧 Setting up AniTrend pre-commit hooks..."

HOOKS_DIR=".git/hooks"
PRECOMMIT_HOOK="$HOOKS_DIR/pre-commit"

if [ ! -d "$HOOKS_DIR" ]; then
    echo "❌ Error: .git/hooks directory not found. Make sure you're in the project root."
    exit 1
fi

# Create the pre-commit hook
cat > "$PRECOMMIT_HOOK" << 'EOF'
#!/bin/bash

# Pre-commit hook for changelog validation
# Validates that all changelog files are within the 500 character limit for Google Play Store

echo "🔍 Running pre-commit changelog validation..."

# Run the validation script but capture its output
if bash .github/scripts/validate-changelogs.sh 2>/dev/null; then
    echo "✅ Changelog validation passed - commit allowed"
    exit 0
else
    echo "❌ Changelog validation failed - commit blocked"
    echo ""
    echo "💡 To fix this issue:"
    echo "   1. Check the changelog files in fastlane/metadata/android/en-GB/changelogs/"
    echo "   2. Reduce content in any file exceeding 500 characters"
    echo "   3. Run 'bash .github/scripts/validate-changelogs.sh' to verify fixes"
    echo "   4. Try committing again"
    echo ""
    exit 1
fi
EOF

# Make the hook executable
chmod +x "$PRECOMMIT_HOOK"

echo "✅ Pre-commit hook installed successfully!"
echo ""
echo "📋 What this hook does:"
echo "   • Validates changelog files before each commit"
echo "   • Ensures all changelog files are ≤ 500 characters"
echo "   • Blocks commits that would violate Google Play Store limits"
echo ""
echo "🚀 You're all set! The hook will run automatically on git commit."