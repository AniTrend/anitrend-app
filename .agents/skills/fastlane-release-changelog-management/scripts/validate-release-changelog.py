#!/usr/bin/env python3
"""Validate the release changelog state of the AniTrend repository.

Read-only validator: no network access, no release publishing, no file writes.
It validates the current repository state only.

Checks:
- gradle/version.properties parses strictly (exactly one version, code, name;
  duplicates, missing keys, malformed lines and unexpected keys are failures).
- version is strict numeric three-part SemVer (no v prefix, no leading zeros,
  no pre-release/build suffixes).
- name equals "v" + version.
- code equals major*1000000000 + minor*1000000 + patch*1000.
- code fits the signed Android int range 0..2147483647.
- app/.meta/version.json (when present) parses strictly (non-standard constants such as NaN
  and Infinity and duplicate object keys are rejected) and is a JSON object whose version is a
  string and code is a non-boolean integer matching version and code.
- the target fastlane/metadata/android/en-GB/changelogs/<code>.txt exists, is a
  regular non-empty file, decodes as UTF-8, has no BOM, and contains 1..500
  characters (Unicode code points, newlines included; bytes are not counted).

The repository shell validator (.github/scripts/validate-changelogs.sh) remains
authoritative for CI and is run separately by the skill.

Usage:
    python3 validate-release-changelog.py [REPO_ROOT]

REPO_ROOT defaults to the current directory. Exits 0 on success, nonzero on
validation failure.
"""

import json
import os
import re
import sys
from typing import NoReturn

INT32_MAX = 2_147_483_647

VERSION_PROPERTIES_REL = os.path.join("gradle", "version.properties")
VERSION_JSON_REL = os.path.join("app", ".meta", "version.json")
CHANGELOG_DIR_REL = os.path.join("fastlane", "metadata", "android", "en-GB", "changelogs")

STRICT_SEMVER_RE = re.compile(r"^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$")

# Non-negative decimal digits, no leading zeros: only "0" or a digit 1-9 followed by digits.
CODE_RE = re.compile(r"^(0|[1-9][0-9]*)$")

REQUIRED_KEYS = ("version", "code", "name")


def fail(message: str) -> NoReturn:
    print("ERROR: " + message, file=sys.stderr)
    sys.exit(1)


def parse_version_properties(path):
    """Return {key: value} parsed strictly from gradle/version.properties."""
    if not os.path.isfile(path):
        fail("missing %s; is the given path a repository root?" % VERSION_PROPERTIES_REL)

    values = {}
    line_numbers = {}
    with open(path, "r", encoding="utf-8") as handle:
        for lineno, raw in enumerate(handle, 1):
            line = raw.strip()
            if not line or line.startswith("#"):
                continue
            if "=" not in line:
                fail("%s:%d malformed line, expected key=value: %r"
                     % (VERSION_PROPERTIES_REL, lineno, line))
            key, _, value = line.partition("=")
            key = key.strip()
            value = value.strip()
            if not key or not value:
                fail("%s:%d malformed key/value pair: %r"
                     % (VERSION_PROPERTIES_REL, lineno, line))
            if key in values:
                fail("%s:%d duplicate key %r (first defined at line %d)"
                     % (VERSION_PROPERTIES_REL, lineno, key, line_numbers[key]))
            values[key] = value
            line_numbers[key] = lineno

    missing = [key for key in REQUIRED_KEYS if key not in values]
    if missing:
        fail("missing key(s) in %s: %s" % (VERSION_PROPERTIES_REL, ", ".join(missing)))
    unexpected = [key for key in values if key not in REQUIRED_KEYS]
    if unexpected:
        fail("unexpected key(s) in %s: %s"
             % (VERSION_PROPERTIES_REL, ", ".join(sorted(unexpected))))
    return values


def parse_code(code_str):
    if CODE_RE.match(code_str):
        return int(code_str, 10)
    if code_str.startswith(("+", "-")):
        fail("code %r in %s must not have a sign; use plain decimal digits"
             % (code_str, VERSION_PROPERTIES_REL))
    if code_str.isdigit():
        fail("code %r in %s has leading zeros; use 0 or decimal digits starting with 1-9"
             % (code_str, VERSION_PROPERTIES_REL))
    fail("code %r in %s is not a non-negative decimal integer"
         % (code_str, VERSION_PROPERTIES_REL))


