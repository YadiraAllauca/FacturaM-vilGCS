package dev.android.appfacturador.utils
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.EditText
import androidx.core.app.ActivityCompat.startActivityForResult

object SpeechToTextUtil {
    fun startSpeechToText(activity: Activity, requestCode: Int) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")

        activity.startActivityForResult(intent, requestCode)
    }
}



