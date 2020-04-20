package com.example.dictation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import com.example.dictation.SpeechRecognizerViewModel.ViewState

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizerViewModel: SpeechRecognizerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dictateBtn.setOnClickListener {
            view -> onDictate()
        }
        setUpSpeechViewModel()
    }

    private fun onDictate() {
        if (speechRecognizerViewModel.isListening) {
            speechRecognizerViewModel.stopListening()
        } else {
            speechRecognizerViewModel.startListening()
        }
    }

    private fun setUpSpeechViewModel() {
        speechRecognizerViewModel =
            ViewModelProviders.of(this).get(SpeechRecognizerViewModel::class.java)
        speechRecognizerViewModel.getViewState().observe(this, Observer<ViewState> { viewState ->
            render(viewState)
        })
    }

    private fun render(uiOutput: ViewState?) {
        if (uiOutput == null) return

        outputTxt.text = uiOutput.spokenText
    }

}
