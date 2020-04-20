package com.example.dictation

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.RESULTS_RECOGNITION
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SpeechRecognizerViewModel(application: Application) : AndroidViewModel(application), RecognitionListener {

    data class ViewState (
        val spokenText: String,
        val isListening: Boolean,
        val error: String?
    )

    private var viewState: MutableLiveData<ViewState>? = null

    var permissionToRecordAudio = checkAudioRecordingPermission(context = application)

    private fun checkAudioRecordingPermission(context: Application) =
        ContextCompat.checkSelfPermission(context,
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(application.applicationContext).apply {
        setRecognitionListener(this@SpeechRecognizerViewModel)
    }

    var isListening = false
        get() = viewState?.value?.isListening ?: false

    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, application.packageName)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private fun initViewState() = ViewState(spokenText = "", isListening = false, error = null)

    fun getViewState(): LiveData<ViewState> {
        if (viewState == null) {
            viewState = MutableLiveData()
            viewState?.value = initViewState()
        }

        return viewState as MutableLiveData<ViewState>
    }

    private fun notifyListening(isRecording: Boolean) {
        viewState?.value = viewState?.value?.copy(isListening = isRecording)
    }

    fun startListening() {
        speechRecognizer.startListening(recognizerIntent)
        notifyListening(isRecording = true)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        notifyListening(isRecording = false)
    }

    private fun updateResults(speechBundle: Bundle?) {
        val userSaid = speechBundle?.getStringArrayList(RESULTS_RECOGNITION)
        viewState?.value = viewState?.value?.copy(spokenText = userSaid?.get(0) ?: "")
    }

    override fun onReadyForSpeech(p0: Bundle?) {}

    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onPartialResults(results: Bundle?) = updateResults(speechBundle = results)

    override fun onEvent(p0: Int, p1: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onEndOfSpeech() = notifyListening(isRecording = false)

    override fun onError(p0: Int) {}

    override fun onResults(results: Bundle?) = updateResults(speechBundle = results)
}