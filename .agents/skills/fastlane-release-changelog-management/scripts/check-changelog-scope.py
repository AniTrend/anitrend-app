#!/usr/bin/env python3
"""Capture and verify repository scope for changelog edits.

Commands:
  snapshot SNAPSHOT_PATH
      Run from the repository root. Captures every path reported by
      `git status --porcelain=v1 -z --untracked-files=all`, its two-character
      status, and the SHA-256 of its current bytes when the file is present on
      disk. Paths with spaces are handled safely (records are NUL-separated and
      paths are not re-quoted). Rename and copy statuses are rejected because
      porcelain v1 -z cannot represent the origin/destination pair in a single
      record; refusing is safer than misrepresenting scope. Writes
      deterministic JSON to SNAPSHOT_PATH and refuses to write the snapshot
      inside the repository.

  verify SNAPSHOT_PATH TARGET_RELATIVE_PATH
      Run from the repository root. Captures the same manifest and compares it
      with the snapshot, ignoring only the exact target path. Every non-target
      path must have identical status and hash; the target must be the only
      allowed difference. A target present in the worktree (newly created,
      staged, or modified) passes. A deleted target (status D) or one that was
      in the snapshot but disappeared fails. If the target is absent from both
      manifests, verification passes only when the target exists on disk as a
      regular tracked file that is unchanged (clean no-op); a target missing on
      disk fails. Snapshot entries are validated before comparison: each entry
      must be an object with a two-character string status and a sha256 that is
      null or a 64-hex-character string; malformed snapshots are rejected with
      clear errors, never tracebacks.

No network access, no writes to repository files (the snapshot file must live
outside the repository), Python standard library only.
"""

import hashlib
import json
import os
import re
import subprocess
import sys
from typing import NoReturn

GIT_STATUS_ARGS = ["git", "status", "--porcelain=v1", "-z", "--untracked-files=all"]
FORMAT_VERSION = 1
REJECTED_STATUS_CHARS = "RC"

USAGE = """\
usage:
  check-changelog-scope.py snapshot SNAPSHOT_PATH
  check-changelog-scope.py verify SNAPSHOT_PATH TARGET_RELATIVE_PATH
  check-changelog-scope.py --help

snapshot SNAPSHOT_PATH
    Run from the repository root. Capture every git status path with its
    two-character status and a SHA-256 of its current bytes (when present),
    written as deterministic JSON to SNAPSHOT_PATH (must be outside the repo).

verify SNAPSHOT_PATH TARGET_RELATIVE_PATH
    Run from the repository root. Capture the same manifest and compare it with
    the snapshot, ignoring only the exact target path. Non-target paths must
    keep identical status and hashes; the target must be the only allowed
    difference. A clean unchanged target present in neither manifest is a valid
    no-op. Deleted, disappeared, or missing targets fail. Rename/copy statuses
    are rejected, not approximated. Snapshot entries are shape-validated.
"""


def fail(message: str, code: int = 1) -> NoReturn:
    print("ERROR: " + message, file=sys.stderr)
    sys.exit(code)


def repo_root_or_fail():
    proc = subprocess.run(["git", "rev-parse", "--show-toplevel"],
                          capture_output=True, text=True)
    if proc.returncode != 0:
        fail("not inside a git repository")
    # realpath normalizes symlinked prefixes (e.g. /var -> /private/var on macOS)
    root = os.path.realpath(proc.stdout.strip())
    if root != os.path.realpath(os.getcwd()):
        fail("must run from the repository root: %s" % root)
    return root


def file_sha256(path):
    if not os.path.isfile(path):
        return None
    digest = hashlib.sha256()
    with open(path, "rb") as handle:
        for chunk in iter(lambda: handle.read(65536), b""):
            digest.update(chunk)
    return digest.hexdigest()


def capture_manifest():
    proc = subprocess.run(GIT_STATUS_ARGS, capture_output=True, text=True)
    if proc.returncode != 0:
        fail("git status failed: %s" % proc.stderr.strip())
    manifest = {}
    for record in proc.stdout.split("\0"):
        if not record:
            continue
        if len(record) < 4:
            fail("unexpected short git status record: %r" % record)
        status = record[0:2]
        path = record[3:]
        if status[0] in REJECTED_STATUS_CHARS or status[1] in REJECTED_STATUS_CHARS:
            fail("unsupported rename/copy status %r for %r; porcelain v1 -z cannot represent "
                 "the origin/destination pair, refusing to misrepresent scope"
                 % (status, path))
        manifest[path] = {"status": status, "sha256": file_sha256(path)}
    return manifest


def normalize_target(target):
    if not target:
        fail("target path must not be empty")
    target = target.replace(os.sep, "/")
    while target.startswith("./"):
        target = target[2:]
    if target.startswith("/") or os.path.isabs(target):
        fail("target path must be relative: %r" % target)
    if any(part in ("", "..") for part in target.split("/")):
        fail("target path must not contain empty or '..' components: %r" % target)
    return target


