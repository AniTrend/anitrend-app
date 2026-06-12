## Title

Enable reliable auto-merge for `version-updater.yml`

## Context

The `version-update` workflow currently creates a PR from `platform/update-version-meta-data` into `develop` using `peter-evans/create-pull-request@v8` with the default `GITHUB_TOKEN`. That token does not trigger downstream `on: pull_request` and `on: push` workflows for PRs created by the action, which prevents required checks from appearing consistently and blocks auto-merge.

Related repository behavior already in place:

- `release-drafter.yml` dispatches the `version-update-and-push` event.
- `auto-approve.yml` already recognizes `platform/update-version-meta-data` and auto-approves PRs from that branch.
- PRs are expected to target `develop`.

## Decision

Use a GitHub App token generated at runtime from `APP_ID` and `APP_PRIVATE_KEY`, then use that token for checkout, PR creation, and enabling auto-merge.

This is preferred over using the default `GITHUB_TOKEN` plus a reopen workaround because the App token allows the created PR to trigger normal PR/push workflows and keeps the workflow deterministic.

## Workflow Changes

1. Add a token generation step using `actions/create-github-app-token`.
2. Use the App token in `actions/checkout` so subsequent git operations are consistently authenticated with the same identity.
3. Use the App token in `peter-evans/create-pull-request@v8`.
4. Capture the previous and next version in the workflow so the PR title/body can describe the bump as `from -> to`.
5. Add an auto-merge enablement step that only runs when the PR action creates or updates a PR.
6. Keep the existing automation branch `platform/update-version-meta-data`, base branch `develop`, and `skip-changelog` label so current release automation continues to work.

## Permissions

Top-level workflow permissions should remain minimal.

The version update job should keep or add only the permissions needed for:

- updating contents
- creating or updating pull requests

If the auto-merge step uses the GitHub CLI or API with the App token, no broader repository policy changes are planned inside this workflow.

## PR Metadata

The PR should become more descriptive:

- Title example: `platform: bump version 2.3.4 -> 2.3.5`
- Body should state which generated files changed and explicitly mention the version transition.

The commit message can remain automation-focused, but it should also include the bumped version when practical.

## Validation Plan

After implementation:

1. Verify workflow YAML syntax is valid.
2. Verify the workflow still targets `develop` and the automation branch name is unchanged.
3. Verify the PR creation step now uses the App token.
4. Verify the auto-merge step is conditional and uses the created PR number.
5. Verify the resulting git diff is limited to the intended workflow changes.

## Risks And Mitigations

- **Missing app secrets**: the workflow will fail early when `APP_ID` or `APP_PRIVATE_KEY` is unavailable.
- **Insufficient app permissions**: document the need for `contents: write`, `pull requests: write`, and workflow-related access if required for workflow-file PRs.
- **Auto-merge still blocked by branch protection**: this change does not bypass protection rules; it ensures the expected checks can run so auto-merge can complete normally.

## Non-Goals

- Changing repository branch protection rules.
- Renaming the automation branch.
- Refactoring unrelated GitHub workflows.
