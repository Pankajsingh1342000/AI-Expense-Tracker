package com.example.aiexpensetracker.ai.speech

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class VoiceRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: ((String) -> Unit)? = null
    private var errorListener: ((String) -> Unit)? = null
    private var listeningStateListener: ((Boolean) -> Unit)? = null
    private var isListening = false

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onListeningStateChange: (Boolean) -> Unit
    ) {
        Timber.d("Starting voice recognition...")

        // Stop any existing recognition first
        stopListening()

        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            val error = "Speech recognition not available on this device"
            Timber.e(error)
            onError(error)
            return
        }

        // Check if Google app is available (often required for speech recognition)
        if (!isGoogleAppInstalled()) {
            val error = "Google app required for speech recognition. Please install Google app from Play Store."
            Timber.e(error)
            onError(error)
            return
        }

        recognitionListener = onResult
        errorListener = onError
        listeningStateListener = onListeningStateChange

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

            if (speechRecognizer == null) {
                val error = "Failed to create speech recognizer"
                Timber.e(error)
                onError(error)
                return
            }

            speechRecognizer?.setRecognitionListener(createRecognitionListener())

            val intent = createRecognitionIntent()

            Timber.d("Starting speech recognition with intent")

            isListening = true
            speechRecognizer?.startListening(intent)

        } catch (e: SecurityException) {
            Timber.e(e, "Security exception - permission issue")
            onError("Microphone permission required. Please grant permission and try again.")
            cleanup()
        } catch (e: Exception) {
            Timber.e(e, "Error starting speech recognition")
            onError("Failed to start speech recognition: ${e.message}")
            cleanup()
        }
    }

    private fun createRecognitionIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // Use US English for better compatibility
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false) // Use online recognition for reliability
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")

            // Add speech timeout settings
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }
    }

    private fun isGoogleAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.googlequicksearchbox", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun stopListening() {
        Timber.d("Stopping voice recognition...")
        try {
            isListening = false
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Timber.e(e, "Error stopping speech recognition")
        } finally {
            // Don't cleanup immediately, let the callback handle it
        }
    }

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context) && isGoogleAppInstalled()
    }

    fun isCurrentlyListening(): Boolean = isListening

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Timber.d("Speech recognition ready")
            listeningStateListener?.invoke(true)
        }

        override fun onBeginningOfSpeech() {
            Timber.d("Speech input started")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Volume level for visual feedback if needed
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Timber.d("Buffer received: ${buffer?.size} bytes")
        }

        override fun onEndOfSpeech() {
            Timber.d("Speech input ended")
            isListening = false
            listeningStateListener?.invoke(false)
        }

        override fun onError(error: Int) {
            Timber.e("Speech recognition error code: $error")
            isListening = false
            listeningStateListener?.invoke(false)

            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check if another app is using the microphone."
                SpeechRecognizer.ERROR_CLIENT -> "Client error occurred."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied. Please grant permission in Settings."
                SpeechRecognizer.ERROR_NETWORK -> "Network error. Please check your internet connection."
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout. Please try again."
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly and try again."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Please wait a moment and try again."
                SpeechRecognizer.ERROR_SERVER -> "Server error. Please try again later."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Please speak after tapping the microphone."
                else -> "Speech recognition error (Code: $error). Please try again."
            }

            Timber.e("Speech error: $errorMessage")
            errorListener?.invoke(errorMessage)

            // Cleanup after error
            cleanup()
        }

        override fun onResults(results: Bundle?) {
            Timber.d("Speech recognition completed with results")
            isListening = false
            listeningStateListener?.invoke(false)

            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

            Timber.d("Recognition results: $matches")
            Timber.d("Confidence scores: ${confidences?.toList()}")

            if (!matches.isNullOrEmpty()) {
                val bestMatch = matches[0]
                Timber.d("Best speech result: '$bestMatch'")
                recognitionListener?.invoke(bestMatch)
            } else {
                errorListener?.invoke("No speech detected. Please try speaking again.")
            }

            cleanup()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                Timber.d("Partial result: ${matches[0]}")
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Timber.d("Speech recognition event: $eventType")
        }
    }

    private fun cleanup() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Timber.e(e, "Error destroying speech recognizer")
        } finally {
            speechRecognizer = null
            recognitionListener = null
            errorListener = null
            listeningStateListener = null
            isListening = false
        }
    }
}