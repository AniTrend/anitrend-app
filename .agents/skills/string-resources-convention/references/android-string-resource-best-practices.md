# Android String Resource Best Practices

Source: https://developer.android.com/guide/topics/resources/string-resource

Use this reference when a string-resource task depends on Android platform behavior, not just this
repo's naming or translator-comment conventions.

## Use This For

- Escaping apostrophes, quotes, `@`, `?`, newlines, tabs, and Unicode characters.
- Positional formatting placeholders such as `%1$s` and `%2$d`.
- Choosing between `<string>`, `<string-array>`, and `<plurals>`.
- Preserving styled text or converting HTML markup safely.
- Avoiding localization bugs caused by whitespace, plurals, or inline markup.

## Core Platform Rules

### 1. Escape special characters correctly

- Apostrophes must be escaped with `\'` unless the string is wrapped in double quotes.
- Double quotes must be escaped with `\"` when they should appear literally.
- Escape `@` as `\@` and `?` as `\?` when they should not be treated as resource syntax.
- Use `\n` for new lines, `\t` for tabs, and `\uXXXX` for explicit Unicode characters.
- Never leave a stray `\u` sequence in plain copy. Android will try to parse it as a Unicode escape and fail resource flattening if the next four characters are not valid hex.
- Android collapses repeated whitespace unless the relevant region is wrapped in double quotes.

### 2. Prefer positional formatting placeholders

- Use `%1$s`, `%2$d`, and similar positional placeholders in translatable strings.
- Mark formatted resources with `formatted="true"` when the file convention expects it.
- Document every placeholder in the XML translator comment immediately above the resource.
- Use `getString(id, args...)` for formatted strings and `getText(id)` when you need to preserve styled text.

### 3. Use plurals only for grammatical quantity

- Use `<plurals>` only when grammar changes with count.
- Always provide at least `one` and `other`; translators determine whether additional quantities are needed.
- If the displayed message includes the count, pass the count twice to `getQuantityString(...)`: once for selection and once for formatting.
- Prefer quantity-neutral phrasing when acceptable because it reduces localization complexity.

### 4. Choose the right resource type

- Use `<string>` for single text values.
- Use `<string-array>` for fixed arrays of text choices.
- Use `<plurals>` for grammatically count-sensitive copy.
- Keep translator comments on every touched resource block, including arrays and plurals.

### 5. Be careful with styled text

- Basic HTML tags such as `<b>`, `<i>`, `<u>`, `<br>`, `<ul>`, and `<li>` are supported in string resources.
- If a string is both formatted and styled, HTML tags usually need to be escaped in XML and rehydrated with `Html.fromHtml(...)` after formatting.
- HTML-encode dynamic text before passing it into an HTML-formatted string.
- For more complex or reusable styling, prefer spans or `<annotation>` tags over fragile inline HTML.
- If using `<annotation>`, apply the annotation consistently across every translation.

## Review Checklist

1. Confirm the correct resource type: string, array, or plurals.
2. Check for Android-specific escaping needs before changing punctuation.
3. Use positional placeholders and document them in XML comments.
4. Validate whether styled text should remain plain text, HTML, or annotations.
5. For plurals, confirm the copy is truly grammatically count-sensitive.

## Common Failure Modes

- Unescaped apostrophes causing AAPT resource compilation failures.
- Copy that includes a literal `\u` sequence, which AAPT treats as an invalid Unicode escape during flattening.
- Non-positional placeholders in translatable text.
- Using plurals for UI state labels that are not grammatically quantity-driven.
- Mixing HTML markup with formatting arguments without escaping or `Html.fromHtml(...)`.
- Forgetting that translator context must still exist even when Android syntax is technically correct.
