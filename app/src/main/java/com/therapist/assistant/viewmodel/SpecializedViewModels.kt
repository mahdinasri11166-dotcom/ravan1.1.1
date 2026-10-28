package com.therapist.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

/**
 * ViewModelهای تخصصی رزرو — فعلاً بدون دسترسی DB تا خطای کامپایل ندهند.
 * UI از AppointmentViewModel استفاده می‌کند.
 */
class ClientsViewModel(application: Application) : AndroidViewModel(application)

class FinanceViewModel(application: Application) : AndroidViewModel(application)

class SettingsFeatureViewModel(application: Application) : AndroidViewModel(application)

class SessionAccessViewModel(application: Application) : AndroidViewModel(application) {
    fun isSecretary(): Boolean = false
}
