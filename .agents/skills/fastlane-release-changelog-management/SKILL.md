---
name: fastlane-release-changelog-management
description: "Use when creating or updating Google Play release notes for AniTrend. Manages Fastlane version-code changelogs in fastlane/metadata/android/en-GB/changelogs/<code>.txt, sourced from Release Drafter draft releases, within Google Play's 500-character limit. Does not publish releases, push tags, run Fastlane deployment, or edit version metadata."
---

# Skill: Fastlane Release Changelog Management

Use this skill when the task is to produce or refresh the Google Play release notes for a specific
AniTrend version code. The deliverable is always the exact file
`fastlane/metadata/android/en-GB/changelogs/<versionCode>.txt`.

## Scope

This skill covers release changelog management only.

May do:

- Create or update the exact `fastlane/metadata/android/en-GB/changelogs/<code>.txt` file for the
  current version defined in `gradle/version.properties`.
- Derive notes from the Release Drafter draft release for the current version. There is no
  fallback source: when the draft is unusable, the skill stops (see Draft Completeness).

Must NOT do:

- Publish releases or promote drafts.
- Push tags.
- Run Fastlane deployment lanes (`deploy`, `submit_beta`, `submit_prod`) or any
  `upload_to_play_store` call.
- Edit version metadata: `gradle/version.properties`, `app/.meta/version.json`, or the
  `version-updater.yml` pipeline files. Version bumps are automation-owned.

## Mandatory Preflight

Run all of the following before reading any draft or writing any file. Any failure stops the
skill.

1. Verify repository identity and authentication:

   ```bash
   gh auth status
   gh repo view AniTrend/anitrend-app --json nameWithOwner
   ```

   Require `AniTrend/anitrend-app` and an authenticated session that can read its releases.

2. Parse `gradle/version.properties` strictly. Require exactly one usable value each for
   `version`, `code`, and `name`. Duplicates, missing keys, malformed lines, and unexpected keys
   are hard failures. The supporting validator performs this parse; prefer it over ad-hoc greps.

3. Validate the values:

   - `version` must be strict numeric three-part SemVer: three dot-separated non-negative
     integers with no leading zeros, no `v` prefix, no pre-release or build suffixes.
     `1.13.0` is valid; `v1.13.0`, `1.13`, `1.13.0.1`, `1.13.0-rc1` are not.
   - `name` must equal `v$version`, e.g. `v1.13.0` for version `1.13.0`.
   - `code` must equal `major*1000000000 + minor*1000000 + patch*1000`, e.g.
     `1*1000000000 + 13*1000000 + 0*1000 = 1013000000`.
   - `code` must fit the signed Android int range: `0 <= code <= 2147483647`.

4. When `app/.meta/version.json` exists, require its `version` and `code` to match
   `gradle/version.properties`. A mismatch is a hard stop.

5. When local configuration permits (JDK 21 and a Gradle setup that does not require secrets),
   run:

   ```bash
   ./gradlew :app:assembleAppRelease --dry-run --no-daemon
   ```

   A failed dry-run means the configured version cannot build; stop instead of writing notes for
   an unbuildable version. Skip the dry-run only when local keystore/secrets configuration makes
   it impossible, and say so explicitly.

## Draft Selection

Never use an unqualified `gh release view`; it resolves to the latest release, which may be a
published tag or the wrong draft. Selection is JSON-based and exact-match only. Fetch the release
list through the GitHub REST API, paginated and flattened into one array:

```bash
gh api repos/AniTrend/anitrend-app/releases --paginate --slurp > /tmp/anitrend-release-pages.json

jq 'add' /tmp/anitrend-release-pages.json > /tmp/anitrend-releases.json

jq -r --arg tag "1.13.0" --arg name "v1.13.0" '
  [.[] | select(.draft == true and .tag_name == $tag and .name == $name
               and .target_commitish == "develop")] | length' /tmp/anitrend-releases.json
```

The flattened array uses GitHub REST release fields: `tag_name`, `name`, `draft`,
`target_commitish`, `body`, `created_at`, `published_at`.

Require exactly one match. The draft must satisfy all three conditions:

