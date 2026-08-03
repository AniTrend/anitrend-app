package com.mxt.anitrend.architecture

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Kotlin-first domain/navigation architecture enforcement, ADR section 9 (Phase 1).
 *
 * Guardrails implemented here (ADR `docs/adr/2026-08-01-kotlin-first-domain-model-and-navigation.md`):
 * 1. `com.mxt.anitrend.model.entity.**` is frozen at the reviewed 95-file baseline.
 * 2. No new handwritten Parcelable code (scoped exceptions: legacy model/entity parceling,
 *    MaterialSearchView.SavedState).
 * 3. The domain layer stays pure Kotlin; only the documented generated GraphQL enum/input
 *    exceptions are allowed.
 * 4. Domain models (files under a `domain` `model` directory) expose only `val` properties.
 * 5. Presentation roots never import local persistence entities or ObjectBox.
 * 6. No new model mutation methods; legacy entity mutation methods are baseline entries.
 *
 * Baselines live in `app/src/test/resources/architecture/` and are documented in its README.md.
 * Baseline entries are removed in the same PR that migrates the underlying violation.
 */
class KotlinFirstArchitectureEnforcementTest {

    private val projectRoot: File = findProjectRoot()

    private val architectureResources: File =
        projectRoot.resolve("app/src/test/resources/architecture")

    // 1. model.entity frozen file set.
    @Test
    fun `model entity package is frozen at the reviewed baseline`() {
        val current = kotlinFiles("app/src/main/java/com/mxt/anitrend/model/entity")
            .map { it.relativeTo(projectRoot).path }
            .toSet()
        val baseline = baselineEntries("model-entity-baseline.txt").toSet()
        val additions = current - baseline

        assertFalse(
            "ADR section 9 (Phase 1): com.mxt.anitrend.model.entity is a frozen legacy " +
                "compatibility package that must only shrink. New classes must be placed in the " +
                "domain, data, or presentation packages instead. New files:\n" +
                additions.sorted().joinToString("\n"),
            additions.isNotEmpty(),
        )
    }

    // 2. No new handwritten Parcelable implementations.
    @Test
    fun `no new handwritten Parcelable implementations are introduced`() {
        val mainFiles = kotlinFiles("app/src/main/java/com/mxt/anitrend")
        val currentCounts = mutableMapOf<String, Int>()
        mainFiles.forEach { file ->
            val path = file.relativeTo(projectRoot).path
            file.readLines().forEach { rawLine ->
                if (parcelablePattern.containsMatchIn(rawLine)) {
                    val key = "$path:${rawLine.trim()}"
                    currentCounts[key] = (currentCounts[key] ?: 0) + 1
                }
            }
        }
        val baselineCounts = baselineEntries("parcelable-baseline.txt").groupingBy { it }.eachCount()
        val additions = currentCounts.flatMap { (key, count) ->
            val allowed = baselineCounts[key] ?: 0
            if (count > allowed) List(count - allowed) { key } else emptyList()
        }

        assertFalse(
            "ADR section 9 (Phase 1) and section 7.6: handwritten Parcelable code " +
                "(writeToParcel, describeContents, Parcelable.Creator, CREATOR) is prohibited. " +
                "Existing model/entity parceling and MaterialSearchView.SavedState are scoped " +
                "exceptions. New violations:\n" + additions.joinToString("\n"),
            additions.isNotEmpty(),
        )
    }

    // 3a. Domain must not import Android, ObjectBox, Gson, or kotlinx.parcelize.
    @Test
    fun `domain does not import android androidx objectbox gson or kotlinx parcelize`() {
        val domainFiles = kotlinFiles("app/src/main/java/com/mxt/anitrend/domain")
        val violations = domainFiles.matchingTrimmedLines(forbiddenDomainImportPattern)

        assertFalse(
            "ADR section 4.1 and section 9 (Phase 1): the domain layer must stay pure Kotlin. " +
                "android.*, androidx.*, io.objectbox.*, com.google.gson.*, and kotlinx.parcelize.* " +
                "imports are prohibited. Violations:\n" + violations.joinToString("\n"),
            violations.isNotEmpty(),
        )
    }

    // 3b. Domain GraphQL imports are confined to the documented exceptions.
    @Test
    fun `domain only uses the documented generated GraphQL exceptions`() {
        val domainFiles = kotlinFiles("app/src/main/java/com/mxt/anitrend/domain")
        val current = domainFiles.matchingTrimmedLines(graphqlImportPattern)
        val baseline = baselineEntries("domain-graphql-baseline.txt").toSet()
        val violations = current.filterNot { it in baseline }

        assertFalse(
            "ADR section 4.1 and section 9 (Phase 1): generated GraphQL types must not be exposed " +
                "through domain APIs except the documented enum/input exceptions (MediaListStatus, " +
                "MediaType, FuzzyDateInput, ScoreFormat, LikeableType, ReviewRating, ReviewSort, " +
                "ActivityType, MediaListSort). Violations:\n" + violations.joinToString("\n"),
            violations.isNotEmpty(),
        )
    }

