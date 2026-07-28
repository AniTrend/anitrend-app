---
name: string-resources-convention
description: 'Android strings.xml naming, XML translator comments, and POEditor context conventions. Use when adding, renaming, or reviewing string resources, placeholder documentation, and localization consistency.'
---

# Skill: String Resource Naming Conventions

This skill defines the naming policy for Android string resources and the mandatory translator
context rules for `strings.xml`.

For Android platform behavior around escaping, formatting, plurals, arrays, and styled text,
load [Android string resource best practices](./references/android-string-resource-best-practices.md).

## Naming pattern

`{prefix}_{module_or_context}_{specific_identifier}`

## Standard prefixes

| Prefix | Usage |
|---|---|
| `label_` | Field labels, section headers, descriptive text |
| `title_` | Screen titles, dialog titles, major headings |
| `subtitle_` | Secondary headings, descriptive subtitles |
| `placeholder_` | Input hints, empty state text |
| `action_` | Button text, menu items, actionable text |
| `message_` | User messages, notifications, feedback |
| `error_` | Error messages, validation messages |
| `hint_` | Helper text, tooltips, guidance |
| `description_` | Accessibility descriptions, detailed explanations |

## Module context guidelines

- Use **underscores** to separate words: `media_list_editor` not `medialisteditor`.
- Be specific but concise: `media_list` not `medialist`, `episode_progress` not `progress`.
- Include feature/module context for feature-specific strings.
- A string is shared only if it is used by more than one unrelated feature module or belongs to a
	common/core module with no single owning feature; all other strings are feature-specific and must
	include a module context segment.
- Use generic context only for shared strings such as `label_loading`, `action_save`, and
	`error_network`.
- Prefer positional placeholders such as `%1$s` and `%2$d` for translatable formatted strings.
- Add `formatted="true"` only when the string contains one or more printf-style placeholders such
	as `%1$s` or `%1$d`. Do not add it for escaped percent signs without real parameters.

## Platform best practices

- Follow Android escaping rules for apostrophes, quotes, `@`, `?`, newline, tab, and Unicode.
- Escape literal apostrophes in normal text with `\'` unless the entire string is deliberately wrapped in double quotes.
- Treat any literal `\u` sequence as dangerous unless you are intentionally writing a Unicode code point such as `\u0027`.
- Use `<plurals>` only when the grammar genuinely changes with quantity.
- Prefer quantity-neutral wording when plurals are avoidable.
- Use `getText()` when styled text must be preserved instead of flattened to plain text.
- Treat HTML markup and `<annotation>` usage as platform-level behavior, not ad hoc XML tricks.

## Good vs bad examples

**Good:**
```xml
<string name="label_media_list_editor_watch_status">Watch Status</string>
<string name="title_profile_settings">Profile Settings</string>
<string name="placeholder_search_anime_manga">Search anime and manga...</string>
<string name="action_mark_as_watched">Mark as Watched</string>
<string name="error_authentication_failed">Authentication failed</string>
```

**Bad:**
```xml
<string name="medialist_editor_watch_status">Watch Status</string>  <!-- Missing prefix -->
<string name="profileSettingsTitle">Profile Settings</string>        <!-- CamelCase, wrong prefix -->
<string name="searchHint">Search anime and manga...</string>         <!-- Generic, unclear purpose -->
<string name="watchedButton">Mark as Watched</string>               <!-- Context unclear -->
<string name="authError">Authentication failed</string>             <!-- Too abbreviated -->
```

## POEditor translator comments (required)

Always add an XML comment immediately before each string resource. POEditor displays these to
community translators.

This is mandatory for every resource block in `strings.xml` that is newly added or modified in the
current edit, including `<string>`, `<plurals>`, and `<string-array>`. A touched resource block is
one resource element whose name, value, or attributes changed in the edit. Write one comment per
resource element and do not share a comment across multiple resources.

**Format:**
```xml
<!-- Displayed when user hasn't set a rating yet -->
<string name="placeholder_media_score_section_rating">Not rated</string>

<!-- Button to save changes to user's anime/manga list -->
<string name="action_media_list_editor_save_changes">Save Changes</string>

<!-- Shows current episode progress out of total; %1$d is the current episode number -->
<string name="label_media_list_editor_progress_percentage" formatted="true">Progress %1$d%%</string>
```

**Effective comment guidelines:**
- **Context**: where/when the string appears in the app.
- **Purpose**: what action or information it represents.
- **Variables**: what each `%1$s` / `%1$d` parameter means.
- **Tone**: formal, casual, urgent, etc. if relevant.
- **Character limits**: note UI space constraints when applicable.

## Audit checklist

Use this checklist before finishing any `strings.xml` edit:

1. Scan the touched file or edited block for uncommented resources.
2. Add an XML comment immediately above every new or modified resource block.
3. For formatted strings, explain each placeholder and any escaping requirements.
4. Keep indentation and comment style consistent with the surrounding file.
5. Re-read the edited section to confirm there are no trailing uncommented additions.

## Migration guidelines

- Prefer the new naming convention when updating existing string resources.
- Add a replacement comment: `<!-- Replaces old_string_name -->`.
- Update all code references when renaming a string.
- Ensure plurals and translations follow the same naming pattern.

## Common misses

- Adding new strings at the bottom of a file without comments.
- Documenting only the first string in a cluster of related additions.
- Forgetting to explain `%1$s`, `%2$d`, or similar placeholders.
- Using non-positional placeholders or incorrect escaping in translatable strings.
- Leaving a literal apostrophe unescaped and only discovering it during AAPT resource flattening.
- Writing copy that accidentally contains a `\u` sequence Android tries to interpret as an invalid Unicode escape.
- Reaching for `<plurals>` when a quantity-neutral phrase would be safer.
- Writing engineering notes instead of translator-facing context.
