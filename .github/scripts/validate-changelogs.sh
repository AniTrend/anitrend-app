#!/bin/bash

# Changelog Validation Script
# Validates that all changelog files are within the 500 character limit for Google Play Store

set -e

echo "🔍 Validating changelog files for 500 character limit..."

CHANGELOG_DIR="fastlane/metadata/android/en-GB/changelogs"
FAILED=false

# Initialize workflow summary (only if running in GitHub Actions)
if [ -n "$GITHUB_STEP_SUMMARY" ]; then
  echo "# 📝 Changelog Validation Report" >> $GITHUB_STEP_SUMMARY
  echo "" >> $GITHUB_STEP_SUMMARY
  echo "**Directory:** \`$CHANGELOG_DIR\`" >> $GITHUB_STEP_SUMMARY
  echo "**Character Limit:** 500 characters (Google Play Store requirement)" >> $GITHUB_STEP_SUMMARY
  echo "" >> $GITHUB_STEP_SUMMARY
  echo "| File | Characters | Status |" >> $GITHUB_STEP_SUMMARY
  echo "|------|------------|--------|" >> $GITHUB_STEP_SUMMARY
fi

if [ -d "$CHANGELOG_DIR" ]; then
  shopt -s nullglob
  changelog_files=("$CHANGELOG_DIR"/*.txt)
  if [ ${#changelog_files[@]} -eq 0 ]; then
    echo "⚠️  No changelog files found in $CHANGELOG_DIR"
    if [ -n "$GITHUB_STEP_SUMMARY" ]; then
      echo "| - | - | ⚠️ **No changelog files found** |" >> $GITHUB_STEP_SUMMARY
    fi
  else
    for changelog in "${changelog_files[@]}"; do
      filename=$(basename "$changelog")
      char_count=$(wc -m < "$changelog" | tr -d ' ')
      
      echo "📄 $filename: $char_count characters"
      
      if [ "$char_count" -gt 500 ]; then
        echo "❌ ERROR: $changelog exceeds 500 character limit ($char_count characters)"
        if [ -n "$GITHUB_STEP_SUMMARY" ]; then
          echo "| \`$filename\` | **$char_count** | ❌ **EXCEEDS LIMIT** |" >> $GITHUB_STEP_SUMMARY
        fi
        FAILED=true
      else
        echo "✅ $filename is within limit"
        if [ -n "$GITHUB_STEP_SUMMARY" ]; then
          echo "| \`$filename\` | $char_count | ✅ Within limit |" >> $GITHUB_STEP_SUMMARY
        fi
      fi
    done
  fi
  shopt -u nullglob
else
  echo "⚠️  Changelog directory not found: $CHANGELOG_DIR"
  if [ -n "$GITHUB_STEP_SUMMARY" ]; then
    echo "| - | - | ⚠️ **Directory not found** |" >> $GITHUB_STEP_SUMMARY
  fi
fi

if [ -n "$GITHUB_STEP_SUMMARY" ]; then
  echo "" >> $GITHUB_STEP_SUMMARY
fi

if [ "$FAILED" = true ]; then
  if [ -n "$GITHUB_STEP_SUMMARY" ]; then
    echo "❌ **VALIDATION FAILED**" >> $GITHUB_STEP_SUMMARY
    echo "" >> $GITHUB_STEP_SUMMARY
    echo "One or more changelog files exceed the 500 character limit required by Google Play Store." >> $GITHUB_STEP_SUMMARY
    echo "Please reduce the changelog content to fit within the character limit before proceeding with the release." >> $GITHUB_STEP_SUMMARY
  fi
  echo ""
  echo "💥 Build failed: One or more changelog files exceed the 500 character limit"
  echo "📝 Please reduce the changelog content to fit within 500 characters"
  exit 1
else
  if [ -n "$GITHUB_STEP_SUMMARY" ]; then
    echo "✅ **VALIDATION PASSED**" >> $GITHUB_STEP_SUMMARY
    echo "" >> $GITHUB_STEP_SUMMARY
    echo "All changelog files are within the 500 character limit. Ready to proceed with release! 🚀" >> $GITHUB_STEP_SUMMARY
  fi
  echo ""
  echo "🎉 All changelog files are within the 500 character limit!"
fi