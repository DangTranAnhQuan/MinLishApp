package com.project.minlishapp.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.abs
import javax.inject.Inject
import javax.inject.Singleton

data class CardAutoFillResult(
    val pronunciation: String = "",
    val meaning: String = "",
    val definition: String = "",
    val example: String = "",
    val collocation: String = "",
    val relatedWords: String = "",
    val tags: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val audioUrlUs: String = "",
    val audioUrlUk: String = ""
)

@Singleton
class CardAutoFillService @Inject constructor() {

    suspend fun lookup(word: String): CardAutoFillResult = withContext(Dispatchers.IO) {
        val normalizedWord = word.trim()
        if (normalizedWord.isBlank()) return@withContext CardAutoFillResult()

        val dictionaryResult = runCatching { lookupDictionary(normalizedWord) }
            .getOrDefault(CardAutoFillResult())
        val enrichedResult = runCatching {
            enrichWithDatamuse(normalizedWord, dictionaryResult)
        }.getOrDefault(dictionaryResult)
        val vietnameseMeaning = runCatching {
            translateVocabularyMeaning(
                word = normalizedWord,
                tags = enrichedResult.tags
            )
        }.getOrDefault("")
        val imageUrl = runCatching { lookupImage(normalizedWord) }
            .getOrDefault("")
        val example = enrichedResult.example.ifBlank {
            runCatching { lookupTatoebaExample(normalizedWord) }.getOrDefault("")
        }.ifBlank {
            fallbackExample(
                word = normalizedWord,
                tags = enrichedResult.tags
            )
        }

        enrichedResult.copy(
            meaning = vietnameseMeaning.ifBlank { enrichedResult.meaning },
            example = example,
            imageUrl = imageUrl
        )
    }

    private fun lookupDictionary(word: String): CardAutoFillResult {
        val encodedWord = URLEncoder.encode(word, "UTF-8")
        val response = getJson("https://api.dictionaryapi.dev/api/v2/entries/en/$encodedWord")
        val entries = JSONArray(response)
        if (entries.length() == 0) return CardAutoFillResult()

        val phonetics = allArrays(entries, "phonetics")
        val pronunciation = firstEntryValue(entries, "phonetic")
            .ifBlank { firstNonBlank(phonetics, "text") }
        val audioUrls = audioUrlsByAccent(phonetics)
        val audioUrlUs = audioUrls.us
        val audioUrlUk = audioUrls.uk
        val audioUrl = audioUrlUs.ifBlank { audioUrlUk }.ifBlank {
            firstNonBlank(phonetics, "audio").normalizeProtocolRelativeUrl()
        }

        val meanings = allArrays(entries, "meanings")
        val firstDefinition = firstDefinition(meanings)
        val example = firstExample(meanings)
        val synonyms = firstSynonyms(meanings)
        val tags = partOfSpeechTags(meanings)

        return CardAutoFillResult(
            pronunciation = pronunciation,
            meaning = firstDefinition,
            definition = firstDefinition,
            example = example,
            relatedWords = synonyms,
            tags = tags,
            audioUrl = audioUrl,
            audioUrlUs = audioUrlUs,
            audioUrlUk = audioUrlUk
        )
    }

    private fun enrichWithDatamuse(
        word: String,
        baseResult: CardAutoFillResult
    ): CardAutoFillResult {
        val synonyms = datamuseWords("https://api.datamuse.com/words?rel_syn=${word.urlEncode()}&max=8")
        val similarWords = datamuseWords("https://api.datamuse.com/words?ml=${word.urlEncode()}&max=8")
        val triggerWords = datamuseWords("https://api.datamuse.com/words?rel_trg=${word.urlEncode()}&max=10")
        val datamuseTags = datamuseTags(word)
        val allTags = mergeCommaValues(baseResult.tags, datamuseTags)

        return baseResult.copy(
            relatedWords = mergeCommaValues(
                baseResult.relatedWords,
                (synonyms + similarWords).joinToString(", ")
            ),
            collocation = baseResult.collocation.ifBlank {
                buildCollocations(
                    word = word,
                    triggerWords = triggerWords,
                    tags = allTags
                )
            },
            tags = allTags
        )
    }

    private fun translateToVietnamese(text: String): String {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return ""
        val url = "https://api.mymemory.translated.net/get" +
            "?q=${trimmedText.urlEncode()}" +
            "&langpair=en%7Cvi"
        return JSONObject(getJson(url))
            .optJSONObject("responseData")
            ?.optString("translatedText")
            .orEmpty()
            .cleanHtmlEntities()
    }

