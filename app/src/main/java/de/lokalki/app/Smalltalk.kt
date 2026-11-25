package de.lokalki.app

object Smalltalk {

    fun reply(text: String): String {
        val t = text.lowercase()
        return when {
            listOf("hallo", "hi", "hey", "moin", "servus").any { t.startsWith(it) } ->
                "Hallo! Schön, dass du da bist. Wie kann ich dir helfen?"
            t.contains("wie geht") ->
                "Mir geht's gut, danke der Nachfrage. Und dir?"
            t.contains("danke") ->
                "Gern geschehen!"
            t.contains("tschüss") || t.contains("bye") ->
                "Bis später!"
            t.contains("wer bist") || t.contains("was kannst") ->
                "Ich bin LokalKI und laufe komplett auf deinem Handy. Ich kann Dateien suchen (z. B. „wo ist rechnung.pdf“), Speicher und Akku prüfen und deine Apps zeigen. Für freie Gespräche brauche ich noch das Sprachmodell."
            else ->
                "Für freie Gespräche brauche ich noch das Sprachmodell (lokalki-model.task im Download-Ordner). Bis dahin kann ich Dateien suchen, Speicher/Akku prüfen und Apps zeigen."
        }
    }
}