    // 4. Domain models expose only val properties.
    @Test
    fun `domain models expose only val properties`() {
        val modelFiles = domainModelFiles()
        val violations = mutableListOf<String>()
        modelFiles.forEach { file ->
            val path = file.relativeTo(projectRoot).path
            val lines = file.readLines()
            val scanner = BraceContextScanner(lines)
            lines.forEachIndexed { index, rawLine ->
                val stripped = stripCommentsAndStrings(rawLine)
                val context = scanner.contexts[index]
                val varMatch = varDeclarationPattern.find(stripped)
                if (varMatch != null) {
                    val inClassBody = context.braceContextAtStart == BraceContext.CLASS
                    val inPendingConstructor = context.pendingClassHeaderAtStart &&
                        context.braceContextAtStart != BraceContext.FUNC
                    val sameLineClassHeader = classKeywordPattern
                        .findAll(stripped)
                        .any { it.range.first < varMatch.range.first }
                    if (inClassBody || inPendingConstructor || sameLineClassHeader) {
                        violations.add("$path:${index + 1}: ${rawLine.trim()}")
                    }
                }
            }
        }

        assertFalse(
            "ADR section 4.2 and section 9 (Phase 1): domain model properties must be val; " +
                "class-level var declarations are prohibited. Violations:\n" +
                violations.joinToString("\n"),
            violations.isNotEmpty(),
        )
    }

    // 5. Presentation roots must not import local persistence entities or ObjectBox.
    @Test
    fun `presentation roots do not import local persistence entities or objectbox`() {
        val presentationRoots = listOf(
            "app/src/main/java/com/mxt/anitrend/view",
            "app/src/main/java/com/mxt/anitrend/adapter",
            "app/src/main/java/com/mxt/anitrend/base/custom/view/widget",
        )
        val violations = presentationRoots.flatMap { root ->
            kotlinFiles(root).matchingTrimmedLines(presentationLeakPattern)
        }

        assertFalse(
            "ADR section 5.1 and section 9 (Phase 1): presentation must not import " +
                "com.mxt.anitrend.data.local.entity or io.objectbox. Violations:\n" +
                violations.joinToString("\n"),
            violations.isNotEmpty(),
        )
    }

    // 6a. Domain model sources must not declare mutation methods.
    @Test
    fun `domain model sources do not declare mutation methods`() {
        val violations = mutationMethodViolations(domainModelFiles(), allowBaseline = false)

        assertFalse(
            "ADR section 4.4 and section 9 (Phase 1): domain models must not declare mutation " +
                "methods (toggle, mergeFrom/merge, increment, markDeleted, copyForEditing, delete, " +
                "save). Violations:\n" + violations.joinToString("\n"),
            violations.isNotEmpty(),
        )
    }

    // 6b. Legacy entity mutation methods are confined to the reviewed baseline.
    @Test
    fun `legacy entity mutation methods are confined to the reviewed baseline`() {
        val entityFiles = kotlinFiles("app/src/main/java/com/mxt/anitrend/model/entity")
        val violations = mutationMethodViolations(entityFiles, allowBaseline = true)

        assertFalse(
            "ADR section 4.4 and section 9 (Phase 1): new model mutation methods are prohibited. " +
                "Existing legacy violations must stay listed in mutation-methods-baseline.txt and " +
                "be removed in the same migration PR. Violations:\n" + violations.joinToString("\n"),
            violations.isNotEmpty(),
        )
    }

    private fun domainModelFiles(): List<File> = kotlinFiles("app/src/main/java/com/mxt/anitrend/domain")
        .filter { it.relativeTo(projectRoot).path.contains("/model/") }

    private fun mutationMethodViolations(files: List<File>, allowBaseline: Boolean): List<String> {
        val baseline = if (allowBaseline) {
            baselineEntries("mutation-methods-baseline.txt").toSet()
        } else {
            emptySet()
        }
        val violations = mutableListOf<String>()
        files.forEach { file ->
            val path = file.relativeTo(projectRoot).path
            val lines = file.readLines()
            val scanner = BraceContextScanner(lines)
            lines.forEachIndexed { index, rawLine ->
                val stripped = stripCommentsAndStrings(rawLine)
                val match = mutationFunPattern.find(stripped)
                if (match != null && scanner.contexts[index].braceContextAtStart == BraceContext.CLASS) {
                    val key = "$path:${match.value}"
                    if (!allowBaseline || key !in baseline) {
                        violations.add("$path:${index + 1}: ${rawLine.trim()}")
                    }
                }
            }
        }
        return violations
    }

    private fun kotlinFiles(relativePath: String): List<File> = projectRoot.resolve(relativePath)
        .walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .toList()

    private fun baselineEntries(fileName: String): List<String> = architectureResources.resolve(fileName)
        .readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    private fun List<File>.matchingTrimmedLines(pattern: Regex): List<String> = flatMap { file ->
        val path = file.relativeTo(projectRoot).path
        file.readLines().mapNotNull { rawLine ->
            val trimmed = rawLine.trim()
            if (pattern.containsMatchIn(trimmed)) "$path:$trimmed" else null
        }
    }

