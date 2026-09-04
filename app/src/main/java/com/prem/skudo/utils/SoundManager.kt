package com.prem.skudo.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import com.prem.skudo.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class SoundManager(private val context: Context) {
    private val settingsRepository = SettingsRepository(context)
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()
    private var isEnabled = true
    
    // Dedicated single-thread dispatcher for audio calls to avoid blocking the UI thread.
    // ToneGenerator.startTone() is a synchronous Binder IPC call that can block 15-50ms+.
    private val audioDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val audioScope = CoroutineScope(audioDispatcher)
    
    // ToneGenerator for "Hardware" sounds (Copyright Free & Zero-File)
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // In the future, you can add your OGG/MP3 files to res/raw and load them here:
        // loadSound("victory", R.raw.victory_sound)
        
        CoroutineScope(Dispatchers.IO).launch {
            settingsRepository.soundEffects.collect {
                isEnabled = it
            }
        }
    }

    private fun loadSound(name: String, resId: Int) {
        sounds[name] = soundPool.load(context, resId, 1)
    }

    fun playSound(name: String) {
        if (!isEnabled) return
        
        // Dispatch all audio to background thread to prevent UI thread blocking
        audioScope.launch {
            try {
                when (name) {
                    "victory" -> playVictoryFanfare()
                    "completion" -> playCompletionChime()
                    "mistake" -> playMistakeTone()
                    "place_number", "erase", "undo", "redo" -> playGenericTap()
                    else -> {
                        sounds[name]?.let { id ->
                            soundPool.play(id, 1.0f, 1.0f, 0, 0, 1.0f)
                        }
                    }
                }
            } catch (_: Exception) {
                // Silently ignore audio errors — never crash the game for a sound
            }
        }
    }

    private suspend fun playVictoryFanfare() {
        // Upbeat "Win" melody
        val sequence = listOf(
            ToneGenerator.TONE_DTMF_1 to 100,
            ToneGenerator.TONE_DTMF_4 to 100,
            ToneGenerator.TONE_DTMF_7 to 100,
            ToneGenerator.TONE_DTMF_9 to 400
        )
        sequence.forEach { (tone, duration) ->
            toneGenerator.startTone(tone, duration)
            delay(duration + 20L)
        }
    }

    private fun playCompletionChime() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    private fun playMistakeTone() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
    }

    private fun playGenericTap() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
    }

    fun release() {
        soundPool.release()
        toneGenerator.release()
    }
}

