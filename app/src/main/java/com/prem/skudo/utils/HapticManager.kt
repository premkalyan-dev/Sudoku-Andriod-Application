package com.prem.skudo.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.prem.skudo.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HapticManager(private val context: Context) {
    private val settingsRepository = SettingsRepository(context)
    private var isEnabled = true

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            settingsRepository.vibration.collect {
                isEnabled = it
            }
        }
    }

    fun vibrate(type: HapticType) {
        if (!isEnabled) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (type) {
                HapticType.LIGHT -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                HapticType.MEDIUM -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                HapticType.STRONG -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                HapticType.SUCCESS -> {
                    val pattern = longArrayOf(0, 50, 50, 50)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                }
                HapticType.ERROR -> {
                    val pattern = longArrayOf(0, 100, 50, 100)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.LIGHT -> vibrator.vibrate(20)
                HapticType.MEDIUM -> vibrator.vibrate(50)
                HapticType.STRONG -> vibrator.vibrate(100)
                HapticType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
                HapticType.ERROR -> vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
            }
        }
    }
}

enum class HapticType {
    LIGHT, MEDIUM, STRONG, SUCCESS, ERROR
}