- `tag_name == <version>` (Release Drafter emits a numeric tag, e.g. `1.13.0`, no `v`).
- `name == <name>` (the draft title carries the `v` prefix, e.g. `v1.13.0`).
- `target_commitish == "develop"`.

Extract the body only from that single match:

```bash
jq -r --arg tag "1.13.0" --arg name "v1.13.0" '
  [.[] | select(.draft == true and .tag_name == $tag and .name == $name
               and .target_commitish == "develop")][0].body' /tmp/anitrend-releases.json
```

Stop immediately when the match count is zero or greater than one, or when any of the three
fields differs. Never widen the filter to make a draft fit.

## Draft Completeness

Treat the draft as incomplete only when its body is absent, empty, or has no usable entries
under the `# What's Changed` heading. Release Drafter always emits that heading; a draft with no
merged changes has no `- ` entries under it. A body that has entries under the heading is
complete, even if the entries are noisy.

Save the selected body to a temporary file and run the deterministic completeness check:

```bash
jq -r --arg tag "1.13.0" --arg name "v1.13.0" '
  [.[] | select(.draft == true and .tag_name == $tag and .name == $name
               and .target_commitish == "develop")][0].body' /tmp/anitrend-releases.json \
  > /tmp/anitrend-draft-body.txt

python3 - <<'PYEOF'
import re
body = open("/tmp/anitrend-draft-body.txt", encoding="utf-8").read()
heading = re.search(r"^# What's Changed[ \t]*$", body, re.M)
if not heading:
    raise SystemExit("draft incomplete: no '# What's Changed' heading; hard stop, no fallback")
section = body[heading.end():]
boundary = re.search(r"^(?:# |\*\*Full Changelog\*\*)", section, re.M)
if boundary:
    section = section[:boundary.start()]
if not re.search(r"^-\s+\S", section, re.M):
    raise SystemExit("draft incomplete: no bullet under '# What's Changed'; hard stop, no fallback")
PYEOF
```

The check extracts the exact section starting at the `# What's Changed` heading and ending at
the next top-level heading, a `**Full Changelog**` line, or the end of the body, then requires
at least one non-empty markdown bullet (`^-\s+\S`) inside that section. Bullets in other
sections do not count. An incomplete body is a hard stop; there is no merged-PR fallback:
commit-message issue-number extraction cannot reliably prove PR membership, base branch, or
range boundaries, so the skill must not invent scope from broad or guessed PR queries. The draft
must instead be refreshed or resolved by release automation (the Release Drafter workflow
regenerates the draft on the next push to `develop`), or reviewed manually, before this skill
continues. Stop and report which of the two is required.

## Generation Guidance

1. Read the draft body's categories (`🚀 Features`, `🐛 Bug Fixes`, `🧰 Maintenance`) and PR
   titles.
2. Map to the house style seen in recent changelogs:

   - Features to a `🚀 What's New:` section.
   - Bug fixes to a `🐛 Bug Fixes:` section.
   - Maintenance, refactor, and dependency entries to an `📈 Improvements:` section, usually
     collapsed into a single "Various stability improvements." style bullet, matching existing
     files.

3. Rewrite PR titles into concise user-facing en-GB prose. Strip conventional-commit prefixes
   such as `fix(ui):`, keep each bullet short, and skip noise such as the version-bump
   automation commit and Renovate dependency churn.
4. Use a safety budget of at most 480 characters for the whole file, every newline included,
   final newline included. Count with a UTF-8-aware tool, never bytes:

   ```bash
   python3 -c "import sys; print(len(open(sys.argv[1], encoding='utf-8').read()))" TARGET
   ```

   The current house footer (`⚠️ A Quick Note:` plus FAQ plus thanks lines) costs 353 characters
   including the final newline, leaving about 127 characters of bullets under the 480 budget.
   Trim or drop the quick note when the content does not fit.
5. Write UTF-8 without BOM. The filename must be exactly `<code>.txt` (e.g. `1013000000.txt`).
   Fastlane expects the exact `<versionCode>.txt`; `default.txt` is only a fallback and must
   never be altered. Historical changelogs must not be edited.
6. If the target file already exists, inspect it first and update it intentionally: preserve
   stable copy, change only entries that changed. Never overwrite blindly.
