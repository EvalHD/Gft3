package de.lokalki.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var chatLog: TextView
    private lateinit var chatScroll: ScrollView
    private lateinit var input: EditText
    @Volatile private var llm: LocalLlm? = null
    private val history = java.util.Collections.synchronizedList(mutableListOf<String>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chatLog = findViewById(R.id.chatLog)
        chatScroll = findViewById(R.id.chatScroll)
        input = findViewById(R.id.input)
        findViewById<Button>(R.id.sendButton).setOnClickListener { onSend() }

        if (!Environment.isExternalStorageManager()) {
            startActivity(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName"))
            )
            append("KI", "Bitte erlaube 'Zugriff auf alle Dateien', damit ich Dateien finden kann.")
        } else {
            append("KI", "Hallo! Schreib mir einfach. Ich kann Dateien suchen, Speicher und Akku prüfen und deine Apps zeigen.")
        }

        Thread {
            llm = LocalLlm.tryLoad(this)
            runOnUiThread {
                if (llm != null) append("KI", "Sprachmodell geladen, jetzt können wir uns frei unterhalten.")
            }
        }.start()
    }

    private fun onSend() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        append("Du", text)

        Thread {
            val answer = answerTo(text)
            synchronized(history) {
                history.add("Du: $text")
                history.add("KI: $answer")
                while (history.size > 12) history.removeAt(0)
            }
            runOnUiThread { append("KI", answer) }
        }.start()
    }

    private fun answerTo(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.startsWith("wo ") || lower.contains("finde") ||
                lower.contains("suche") || lower.contains("datei") -> FileSearch.answer(text)
            lower.contains("speicher") || lower.contains("akku") -> DeviceInfo.status(this)
            lower.contains("apps") -> DeviceInfo.apps(this)
            else -> llm?.ask(buildPrompt(text)) ?: Smalltalk.reply(text)
        }
    }

    private fun buildPrompt(text: String): String {
        val context = synchronized(history) { history.joinToString("\n") }
        return "Du bist LokalKI, ein freundlicher Assistent auf dem Handy. " +
            "Antworte auf Deutsch, kurz und hilfreich.\n" +
            "$context\nDu: $text\nKI:"
    }

    private fun append(who: String, msg: String) {
        chatLog.append("\n$who: $msg\n")
        chatScroll.post { chatScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
