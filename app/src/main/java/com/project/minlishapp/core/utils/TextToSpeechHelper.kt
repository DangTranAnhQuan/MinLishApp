package com.project.minlishapp.core.utils

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class PronunciationAccent(
    val label: String,
    val locale: Locale
) {
    US("US", Locale.US),
    UK("UK", Locale.UK)
}

class TextToSpeechHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var playbackRequestId = 0

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                } else {
                    configureVoice()
                    isInitialized = true
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }

    fun speak(
        text: String,
        audioUrl: String = "",
        accent: PronunciationAccent = PronunciationAccent.US
    ) {
        val requestId = ++playbackRequestId
        stopAudio()
        if (audioUrl.isNotBlank()) {
            playAudioUrl(
                audioUrl = audioUrl,
                fallbackText = text,
                fallbackAccent = accent,
                requestId = requestId
            )
            return
        }
        speakWithTts(text, accent)
    }

    private fun speakWithTts(
        text: String,
        accent: PronunciationAccent = PronunciationAccent.US
    ) {
        if (isInitialized) {
            configureVoice(accent)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun playAudioUrl(
        audioUrl: String,
        fallbackText: String,
        fallbackAccent: PronunciationAccent,
        requestId: Int
    ) {
        scope.launch {
            val player = MediaPlayer()
            val isDataSourceReady = withContext(Dispatchers.IO) {
                runCatching {
                    player.setDataSource(audioUrl)
                    true
                }.getOrElse {
                    false
                }
            }

            if (!isDataSourceReady || requestId != playbackRequestId) {
                player.runCatching { release() }
                if (requestId == playbackRequestId) {
                    speakWithTts(fallbackText, fallbackAccent)
                }
                return@launch
            }

            runCatching {
                player.apply {
                setOnPreparedListener { preparedPlayer ->
                    if (mediaPlayer === preparedPlayer && requestId == playbackRequestId) {
                        preparedPlayer.start()
                    } else {
                        preparedPlayer.release()
                    }
                }
                setOnCompletionListener { completedPlayer ->
                    if (mediaPlayer === completedPlayer) {
                        mediaPlayer = null
                    }
                    completedPlayer.release()
                }
                setOnErrorListener { errorPlayer, _, _ ->
                    if (mediaPlayer === errorPlayer) {
                        mediaPlayer = null
                    }
                    errorPlayer.release()
                    if (requestId == playbackRequestId) {
                        speakWithTts(fallbackText, fallbackAccent)
                    }
                    true
                }
                mediaPlayer = player
                prepareAsync()
            }
            }.onFailure {
                player.runCatching { release() }
                if (requestId == playbackRequestId) {
                    speakWithTts(fallbackText, fallbackAccent)
                }
            }
        }
    }

    private fun configureVoice(accent: PronunciationAccent = PronunciationAccent.US) {
        tts?.setSpeechRate(0.86f)
        tts?.setPitch(1.0f)
        tts?.setLanguage(accent.locale)
        val bestVoice = tts?.voices
            ?.filter { voice ->
                voice.locale.language == accent.locale.language &&
                    voice.locale.country == accent.locale.country
            }
            ?.sortedWith(
                compareBy<android.speech.tts.Voice> { it.isNetworkConnectionRequired }
                    .thenByDescending { it.quality }
                    .thenBy { it.latency }
            )
            ?.firstOrNull()
        if (bestVoice != null) {
            tts?.voice = bestVoice
        }
    }

    private fun stopAudio() {
        mediaPlayer?.runCatching {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    fun shutdown() {
        scope.cancel()
        stopAudio()
        tts?.stop()
        tts?.shutdown()
    }
}