7. If the generated notes are identical to the existing target, write nothing and report the
   no-op.

## Required Before/After Scope Check

The worktree may already contain unrelated pre-existing changes. Protect scope precisely with
the scope helper (stdlib only, no network, writes only the snapshot file you name, never
repository files).

Before writing, capture the snapshot from the repository root:

```bash
python3 .agents/skills/fastlane-release-changelog-management/scripts/check-changelog-scope.py \
  snapshot /tmp/anitrend-scope-before.json
```

The snapshot records every `git status --porcelain=v1 -z --untracked-files=all` path with
spaces handled safely, its two-character status, and the SHA-256 of its current bytes when the
file is present. Rename and copy statuses are rejected rather than silently misrepresented.

After writing, verify against the snapshot, naming the exact target path:

```bash
python3 .agents/skills/fastlane-release-changelog-management/scripts/check-changelog-scope.py \
  verify /tmp/anitrend-scope-before.json \
  "fastlane/metadata/android/en-GB/changelogs/$CODE.txt"
```

The verify command ignores only the exact target path. Every other path must have identical
status and hash; the target must be the only allowed difference. Unrelated pre-existing changes
are allowed only if unchanged. A target absent from both the snapshot and the worktree manifest
passes only when it exists on disk as a regular, tracked, unchanged file (clean no-op, e.g. an
already committed changelog that needs no write); otherwise it fails as a typo or a write that
never happened. Stop on any unexpected path, status, or content change, on a deleted or
disappeared target, and on malformed snapshots (entries must carry a two-character status and a
null or 64-hex-character sha256).

Then run the validators, including the scope helper:

```bash
LC_ALL=en_US.UTF-8 bash .github/scripts/validate-changelogs.sh
python3 .agents/skills/fastlane-release-changelog-management/scripts/validate-release-changelog.py
python3 .agents/skills/fastlane-release-changelog-management/scripts/check-changelog-scope.py \
  verify /tmp/anitrend-scope-before.json \
  "fastlane/metadata/android/en-GB/changelogs/$CODE.txt"
```

## Current Pipeline Caveats

- `.github/scripts/validate-changelogs.sh` is warn-only for missing files: it fails CI only when
  a file exceeds 500 characters. The supporting validator is the strict local check for the
  target file's existence and content; the shell validator remains authoritative for CI and is
  run separately by this skill.
- In `.github/workflows/android-build.yaml`, the artifact upload and `gh release upload` steps
  run with `if: always()`, so APKs are published to the tag release even when earlier steps
  fail. A bad changelog therefore ships; the file must be right locally.
- `fastlane/Fastfile` sets `skip_metadata_upload = true`, so both `submit_beta` and
  `submit_prod` call `upload_to_play_store` with `skip_upload_metadata: true`. Fastlane
  currently does not upload changelog metadata to Google Play.
- Google Play enforces a 500 Unicode-character limit per locale; this project's en-GB changelog
  must stay within it. Fastlane reads changelog files as UTF-8, so a BOM or invalid UTF-8
  corrupts uploads.

## Quick Reference

