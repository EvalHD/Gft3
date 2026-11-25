package de.lokalki.app

import android.os.Environment
import java.io.File

object FileSearch {

    fun answer(query: String): String {
        val terms = query.lowercase()
            .replace(Regex("\\b(wo|ist|finde|suche|nach|die|der|das|datei|mir|bitte|mal)\\b"), " ")
            .replace(Regex("[?!]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }

        if (terms.isEmpty()) return "Wonach soll ich suchen? Nenne einen Dateinamen oder ein Wort."

        val root = Environment.getExternalStorageDirectory()
        val hits: List<File> = root.walkTopDown()
            .onEnter { !it.name.startsWith(".") && it.name != "Android" }
            .filter { it.isFile }
            .filter { f -> terms.all { f.name.lowercase().contains(it) } }
            .take(20)
            .toList()

        if (hits.isEmpty()) {
            return "Ich habe keine Datei mit '${terms.joinToString(" ")}' im Gerätespeicher gefunden."
        }
        return hits.joinToString("\n") { "📄 ${it.name}\n    Ordner: ${it.parent}" }
    }
}