    private fun translateVocabularyMeaning(
        word: String,
        tags: String
    ): String {
        val candidates = if (tags.contains("noun", ignoreCase = true)) {
            listOf("${articleFor(word)} $word", word)
        } else {
            listOf(word)
        }

        for (candidate in candidates) {
            val translated = translateToVietnamese(candidate)
                .toVocabularyMeaning()
            if (translated.isUsefulMeaningFor(word)) {
                return translated
            }
        }
        return ""
    }

    private fun articleFor(word: String): String {
        return if (word.firstOrNull()?.lowercaseChar() in VOWELS) "an" else "a"
    }

    private fun lookupImage(word: String): String {
        return lookupWikipediaThumbnail(word).ifBlank {
            lookupWikimediaImage(word)
        }
    }

    private fun lookupTatoebaExample(word: String): String {
        val url = "https://tatoeba.org/en/api_v0/search" +
            "?from=eng" +
            "&query=${word.urlEncode()}" +
            "&sort=relevance" +
            "&orphans=no"

        val results = JSONObject(getJson(url)).optJSONArray("results") ?: return ""
        return List(results.length()) { index ->
            results.optJSONObject(index)
                ?.optString("text")
                .orEmpty()
                .cleanHtmlEntities()
                .normalizeExampleText()
        }
            .filter { it.isUsefulExampleFor(word) }
            .maxByOrNull { it.scoreAsExampleFor(word) }
            .orEmpty()
    }

    private fun lookupWikipediaThumbnail(word: String): String {
        val encodedQuery = word.urlEncode()
        val url = "https://en.wikipedia.org/w/api.php" +
            "?action=query" +
            "&format=json" +
            "&generator=search" +
            "&gsrsearch=$encodedQuery" +
            "&gsrlimit=8" +
            "&prop=pageimages%7Cpageterms" +
            "&piprop=thumbnail" +
            "&pithumbsize=640" +
            "&pilimit=8" +
            "&wbptterms=description" +
            "&redirects=1"

        val json = JSONObject(getJson(url))
        val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return ""
        val candidates = mutableListOf<WikipediaImageCandidate>()
        val keys = pages.keys()
        while (keys.hasNext()) {
            val page = pages.optJSONObject(keys.next()) ?: continue
            val source = page.optJSONObject("thumbnail")?.optString("source").orEmpty()
            if (source.isBlank()) continue
            val description = page.optJSONObject("terms")
                ?.optJSONArray("description")
                ?.optString(0)
                .orEmpty()
            candidates += WikipediaImageCandidate(
                title = page.optString("title"),
                description = description,
                imageUrl = source.normalizeProtocolRelativeUrl()
            )
        }
        return candidates
            .maxByOrNull { it.scoreFor(word) }
            ?.takeIf { it.scoreFor(word) > MIN_IMAGE_SCORE }
            ?.imageUrl
            .orEmpty()
    }

    private fun lookupWikimediaImage(word: String): String {
        val encodedQuery = word.urlEncode()
        val url = "https://commons.wikimedia.org/w/api.php" +
            "?action=query" +
            "&generator=search" +
            "&gsrnamespace=6" +
            "&gsrlimit=8" +
            "&gsrsearch=$encodedQuery" +
            "&prop=imageinfo" +
            "&iiprop=url%7Cmime" +
            "&iiurlwidth=640" +
            "&format=json"

        val json = JSONObject(getJson(url))
        val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return ""
        val candidates = mutableListOf<WikimediaImageCandidate>()
        val keys = pages.keys()
        while (keys.hasNext()) {
            val page = pages.optJSONObject(keys.next()) ?: continue
            val imageInfo = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
            val mime = imageInfo.optString("mime")
            val imageUrl = imageInfo.optString("thumburl")
                .ifBlank { imageInfo.optString("url") }
            if (imageUrl.isNotBlank() && mime.startsWith("image/")) {
                candidates += WikimediaImageCandidate(
                    title = page.optString("title"),
                    imageUrl = imageUrl.normalizeProtocolRelativeUrl()
                )
            }
        }
        return candidates
            .maxByOrNull { it.scoreFor(word) }
            ?.imageUrl
            .orEmpty()
    }

