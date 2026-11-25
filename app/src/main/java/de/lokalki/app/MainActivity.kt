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
            append("KI", "Hallo! Frag mich z. B. 'wo ist rechnung.pdf' oder 'wie voll ist mein Speicher'.")
        }

        Thread {
            llm = LocalLlm.tryLoad(this)
            runOnUiThread {
                if (llm != null) append("KI", "Sprachmodell geladen, du kannst frei chatten.")
            }
        }.start()
    }

    private fun onSend() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        append("Du", text)

        Thread {
            val lower = text.lowercase()
            val answer = when {
                lower.startsWith("wo") || lower.contains("finde") || lower.contains("suche") ->
                    FileSearch.answer(text)
                lower.contains("speicher") || lower.contains("akku") ->
                    DeviceInfo.status(this)
                lower.contains("apps") ->
                    DeviceInfo.apps(this)
                else -> llm?.ask(text)
                    ?: "Kein Sprachmodell geladen. Lege 'lokalki-model.task' in den Download-Ordner, dann kann ich frei antworten. Dateisuche und Gerätestatus funktionieren schon."
            }
            runOnUiThread { append("KI", answer) }
        }.start()
    }

    private fun append(who: String, msg: String) {
        chatLog.append("\n$who: $msg\n")
        chatScroll.post { chatScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
