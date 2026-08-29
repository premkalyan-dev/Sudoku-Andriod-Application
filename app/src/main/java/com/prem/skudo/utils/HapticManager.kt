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

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (type) {
                    HapticType.LIGHT -> {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    }
                    HapticType.MEDIUM -> {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                    HapticType.STRONG -> {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    }
                    HapticType.SUCCESS -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                            vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK, VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                            val composition = VibrationEffect.startComposition()
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.6f)
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f, 50)
                                .compose()
                            vibrator.vibrate(composition)
                        } else {
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 25, 40, 35), -1))
                        }
                    }
                    HapticType.ERROR -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                            vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)) {
                            val composition = VibrationEffect.startComposition()
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f)
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 60)
                                .compose()
                            vibrator.vibrate(composition)
                        } else {
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 40, 60), -1))
                        }
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (type) {
                    HapticType.LIGHT -> vibrator.vibrate(VibrationEffect.createOneShot(10, 70))
                    HapticType.MEDIUM -> vibrator.vibrate(VibrationEffect.createOneShot(18, 130))
                    HapticType.STRONG -> vibrator.vibrate(VibrationEffect.createOneShot(40, 220))
                    HapticType.SUCCESS -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 25, 40, 35), -1))
                    HapticType.ERROR -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 40, 60), -1))
                }
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.LIGHT -> vibrator.vibrate(10)
                    HapticType.MEDIUM -> vibrator.vibrate(20)
                    HapticType.STRONG -> vibrator.vibrate(50)
                    HapticType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 25, 40, 35), -1)
                    HapticType.ERROR -> vibrator.vibrate(longArrayOf(0, 40, 40, 60), -1)
                }
            }
        } catch (_: Exception) {
            try {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            } catch (_: Exception) {}
        }
    }
}

enum class HapticType {
    LIGHT, MEDIUM, STRONG, SUCCESS, ERROR
}