    private fun getJson(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "MinLishApp/1.0")
        }
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun datamuseWords(url: String): List<String> {
        val response = JSONArray(getJson(url))
        return List(response.length()) { index ->
            response.optJSONObject(index)?.optString("word").orEmpty()
        }
            .map(String::trim)
            .filter(String::isNotBlank)
    }

    private fun datamuseTags(word: String): String {
        val response = JSONArray(
            getJson("https://api.datamuse.com/words?sp=${word.urlEncode()}&md=p&max=1")
        )
        val tags = response.optJSONObject(0)
            ?.optJSONArray("tags")
            .toStringList()
            .mapNotNull(::mapDatamuseTag)
        return tags.distinct().joinToString(", ")
    }

    private fun mapDatamuseTag(tag: String): String? {
        return when (tag) {
            "n" -> "noun"
            "v" -> "verb"
            "adj" -> "adjective"
            "adv" -> "adverb"
            "u" -> null
            else -> null
        }
    }

    private fun firstNonBlank(array: JSONArray, fieldName: String): String {
        for (index in 0 until array.length()) {
            val value = array.optJSONObject(index)?.optString(fieldName).orEmpty()
            if (value.isNotBlank()) return value
        }
        return ""
    }

    private fun audioUrlsByAccent(phonetics: JSONArray): AccentAudioUrls {
        var us = ""
        var uk = ""
        for (index in 0 until phonetics.length()) {
            val audio = phonetics.optJSONObject(index)
                ?.optString("audio")
                .orEmpty()
                .normalizeProtocolRelativeUrl()
            if (audio.isBlank()) continue
            val normalizedAudio = audio.lowercase()
            when {
                us.isBlank() && (normalizedAudio.contains("-us.") || normalizedAudio.contains("_us.")) -> {
                    us = audio
                }
                uk.isBlank() && (normalizedAudio.contains("-uk.") || normalizedAudio.contains("_uk.")) -> {
                    uk = audio
                }
            }
        }
        return AccentAudioUrls(us = us, uk = uk)
    }

    private fun firstEntryValue(entries: JSONArray, fieldName: String): String {
        for (index in 0 until entries.length()) {
            val value = entries.optJSONObject(index)?.optString(fieldName).orEmpty()
            if (value.isNotBlank()) return value
        }
        return ""
    }

    private fun allArrays(entries: JSONArray, fieldName: String): JSONArray {
        val result = JSONArray()
        for (entryIndex in 0 until entries.length()) {
            val values = entries.optJSONObject(entryIndex)?.optJSONArray(fieldName) ?: continue
            for (valueIndex in 0 until values.length()) {
                result.put(values.get(valueIndex))
            }
        }
        return result
    }

    private fun firstDefinition(meanings: JSONArray): String {
        for (meaningIndex in 0 until meanings.length()) {
            val definitions = meanings.optJSONObject(meaningIndex)
                ?.optJSONArray("definitions")
                ?: continue
            for (definitionIndex in 0 until definitions.length()) {
                val definition = definitions.optJSONObject(definitionIndex)
                    ?.optString("definition")
                    .orEmpty()
                if (definition.isNotBlank()) return definition
            }
        }
        return ""
    }

    private fun firstExample(meanings: JSONArray): String {
        for (meaningIndex in 0 until meanings.length()) {
            val definitions = meanings.optJSONObject(meaningIndex)
                ?.optJSONArray("definitions")
                ?: continue
            for (definitionIndex in 0 until definitions.length()) {
                val example = definitions.optJSONObject(definitionIndex)
                    ?.optString("example")
                    .orEmpty()
                if (example.isNotBlank()) return example
            }
        }
        return ""
    }

    private fun firstSynonyms(meanings: JSONArray): String {
        val synonyms = mutableListOf<String>()
        for (meaningIndex in 0 until meanings.length()) {
            val meaning = meanings.optJSONObject(meaningIndex) ?: continue
            synonyms += meaning.optJSONArray("synonyms").toStringList()

            val definitions = meaning.optJSONArray("definitions") ?: continue
            for (definitionIndex in 0 until definitions.length()) {
                synonyms += definitions.optJSONObject(definitionIndex)
                    ?.optJSONArray("synonyms")
                    .toStringList()
            }
        }
        return synonyms
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .take(MAX_RELATED_WORDS)
            .joinToString(", ")
    }

    private fun partOfSpeechTags(meanings: JSONArray): String {
        val tags = mutableListOf<String>()
        for (index in 0 until meanings.length()) {
            val partOfSpeech = meanings.optJSONObject(index)
                ?.optString("partOfSpeech")
                .orEmpty()
            if (partOfSpeech.isNotBlank()) tags += partOfSpeech
        }
        return tags.distinct().joinToString(", ")
    }

    private fun fallbackExample(
        word: String,
        tags: String
    ): String {
        val normalizedWord = word.trim()
        if (normalizedWord.isBlank()) return ""

        return when {
            tags.contains("verb", ignoreCase = true) -> {
                "I try to $normalizedWord every day."
            }
            tags.contains("adjective", ignoreCase = true) -> {
                "The $normalizedWord answer was easy to remember."
            }
            tags.contains("adverb", ignoreCase = true) -> {
                "She answered $normalizedWord during practice."
            }
            else -> {
                val nounPhrase = if (
                    normalizedWord.contains(" ") ||
                    normalizedWord.endsWith("s", ignoreCase = true)
                ) {
                    normalizedWord
                } else {
                    "${articleFor(normalizedWord)} $normalizedWord"
                }
                "I saw $nounPhrase in the picture."
            }
        }
    }

    private fun buildCollocations(
        word: String,
        triggerWords: List<String>,
        tags: String
    ): String {
        val normalizedWord = word.trim()
        if (normalizedWord.isBlank()) return ""
        val usefulTriggers = triggerWords
            .map(String::trim)
            .filter { it.isNotBlank() && !it.equals(normalizedWord, ignoreCase = true) }
            .distinct()
            .take(MAX_COLLOCATION_WORDS)

        val isNoun = tags.contains("noun", ignoreCase = true)
        val isAdjective = tags.contains("adjective", ignoreCase = true)
        val phrases = usefulTriggers.map { trigger ->
            when {
                isAdjective -> "$normalizedWord $trigger"
                isNoun -> {
                    if (NOUN_SUFFIX_TRIGGERS.any { suffix -> trigger.contains(suffix, ignoreCase = true) }) {
                        "$normalizedWord $trigger"
                    } else {
                        "$trigger $normalizedWord"
                    }
                }
                else -> "$normalizedWord $trigger"
            }
        }
        return phrases.joinToString(", ")
    }

    private fun mergeCommaValues(first: String, second: String): String {
        return (first.split(",") + second.split(","))
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .take(MAX_RELATED_WORDS)
            .joinToString(", ")
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return List(length()) { index -> optString(index) }
    }

    private fun String.normalizeProtocolRelativeUrl(): String {
        return if (startsWith("//")) "https:$this" else this
    }

    private fun String.urlEncode(): String {
        return URLEncoder.encode(this, "UTF-8")
    }

    private fun String.cleanHtmlEntities(): String {
        val namedEntitiesCleaned = replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
        return NUMERIC_HTML_ENTITY_REGEX.replace(namedEntitiesCleaned) { match ->
            val value = match.groupValues[1]
            val codePoint = if (value.startsWith("x", ignoreCase = true)) {
                value.drop(1).toIntOrNull(16)
            } else {
                value.toIntOrNull()
            }
            codePoint?.let { String(Character.toChars(it)) } ?: match.value
        }
    }

    private fun String.normalizeExampleText(): String {
        return replace(WHITESPACE_REGEX, " ").trim()
    }

    private fun String.toVocabularyMeaning(): String {
        return trim()
            .removePrefix("một ")
            .removePrefix("Một ")
            .removePrefix("1 ")
            .substringBefore(";")
            .trim()
            .trimEnd('.', ',', ';')
    }

    private fun String.isUsefulMeaningFor(word: String): Boolean {
        val normalized = trim()
        return normalized.isNotBlank() &&
            !normalized.equals(word, ignoreCase = true) &&
            normalized.length <= MAX_MEANING_LENGTH
    }

    private fun String.isUsefulExampleFor(word: String): Boolean {
        val normalized = trim()
        return normalized.length in MIN_EXAMPLE_LENGTH..MAX_EXAMPLE_LENGTH &&
            !normalized.equals(word, ignoreCase = true) &&
            normalized.containsVocabularyTerm(word) &&
            !UNHELPFUL_EXAMPLE_MARKERS.any { marker ->
                normalized.contains(marker, ignoreCase = true)
            }
    }

    private fun String.scoreAsExampleFor(word: String): Int {
        val targetForms = word.targetWordForms()
        val lowerText = lowercase()
        val exactScore = if (targetForms.firstOrNull()?.let { target ->
                lowerText.containsWordForm(target)
            } == true
        ) {
            120
        } else {
            80
        }
        val lengthScore = 50 - abs(length - IDEAL_EXAMPLE_LENGTH).coerceAtMost(50)
        val naturalSentenceBonus = if (lastOrNull() in NATURAL_SENTENCE_ENDINGS) 15 else 0
        return exactScore + lengthScore + naturalSentenceBonus
    }

    private fun String.containsVocabularyTerm(word: String): Boolean {
        return word.targetWordForms().any { form -> lowercase().containsWordForm(form) }
    }

    private fun String.containsWordForm(wordForm: String): Boolean {
        val pattern = Regex("(^|[^A-Za-z])${Regex.escape(wordForm)}([^A-Za-z]|$)")
        return pattern.containsMatchIn(this)
    }

    private fun String.targetWordForms(): List<String> {
        val normalized = trim().lowercase()
        if (normalized.isBlank()) return emptyList()
        val forms = mutableListOf(normalized)
        if (!normalized.contains(" ")) {
            forms += when {
                normalized.endsWith("y") && normalized.length > 1 &&
                    normalized[normalized.length - 2] !in VOWELS -> {
                    normalized.dropLast(1) + "ies"
                }
                PLURAL_ES_SUFFIXES.any { suffix -> normalized.endsWith(suffix) } -> {
                    normalized + "es"
                }
                else -> normalized + "s"
            }
            forms += if (normalized.endsWith("e")) {
                normalized.dropLast(1) + "ing"
            } else {
                normalized + "ing"
            }
            forms += if (normalized.endsWith("e")) normalized + "d" else normalized + "ed"
        }
        return forms.distinct()
    }

    private data class WikipediaImageCandidate(
        val title: String,
        val description: String,
        val imageUrl: String
    ) {
        fun scoreFor(word: String): Int {
            val normalizedTitle = title.lowercase()
            val normalizedDescription = description.lowercase()
            val normalizedWord = word.lowercase()
            val avoidPenalty = AVOID_IMAGE_TERMS.count { term ->
                normalizedTitle.contains(term) || normalizedDescription.contains(term.trim())
            } * 25
            val matchScore = when {
                normalizedTitle == normalizedWord -> 160
                normalizedTitle.startsWith("$normalizedWord ") -> 110
                normalizedTitle.contains(normalizedWord) -> 80
                normalizedDescription.contains(normalizedWord) -> 50
                else -> 0
            }
            return matchScore - avoidPenalty
        }
    }

    private data class AccentAudioUrls(
        val us: String = "",
        val uk: String = ""
    )

    private data class WikimediaImageCandidate(
        val title: String,
        val imageUrl: String
    ) {
        fun scoreFor(word: String): Int {
            val normalizedTitle = title
                .removePrefix("File:")
                .substringBeforeLast(".")
                .replace('_', ' ')
                .lowercase()
            val normalizedWord = word.lowercase()
            val avoidPenalty = AVOID_IMAGE_TERMS.count { term ->
                normalizedTitle.contains(term)
            } * 30
            val matchScore = when {
                normalizedTitle == normalizedWord -> 120
                normalizedTitle.startsWith("$normalizedWord ") -> 90
                normalizedTitle.endsWith(" $normalizedWord") -> 75
                normalizedTitle.contains(" $normalizedWord ") -> 65
                normalizedTitle.contains(normalizedWord) -> 40
                else -> 0
            }
            return matchScore - avoidPenalty
        }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000
        const val MAX_RELATED_WORDS = 8
        const val MAX_COLLOCATION_WORDS = 6
        const val MAX_MEANING_LENGTH = 80
        const val MIN_EXAMPLE_LENGTH = 8
        const val MAX_EXAMPLE_LENGTH = 160
        const val IDEAL_EXAMPLE_LENGTH = 55
        const val MIN_IMAGE_SCORE = 30
        val VOWELS = setOf('a', 'e', 'i', 'o', 'u')
        val NUMERIC_HTML_ENTITY_REGEX = Regex("&#(x?[0-9A-Fa-f]+);")
        val WHITESPACE_REGEX = Regex("\\s+")
        val PLURAL_ES_SUFFIXES = listOf("s", "x", "z", "ch", "sh", "o")
        val NATURAL_SENTENCE_ENDINGS = setOf('.', '!', '?')
        val UNHELPFUL_EXAMPLE_MARKERS = listOf(
            "this sentence uses",
            "in context",
            "example sentence"
        )
        val NOUN_SUFFIX_TRIGGERS = listOf(
            "pie",
            "tree",
            "juice",
            "fruit",
            "cake",
            "market",
            "seed"
        )
        val AVOID_IMAGE_TERMS = listOf(
            " logo",
            " icon",
            " screenshot",
            " diagram",
            " map",
            " flag",
            " symbol",
            " apple ii"
        )
    }
}
