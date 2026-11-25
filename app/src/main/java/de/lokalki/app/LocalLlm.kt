package de.lokalki.app

import android.content.Context
import android.os.Environment
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class LocalLlm private constructor(private val engine: LlmInference) {

    fun ask(prompt: String): String = try {
        engine.generateResponse(prompt)
    } catch (e: Exception) {
        "Fehler im Modell: ${e.message}"
    }

    companion object {
        fun tryLoad(ctx: Context): LocalLlm? {
            val file = File(Environment.getExternalStorageDirectory(), "Download/lokalki-model.task")
            if (!file.exists()) return null
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(file.absolutePath)
                .setMaxTokens(512)
                .build()
            return LocalLlm(LlmInference.createFromOptions(ctx, options))
        }
    }
}
