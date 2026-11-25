package de.lokalki.app

import android.os.Environment
import java.io.File

object FileSearch {

    fun answer(query: String): String {
        val terms = normalize(query)
            .replace(Regex("\\b(wo|ist|finde|suche|nach|die|der|das|datei|mir|bitte|mal|ein|eine)\\b"), " ")
            .replace(Regex("[?!.,]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }

        if (terms.isEmpty()) return "Wonach soll ich suchen? Nenne einen Dateinamen oder ein Wort."

        val root = Environment.getExternalStorageDirectory()
        val scored = root.walkTopDown()
            .onEnter { !it.name.startsWith(".") && it.name != "Android" }
            .filter { it.isFile }
            .mapNotNull { file ->
                val name = normalize(file.name)
                val scores = terms.map { termScore(it, name) }
                if (scores.any { it == 0 }) null else file to scores.sum()
            }
            .sortedByDescending { it.second }
            .take(20)
            .toList()

        if (scored.isEmpty()) {
            return "Ich habe nichts gefunden, das zu '${terms.joinToString(" ")}' passt."
        }
        return scored.joinToString("\n") { (file, _) ->
            "📄 ${file.name}\n    Ordner: ${file.parent}"
        }
    }

    // Groß/Klein, Umlaute und Sonderzeichen egal machen
    private fun normalize(s: String): String = s.lowercase()
        .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")

    // 0 = kein Treffer, 1 = ähnlicher Treffer (Tippfehler), 2 = genauer Treffer
    private fun termScore(term: String, name: String): Int {
        if (name.contains(term)) return 2
        val tokens = name.split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
        val allowed = when {
            term.length <= 3 -> 0
            term.length <= 6 -> 1
            else -> 2
        }
        val best = tokens.minOfOrNull { levenshtein(term, it) } ?: Int.MAX_VALUE
        return if (best <= allowed) 1 else 0
    }

    // Anzahl Buchstaben-Änderungen, die nötig sind, um a in b zu verwandeln
    private fun levenshtein(a: String, b: String): Int {
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val tmp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + cost)
                prev = tmp
            }
        }
        return dp[b.length]
    }
}
