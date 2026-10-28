package com.therapist.assistant

import android.app.Application
import android.util.Log
import com.therapist.assistant.data.BackupWorker
import com.therapist.assistant.notification.NotificationHelper

class TherapistApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            NotificationHelper.createChannel(this)
        } catch (t: Throwable) {
            Log.e("TherapistApp", "notification channel", t)
        }
        try {
            BackupWorker.schedule(this)
        } catch (t: Throwable) {
            Log.e("TherapistApp", "backup schedule", t)
        }
    }
}