def cmd_snapshot(snapshot_path):
    root = repo_root_or_fail()
    abs_snapshot = os.path.realpath(snapshot_path)
    if abs_snapshot != root and os.path.commonpath([root, abs_snapshot]) == root:
        fail("refusing to write snapshot inside the repository: %s "
             "(use a path outside the repo, e.g. /tmp)" % snapshot_path)
    manifest = capture_manifest()
    payload = {"format": FORMAT_VERSION, "entries": manifest}
    with open(snapshot_path, "w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2, sort_keys=True, ensure_ascii=False)
        handle.write("\n")
    print("snapshot written: %s (%d entries)" % (snapshot_path, len(manifest)))


def validate_snapshot_entries(entries, snapshot_path):
    for path, entry in sorted(entries.items()):
        if not isinstance(entry, dict):
            fail("snapshot %s entry %r must be an object; got %s"
                 % (snapshot_path, path, type(entry).__name__))
        if "status" not in entry or "sha256" not in entry:
            fail("snapshot %s entry %r is missing status or sha256"
                 % (snapshot_path, path))
        status = entry["status"]
        if not isinstance(status, str) or len(status) != 2:
            fail("snapshot %s entry %r has invalid status %r; expected a two-character "
                 "string" % (snapshot_path, path, status))
        sha256 = entry["sha256"]
        if sha256 is not None and (not isinstance(sha256, str)
                                   or not re.fullmatch(r"[0-9a-f]{64}", sha256)):
            fail("snapshot %s entry %r has invalid sha256 %r; expected null or a "
                 "64-hex-character string" % (snapshot_path, path, sha256))


def load_snapshot(snapshot_path):
    if not os.path.isfile(snapshot_path):
        fail("snapshot file not found: %s" % snapshot_path)
    try:
        with open(snapshot_path, "r", encoding="utf-8") as handle:
            payload = json.load(handle)
    except (OSError, ValueError) as exc:
        fail("cannot load snapshot %s: %s" % (snapshot_path, exc))
    if not isinstance(payload, dict):
        fail("snapshot %s is not a JSON object (format %d expected)"
             % (snapshot_path, FORMAT_VERSION))
    if payload.get("format") != FORMAT_VERSION:
        fail("snapshot %s has unsupported format %r (expected %d)"
             % (snapshot_path, payload.get("format"), FORMAT_VERSION))
    entries = payload.get("entries")
    if not isinstance(entries, dict):
        fail("snapshot %s is missing a valid entries object" % snapshot_path)
    validate_snapshot_entries(entries, snapshot_path)
    return entries


def target_is_clean_noop(target):
    """True when the target is a regular file, tracked, and unchanged vs HEAD."""
    if not os.path.isfile(target):
        return False
    tracked = subprocess.run(["git", "ls-files", "--error-unmatch", "--", target],
                             capture_output=True, text=True)
    if tracked.returncode != 0:
        return False
    unstaged = subprocess.run(["git", "diff", "--quiet", "--", target],
                              capture_output=True, text=True)
    staged = subprocess.run(["git", "diff", "--cached", "--quiet", "--", target],
                            capture_output=True, text=True)
    return unstaged.returncode == 0 and staged.returncode == 0


def cmd_verify(snapshot_path, target_raw):
    repo_root_or_fail()
    target = normalize_target(target_raw)
    snapshot_entries = load_snapshot(snapshot_path)
    current = capture_manifest()

    noop_target = False
    if target not in current and target not in snapshot_entries:
        if target_is_clean_noop(target):
            noop_target = True
        else:
            fail("target path %r is absent from both the snapshot and the worktree status "
                 "manifest and is not a clean tracked file on disk; nothing was written on the "
                 "target path or the target path is wrong" % target)
    elif target not in current:
        fail("target path %r is in the snapshot but is no longer present in the worktree" % target)
    else:
        target_status = current[target]["status"]
        if "D" in target_status:
            fail("target path %r is deleted in the worktree (status %r); the skill never "
                 "deletes the target" % (target, target_status))

    problems = []
    for path, entry in sorted(snapshot_entries.items()):
        if path == target:
            continue
        if path not in current:
            problems.append("path disappeared from the worktree: %r" % path)
            continue
        cur = current[path]
        if cur["status"] != entry["status"]:
            problems.append("status changed for %r: was %r, now %r"
                            % (path, entry["status"], cur["status"]))
        if cur["sha256"] != entry["sha256"]:
            problems.append("content changed for %r: sha256 %r -> %r"
                            % (path, entry["sha256"], cur["sha256"]))
    for path in sorted(current):
        if path != target and path not in snapshot_entries:
            problems.append("unexpected new path in the worktree: %r" % path)
    if problems:
        fail("scope check failed:\n  - " + "\n  - ".join(problems))
    if noop_target:
        print("scope OK: no unexpected changes; target %r is a clean unchanged no-op" % target)
    else:
        print("scope OK: no unexpected changes outside %r" % target)


def main(argv):
    if len(argv) == 1:
        print(USAGE, file=sys.stderr)
        return 2
    if argv[1] in ("-h", "--help", "help"):
        print(USAGE)
        return 0
    command = argv[1]
    if command == "snapshot":
        if len(argv) != 3:
            print(USAGE, file=sys.stderr)
            return 2
        cmd_snapshot(argv[2])
        return 0
    if command == "verify":
        if len(argv) != 4:
            print(USAGE, file=sys.stderr)
            return 2
        cmd_verify(argv[2], argv[3])
        return 0
    print("unknown command: %s" % command, file=sys.stderr)
    print(USAGE, file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv))
