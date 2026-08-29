package com.prem.skudo.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import com.prem.skudo.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundManager(private val context: Context) {
    private val settingsRepository = SettingsRepository(context)
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()
    private var isEnabled = true
    
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
    }

    private fun playVictoryFanfare() {
        if (!isEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
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
    }

    private fun playCompletionChime() {
        if (!isEnabled) return
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    private fun playMistakeTone() {
        if (!isEnabled) return
        toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
    }

    private fun playGenericTap() {
        if (!isEnabled) return
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
    }

    fun release() {
        soundPool.release()
        toneGenerator.release()
    }
}