```bash
# Preflight identity
gh auth status
gh repo view AniTrend/anitrend-app --json nameWithOwner

# Fetch release pages via REST API and flatten into one array
gh api repos/AniTrend/anitrend-app/releases --paginate --slurp > /tmp/anitrend-release-pages.json
jq 'add' /tmp/anitrend-release-pages.json > /tmp/anitrend-releases.json

# Draft body for exactly one match, saved for the completeness check
jq -r --arg tag "1.13.0" --arg name "v1.13.0" '
  [.[] | select(.draft == true and .tag_name == $tag and .name == $name
               and .target_commitish == "develop")][0].body' /tmp/anitrend-releases.json \
  > /tmp/anitrend-draft-body.txt

# Deterministic completeness check: bullet must be inside the "# What's Changed" section
python3 - <<'PYEOF'
import re
body = open("/tmp/anitrend-draft-body.txt", encoding="utf-8").read()
heading = re.search(r"^# What's Changed[ \t]*$", body, re.M)
if not heading:
    raise SystemExit("draft incomplete: no '# What's Changed' heading; hard stop, no fallback")
section = body[heading.end():]
boundary = re.search(r"^(?:# |\*\*Full Changelog\*\*)", section, re.M)
if boundary:
    section = section[:boundary.start()]
if not re.search(r"^-\s+\S", section, re.M):
    raise SystemExit("draft incomplete: no bullet under '# What's Changed'; hard stop, no fallback")
PYEOF

# Scope snapshot before writing (from the repository root)
python3 .agents/skills/fastlane-release-changelog-management/scripts/check-changelog-scope.py \
  snapshot /tmp/anitrend-scope-before.json

# Write notes (UTF-8, no BOM)
printf '%s' "$NOTES" > "fastlane/metadata/android/en-GB/changelogs/$CODE.txt"

# Scope verify after writing: only the target path may differ (clean no-op targets pass)
python3 .agents/skills/fastlane-release-changelog-management/scripts/check-changelog-scope.py \
  verify /tmp/anitrend-scope-before.json \
  "fastlane/metadata/android/en-GB/changelogs/$CODE.txt"

# Count characters, newlines included
python3 -c "import sys; print(len(open(sys.argv[1], encoding='utf-8').read()))" TARGET

# Validate
LC_ALL=en_US.UTF-8 bash .github/scripts/validate-changelogs.sh
python3 .agents/skills/fastlane-release-changelog-management/scripts/validate-release-changelog.py
python3 .agents/skills/fastlane-release-changelog-management/scripts/check-changelog-scope.py \
  verify /tmp/anitrend-scope-before.json \
  "fastlane/metadata/android/en-GB/changelogs/$CODE.txt"
```

## Common Failure Modes and Rationalizations

- "The draft name has an extra word, close enough." Stop. Only exact triple matches count.
- "There are two drafts, take the newer one." Stop. Resolve the duplicate upstream.
- "The draft is empty, just query recent PRs to fill it in." Stop. Commit-message issue numbers
  cannot prove PR membership, base, or range; refresh the draft via release automation or review
  it manually.
- "The draft body exists but has no bullets under the heading, close enough." Stop. The
  deterministic completeness check extracts the section under `# What's Changed` (up to the next
  top-level heading, `**Full Changelog**`, or EOF) and requires at least one non-empty bullet
  (`^-\s+\S`) inside it; a bullet in another section does not count.
- "The status diff looks clean, so nothing else changed." Stop. Status alone misses content
  edits; the scope helper hashes every path and rejects rename/copy statuses instead of
  approximating them.
- "The changelog is already committed and clean, so I skipped the scope verify." Stop. Run
  verify anyway; a clean tracked target present in neither manifest passes as a no-op, and the
  rest of the worktree is still checked.
- "I changed a bullet in an old changelog to keep the style consistent." Stop. Historical files
  are frozen.
- "default.txt is the same thing." No. It is only a fallback and stays untouched.
- "wc -c says 480 so we are fine." Bytes are not characters. Count Unicode characters.
- "The file was already there, so I overwrote it." Inspect first, then update intentionally.

## Explicit Stop Conditions

- Preflight: auth or repo identity mismatch; `version.properties` unparsable, duplicate, or with
  unexpected keys; `version` not strict numeric three-part SemVer; `name != v$version`; code
  formula mismatch; code outside `0..2147483647`; `version.json` mismatch; gradle dry-run
  failure (when run).
- Draft: zero, multiple, or ambiguous drafts; missing fields; incomplete draft body (absent,
  empty, or failing the deterministic check: no `# What's Changed` heading, or no non-empty
  bullet inside the extracted section). No fallback exists; refresh via release automation or
  review manually.
- Generation: character budget exceeded (480); any uncertainty about the draft's intended
  audience.
- Scope: the scope helper rejects rename/copy statuses; any path other than the target changelog
  changed relative to the pre-write snapshot (path, status, or content), including unrelated
  pre-existing changes that are no longer byte-for-byte or status-identical; the target is
  deleted (status D) or disappeared from the worktree; the target is absent from both the
  snapshot and the worktree without being a clean tracked file on disk; the snapshot file is
  missing, invalid, or has malformed entries (status not a two-character string, sha256 not null
  or a 64-hex-character string).
- Validation: any of the three commands exits nonzero (shell validator, changelog validator,
  scope helper verify).