def check_version_meta(repo_root, version, code):
    path = os.path.join(repo_root, VERSION_JSON_REL)
    if not os.path.isfile(path):
        print("note: %s not found; comparison skipped" % VERSION_JSON_REL)
        return

    def reject_constant(constant):
        fail("%s contains non-standard JSON constant %r" % (VERSION_JSON_REL, constant))

    def reject_duplicate_keys(pairs):
        seen = {}
        for key, value in pairs:
            if key in seen:
                fail("%s contains duplicate key %r" % (VERSION_JSON_REL, key))
            seen[key] = value
        return seen

    try:
        with open(path, "r", encoding="utf-8") as handle:
            meta = json.load(handle,
                             parse_constant=reject_constant,
                             object_pairs_hook=reject_duplicate_keys)
    except (OSError, ValueError) as exc:
        fail("cannot parse %s: %s" % (VERSION_JSON_REL, exc))
    if not isinstance(meta, dict):
        fail("%s must be a JSON object with version and code; got %s"
             % (VERSION_JSON_REL, type(meta).__name__))
    if "version" not in meta:
        fail("%s is missing the version key" % VERSION_JSON_REL)
    if "code" not in meta:
        fail("%s is missing the code key" % VERSION_JSON_REL)
    meta_version = meta["version"]
    meta_code = meta["code"]
    if not isinstance(meta_version, str):
        fail("%s version must be a string; got %s"
             % (VERSION_JSON_REL, type(meta_version).__name__))
    if isinstance(meta_code, bool) or not isinstance(meta_code, int):
        fail("%s code must be a JSON integer, not a boolean or other type; got %s"
             % (VERSION_JSON_REL, type(meta_code).__name__))
    mismatches = []
    if meta_version != version:
        mismatches.append("version %r does not match %r" % (meta_version, version))
    if meta_code != code:
        mismatches.append("code %r does not match %d" % (meta_code, code))
    if mismatches:
        fail("%s does not match %s: %s"
             % (VERSION_JSON_REL, VERSION_PROPERTIES_REL, "; ".join(mismatches)))


def check_changelog(repo_root, code):
    target_rel = os.path.join(CHANGELOG_DIR_REL, "%d.txt" % code)
    target_path = os.path.join(repo_root, target_rel)
    if not os.path.exists(target_path):
        fail("target changelog missing: %s (note: .github/scripts/validate-changelogs.sh is "
             "warn-only for missing files; this validator treats it as a hard error)" % target_rel)
    if not os.path.isfile(target_path):
        fail("target changelog is not a regular file: %s" % target_rel)
    if os.path.getsize(target_path) == 0:
        fail("target changelog is empty: %s" % target_rel)
    with open(target_path, "rb") as handle:
        raw = handle.read()
    if raw.startswith(b"\xef\xbb\xbf"):
        fail("target changelog has a UTF-8 BOM: %s" % target_rel)
    try:
        text = raw.decode("utf-8")
    except UnicodeDecodeError as exc:
        fail("target changelog is not valid UTF-8: %s (%s)" % (target_rel, exc))
    char_count = len(text)
    if not 1 <= char_count <= 500:
        fail("target changelog has %d characters; Google Play allows 1..500 Unicode characters "
             "per locale (newlines included)" % char_count)
    return target_rel, char_count


def main(argv):
    if len(argv) > 2:
        print("usage: validate-release-changelog.py [REPO_ROOT]", file=sys.stderr)
        return 2
    repo_root = os.path.abspath(argv[1] if len(argv) == 2 else os.getcwd())
    if not os.path.isdir(repo_root):
        fail("repo root is not a directory: %s" % repo_root)

    props_path = os.path.join(repo_root, VERSION_PROPERTIES_REL)
    values = parse_version_properties(props_path)

    version = values["version"]
    match = STRICT_SEMVER_RE.match(version)
    if not match:
        fail("version %r is not strict numeric three-part SemVer (three dot-separated integers, "
             "no v prefix, no leading zeros, no pre-release/build suffixes)" % version)
    major, minor, patch = (int(part) for part in match.groups())

    name = values["name"]
    expected_name = "v" + version
    if name != expected_name:
        fail("name %r does not equal v$version (%r)" % (name, expected_name))

    code = parse_code(values["code"])
    expected_code = major * 1_000_000_000 + minor * 1_000_000 + patch * 1_000
    if code != expected_code:
        fail("code %d does not match formula major*1000000000 + minor*1000000 + patch*1000 "
             "(%d for version %s)" % (code, expected_code, version))
    if not 0 <= code <= INT32_MAX:
        fail("code %d is outside the signed Android int range 0..%d" % (code, INT32_MAX))

    check_version_meta(repo_root, version, code)
    target_rel, char_count = check_changelog(repo_root, code)

    print("OK: version %s (%s), code %d, changelog %s (%d characters)"
         % (version, name, code, target_rel, char_count))
    print("note: .github/scripts/validate-changelogs.sh remains authoritative for CI and is run "
         "separately by the skill")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
