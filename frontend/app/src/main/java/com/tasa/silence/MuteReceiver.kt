package com.tasa.silence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.tasa.DependenciesContainer
import com.tasa.TasaApplication
import com.tasa.domain.Action
import com.tasa.domain.Mode
import com.tasa.domain.UserInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MuteReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
        val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            val repo = ((context.applicationContext as TasaApplication) as DependenciesContainer).userInfoRepository
            when (intent.getParcelableExtra("action", Action::class.java)) {
                Action.MUTE -> {
                    mute(audioManager, repo)
                }
                Action.UNMUTE -> {
                    unmute(audioManager, repo)
                }

                null -> {}
            }

            Log.d("Alarm", "Alarme disparado")
        } catch (ex: Throwable) {
            Log.e("Alarm", "Erro ao agendar alarme: ${ex.message}")
        }
    }

    private fun mute(
        audioManager: AudioManager,
        repo: UserInfoRepository,
    ) {
        val currentMode = audioManager.getCurrentMode()
        CoroutineScope(Dispatchers.IO).launch {
            repo.writeLastMode(currentMode)
        }
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    private fun unmute(
        audioManager: AudioManager,
        repo: UserInfoRepository,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val lastMode = repo.lastMode()
            if (lastMode != null) {
                audioManager.ringerMode =
                    when (lastMode) {
                        Mode.SILENT -> AudioManager.RINGER_MODE_SILENT
                        Mode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                        Mode.RINGING -> AudioManager.RINGER_MODE_NORMAL
                    }
            }
        }
    }

    private fun AudioManager.getCurrentMode(): Mode {
        return when (ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> Mode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> Mode.VIBRATE
            AudioManager.RINGER_MODE_NORMAL -> Mode.RINGING
            else -> Mode.RINGING
        }
    }
}