    private fun findProjectRoot(): File {
        val userDir = requireNotNull(System.getProperty("user.dir"))
        var current = File(userDir).absoluteFile
        while (true) {
            if (current.resolve("settings.gradle.kts").exists()) {
                return current
            }
            current = current.parentFile ?: error("Unable to locate project root from $userDir")
        }
    }

    private companion object {
        val parcelablePattern = Regex("writeToParcel|describeContents|Parcelable\\.Creator|CREATOR")
        val forbiddenDomainImportPattern =
            Regex("^import\\s+(android\\.|androidx\\.|io\\.objectbox\\.|com\\.google\\.gson\\.|kotlinx\\.parcelize\\.)")
        val graphqlImportPattern = Regex("^import\\s+com\\.mxt\\.anitrend\\.graphql\\.")
        val presentationLeakPattern =
            Regex("^import\\s+(com\\.mxt\\.anitrend\\.data\\.local\\.entity|io\\.objectbox)(\\.|\\s|$)")
        val varDeclarationPattern = Regex("\\bvar\\s+[A-Za-z_][A-Za-z0-9_]*")
        val classKeywordPattern = Regex("\\b(class|interface|object)\\b")
        val mutationFunPattern = Regex(
            "\\bfun\\s+(toggle|mergeFrom|merge|increment|markDeleted|copyForEditing|delete|save)" +
                "[A-Za-z0-9_]*\\s*\\(",
        )
    }
}

private enum class BraceContext { CLASS, FUNC }

private class LineContext(
    val braceContextAtStart: BraceContext?,
    val pendingClassHeaderAtStart: Boolean,
)

/**
 * Line-based, brace-aware scanner used to distinguish class-level declarations from local
 * function/expression bodies without falsely flagging local function vars.
 *
 * Heuristics:
 * - `class`, `interface`, and `object` set a pending class header that the next `{` consumes as
 *   a CLASS body brace. Function bodies, lambdas, `when`/`if` blocks default to FUNC.
 * - A constructor-closing `)` line without a brace clears a stale pending header (class has no body).
 * - Comments and string/char literals are stripped before scanning so braces inside them are ignored.
 */
private class BraceContextScanner(lines: List<String>) {
    val contexts: MutableList<LineContext> = mutableListOf()

    private val stack = mutableListOf<BraceContext>()
    private var pendingClassHeader = false

    init {
        lines.forEach { rawLine ->
            contexts.add(LineContext(stack.lastOrNull(), pendingClassHeader))
            scanLine(stripCommentsAndStrings(rawLine))
        }
    }

    private fun scanLine(line: String) {
        if (line.trimStart().startsWith(")") && !line.contains('{')) {
            pendingClassHeader = false
        }
        var i = 0
        val n = line.length
        while (i < n) {
            when (val c = line[i]) {
                '{' -> {
                    stack.add(if (pendingClassHeader) BraceContext.CLASS else BraceContext.FUNC)
                    pendingClassHeader = false
                    i++
                }
                '}' -> {
                    if (stack.isNotEmpty()) {
                        stack.removeAt(stack.size - 1)
                    }
                    i++
                }
                else -> {
                    if (c.isLetter()) {
                        val start = i
                        while (i < n && (line[i].isLetterOrDigit() || line[i] == '_')) {
                            i++
                        }
                        val word = line.substring(start, i)
                        if (word == "class" || word == "interface" || word == "object") {
                            pendingClassHeader = true
                        }
                    } else {
                        i++
                    }
                }
            }
        }
    }
}

private fun stripCommentsAndStrings(line: String): String {
    val sb = StringBuilder()
    var i = 0
    val n = line.length
    while (i < n) {
        when (val c = line[i]) {
            '/' -> {
                if (i + 1 < n && line[i + 1] == '/') {
                    return sb.toString()
                }
                if (i + 1 < n && line[i + 1] == '*') {
                    i += 2
                    while (i < n) {
                        if (line[i] == '*' && i + 1 < n && line[i + 1] == '/') {
                            i += 2
                            break
                        }
                        i++
                    }
                } else {
                    sb.append(c)
                    i++
                }
            }
            '"' -> {
                val triple = i + 2 < n && line[i + 1] == '"' && line[i + 2] == '"'
                i = if (triple) i + 3 else i + 1
                while (i < n) {
                    if (triple) {
                        if (i + 2 < n && line[i] == '"' && line[i + 1] == '"' && line[i + 2] == '"') {
                            i += 3
                            break
                        }
                        i++
                    } else if (line[i] == '\\') {
                        i += 2
                    } else if (line[i] == '"') {
                        i++
                        break
                    } else {
                        i++
                    }
                }
            }
            '\'' -> {
                i++
                while (i < n) {
                    if (line[i] == '\\') {
                        i += 2
                    } else if (line[i] == '\'') {
                        i++
                        break
                    } else {
                        i++
                    }
                }
            }
            else -> {
                sb.append(c)
                i++
            }
        }
    }
    return sb.toString()
}
