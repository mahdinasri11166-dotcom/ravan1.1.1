package com.therapist.assistant.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("therapist_settings")

class SettingsRepository(private val context: Context) {
    private val remindersKey = booleanPreferencesKey("reminders_enabled")
    private val dayBeforeKey = booleanPreferencesKey("remind_day_before")
    private val hourBeforeKey = booleanPreferencesKey("remind_hour_before")
    private val morningKey = booleanPreferencesKey("remind_morning")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")
    private val pinHashKey = stringPreferencesKey("pin_hash")
    private val clinicNameKey = stringPreferencesKey("clinic_name")
    private val clinicPhoneKey = stringPreferencesKey("clinic_phone")
    private val clinicAddressKey = stringPreferencesKey("clinic_address")
    private val msgTemplateKey = stringPreferencesKey("msg_template")
    private val themeHueKey = floatPreferencesKey("theme_hue")
    private val themeIdKey = stringPreferencesKey("theme_id")
    private val isProKey = booleanPreferencesKey("is_pro")
    private val privacyKey = booleanPreferencesKey("privacy_accepted")
    private val biometricKey = booleanPreferencesKey("biometric_enabled")
    private val trialStartKey = longPreferencesKey("trial_start_millis")
    private val isOrgKey = booleanPreferencesKey("is_org")
    private val deviceBindKey = stringPreferencesKey("device_bind_id")
    private val tipsDoneKey = booleanPreferencesKey("tips_done")
    private val autoBackupKey = booleanPreferencesKey("auto_backup_enabled")
    private val usedCodesKey = stringPreferencesKey("used_pro_codes")
    private val lastVersionKey = stringPreferencesKey("last_app_version")
    private val tipsTourKey = booleanPreferencesKey("tips_tour_done")
    private val defaultFeeKey = doublePreferencesKey("default_session_fee")
    private val currentTherapistIdKey = longPreferencesKey("current_therapist_id")
    private val therapistNameKey = stringPreferencesKey("default_therapist_name")
    private val autoLockMinutesKey = intPreferencesKey("auto_lock_minutes")
    private val lastActiveKey = longPreferencesKey("last_active_millis")
    private val backupTreeUriKey = stringPreferencesKey("backup_tree_uri")
    private val lastBackupPathKey = stringPreferencesKey("last_backup_path")
    private val lastBackupSizeKey = longPreferencesKey("last_backup_size")
    private val lastBackupTimeKey = longPreferencesKey("last_backup_time")
    private val backupPassphraseKey = stringPreferencesKey("backup_passphrase")
    private val darkModeKey = stringPreferencesKey("dark_mode") // system|light|dark
    private val dailyCapKey = intPreferencesKey("daily_appointment_cap")
    private val ongoingNotifKey = booleanPreferencesKey("ongoing_next_session_notif")
    private val driveTipDoneKey = booleanPreferencesKey("drive_tip_done")
    private val msgDebtKey = stringPreferencesKey("msg_template_debt")
    private val msgBirthdayKey = stringPreferencesKey("msg_template_birthday")

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[remindersKey] ?: true }
    val remindDayBefore: Flow<Boolean> = context.dataStore.data.map { it[dayBeforeKey] ?: true }
    val remindHourBefore: Flow<Boolean> = context.dataStore.data.map { it[hourBeforeKey] ?: true }
    val remindMorning: Flow<Boolean> = context.dataStore.data.map { it[morningKey] ?: false }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[onboardingKey] ?: false }
    val pinHash: Flow<String?> = context.dataStore.data.map { it[pinHashKey] }
    val clinicName: Flow<String> = context.dataStore.data.map { it[clinicNameKey] ?: "مرکز گل سرخ" }
    val clinicPhone: Flow<String> = context.dataStore.data.map { it[clinicPhoneKey] ?: "" }
    val clinicAddress: Flow<String> = context.dataStore.data.map { it[clinicAddressKey] ?: "" }
    val msgTemplate: Flow<String> = context.dataStore.data.map {
        it[msgTemplateKey] ?: "سلام {name}، یادآوری جلسه: {date} ساعت {time}. مرکز گل سرخ"
    }
    val isPro: Flow<Boolean> = context.dataStore.data.map { it[isProKey] ?: false }
    val privacyAccepted: Flow<Boolean> = context.dataStore.data.map { it[privacyKey] ?: false }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[biometricKey] ?: false }
    val trialStartMillis: Flow<Long> = context.dataStore.data.map { it[trialStartKey] ?: 0L }
    val isOrg: Flow<Boolean> = context.dataStore.data.map { it[isOrgKey] ?: false }
    val deviceBindId: Flow<String> = context.dataStore.data.map { it[deviceBindKey] ?: "" }
    val tipsDone: Flow<Boolean> = context.dataStore.data.map { it[tipsDoneKey] ?: false }
    val autoBackupEnabled: Flow<Boolean> = context.dataStore.data.map { it[autoBackupKey] ?: true }
    val usedProCodes: Flow<String> = context.dataStore.data.map { it[usedCodesKey] ?: "" }
    val lastAppVersion: Flow<String> = context.dataStore.data.map { it[lastVersionKey] ?: "" }
    val tipsTourDone: Flow<Boolean> = context.dataStore.data.map { it[tipsTourKey] ?: false }
    val defaultSessionFee: Flow<Double> = context.dataStore.data.map { it[defaultFeeKey] ?: 0.0 }
    val currentTherapistId: Flow<Long> = context.dataStore.data.map { it[currentTherapistIdKey] ?: 0L }
    val defaultTherapistName: Flow<String> = context.dataStore.data.map { it[therapistNameKey] ?: "" }
    /** ۰ = غیرفعال؛ ۵/۱۰/۱۵ دقیقه */
    val autoLockMinutes: Flow<Int> = context.dataStore.data.map { it[autoLockMinutesKey] ?: 0 }
    val lastActiveMillis: Flow<Long> = context.dataStore.data.map { it[lastActiveKey] ?: 0L }
    val backupTreeUri: Flow<String?> = context.dataStore.data.map { it[backupTreeUriKey] }
    val lastBackupPath: Flow<String> = context.dataStore.data.map { it[lastBackupPathKey] ?: "" }
    val lastBackupSize: Flow<Long> = context.dataStore.data.map { it[lastBackupSizeKey] ?: 0L }
    val lastBackupTime: Flow<Long> = context.dataStore.data.map { it[lastBackupTimeKey] ?: 0L }
    val backupPassphrase: Flow<String> = context.dataStore.data.map { it[backupPassphraseKey] ?: "" }
    val darkMode: Flow<String> = context.dataStore.data.map { it[darkModeKey] ?: "system" }
    val dailyAppointmentCap: Flow<Int> = context.dataStore.data.map { it[dailyCapKey] ?: 0 }
    val ongoingNextSessionNotif: Flow<Boolean> = context.dataStore.data.map { it[ongoingNotifKey] ?: false }
    val driveTipDone: Flow<Boolean> = context.dataStore.data.map { it[driveTipDoneKey] ?: false }
    val msgTemplateDebt: Flow<String> = context.dataStore.data.map {
        it[msgDebtKey] ?: "سلام {name}، یادآوری بدهی جلسه در {clinic}. مبلغ: {amount}"
    }
    val msgTemplateBirthday: Flow<String> = context.dataStore.data.map {
        it[msgBirthdayKey] ?: "{name} عزیز، تولدتان مبارک از طرف {clinic}"
    }
    val themeHue: Flow<Float> = context.dataStore.data.map { it[themeHueKey] ?: 350f }
    val themeId: Flow<String> = context.dataStore.data.map { it[themeIdKey] ?: "sky" }

    suspend fun setRemindersEnabled(v: Boolean) = context.dataStore.edit { it[remindersKey] = v }
    suspend fun setRemindDayBefore(v: Boolean) = context.dataStore.edit { it[dayBeforeKey] = v }
    suspend fun setRemindHourBefore(v: Boolean) = context.dataStore.edit { it[hourBeforeKey] = v }
    suspend fun setRemindMorning(v: Boolean) = context.dataStore.edit { it[morningKey] = v }
    suspend fun setOnboardingDone(v: Boolean) = context.dataStore.edit { it[onboardingKey] = v }
    suspend fun setPinHash(hash: String?) = context.dataStore.edit {
        if (hash == null) it.remove(pinHashKey) else it[pinHashKey] = hash
    }
    suspend fun setClinicName(v: String) = context.dataStore.edit { it[clinicNameKey] = v.trim().ifBlank { "مرکز گل سرخ" } }
    suspend fun setClinicPhone(v: String) = context.dataStore.edit { it[clinicPhoneKey] = v }
    suspend fun setClinicAddress(v: String) = context.dataStore.edit { it[clinicAddressKey] = v }
    suspend fun setMsgTemplate(v: String) = context.dataStore.edit { it[msgTemplateKey] = v }
    suspend fun setThemeHue(v: Float) = context.dataStore.edit { it[themeHueKey] = v }
    suspend fun setThemeId(v: String) = context.dataStore.edit { it[themeIdKey] = v }
    suspend fun setIsPro(v: Boolean) = context.dataStore.edit { it[isProKey] = v }
    suspend fun setPrivacyAccepted(v: Boolean) = context.dataStore.edit { it[privacyKey] = v }
    suspend fun setBiometricEnabled(v: Boolean) = context.dataStore.edit { it[biometricKey] = v }
    suspend fun setTrialStartMillis(v: Long) = context.dataStore.edit { it[trialStartKey] = v }
    suspend fun setIsOrg(v: Boolean) = context.dataStore.edit { it[isOrgKey] = v }
    suspend fun setDeviceBindId(v: String) = context.dataStore.edit { it[deviceBindKey] = v }
    suspend fun setTipsDone(v: Boolean) = context.dataStore.edit { it[tipsDoneKey] = v }
    suspend fun setAutoBackupEnabled(v: Boolean) = context.dataStore.edit { it[autoBackupKey] = v }
    suspend fun setUsedProCodes(v: String) = context.dataStore.edit { it[usedCodesKey] = v }
    suspend fun setLastAppVersion(v: String) = context.dataStore.edit { it[lastVersionKey] = v }
    suspend fun setTipsTourDone(v: Boolean) = context.dataStore.edit { it[tipsTourKey] = v }
    suspend fun setDefaultSessionFee(v: Double) = context.dataStore.edit { it[defaultFeeKey] = v }
    suspend fun setCurrentTherapistId(v: Long) = context.dataStore.edit { it[currentTherapistIdKey] = v }
    suspend fun setDefaultTherapistName(v: String) = context.dataStore.edit { it[therapistNameKey] = v }
    suspend fun setAutoLockMinutes(v: Int) = context.dataStore.edit { it[autoLockMinutesKey] = v.coerceIn(0, 60) }
    suspend fun setLastActiveMillis(v: Long) = context.dataStore.edit { it[lastActiveKey] = v }
    suspend fun setBackupTreeUri(v: String?) = context.dataStore.edit {
        if (v.isNullOrBlank()) it.remove(backupTreeUriKey) else it[backupTreeUriKey] = v
    }
    suspend fun setLastBackupMeta(path: String, size: Long, time: Long = System.currentTimeMillis()) =
        context.dataStore.edit {
            it[lastBackupPathKey] = path
            it[lastBackupSizeKey] = size
            it[lastBackupTimeKey] = time
        }
    suspend fun setBackupPassphrase(v: String) = context.dataStore.edit {
        if (v.isBlank()) it.remove(backupPassphraseKey) else it[backupPassphraseKey] = v
    }
    suspend fun setDarkMode(v: String) = context.dataStore.edit { it[darkModeKey] = v }
    suspend fun setDailyAppointmentCap(v: Int) = context.dataStore.edit { it[dailyCapKey] = v.coerceIn(0, 50) }
    suspend fun setOngoingNextSessionNotif(v: Boolean) = context.dataStore.edit { it[ongoingNotifKey] = v }
    suspend fun setDriveTipDone(v: Boolean) = context.dataStore.edit { it[driveTipDoneKey] = v }
    suspend fun setMsgTemplateDebt(v: String) = context.dataStore.edit { it[msgDebtKey] = v }
    suspend fun setMsgTemplateBirthday(v: String) = context.dataStore.edit { it[msgBirthdayKey] = v }

    /** همه تنظیمات به‌صورت Map برای پشتیبان */
    suspend fun getAllAsMap(): Map<String, String> {
        val prefs = context.dataStore.data.first()
        val map = mutableMapOf<String, String>()
        prefs[remindersKey]?.let { map["reminders_enabled"] = it.toString() }
        prefs[dayBeforeKey]?.let { map["remind_day_before"] = it.toString() }
        prefs[hourBeforeKey]?.let { map["remind_hour_before"] = it.toString() }
        prefs[morningKey]?.let { map["remind_morning"] = it.toString() }
        prefs[onboardingKey]?.let { map["onboarding_done"] = it.toString() }
        prefs[pinHashKey]?.let { map["pin_hash"] = it }
        prefs[clinicNameKey]?.let { map["clinic_name"] = it }
        prefs[clinicPhoneKey]?.let { map["clinic_phone"] = it }
        prefs[clinicAddressKey]?.let { map["clinic_address"] = it }
        prefs[msgTemplateKey]?.let { map["msg_template"] = it }
        prefs[themeHueKey]?.let { map["theme_hue"] = it.toString() }
        prefs[themeIdKey]?.let { map["theme_id"] = it }
        prefs[isProKey]?.let { map["is_pro"] = it.toString() }
        prefs[privacyKey]?.let { map["privacy_accepted"] = it.toString() }
        prefs[biometricKey]?.let { map["biometric_enabled"] = it.toString() }
        prefs[trialStartKey]?.let { map["trial_start_millis"] = it.toString() }
        prefs[isOrgKey]?.let { map["is_org"] = it.toString() }
        prefs[deviceBindKey]?.let { map["device_bind_id"] = it }
        prefs[tipsDoneKey]?.let { map["tips_done"] = it.toString() }
        prefs[autoBackupKey]?.let { map["auto_backup_enabled"] = it.toString() }
        prefs[defaultFeeKey]?.let { map["default_session_fee"] = it.toString() }
        prefs[currentTherapistIdKey]?.let { map["current_therapist_id"] = it.toString() }
        prefs[therapistNameKey]?.let { map["default_therapist_name"] = it }
        prefs[autoLockMinutesKey]?.let { map["auto_lock_minutes"] = it.toString() }
        prefs[backupTreeUriKey]?.let { map["backup_tree_uri"] = it }
        return map
    }

    suspend fun applyFromMap(map: Map<String, String>) {
        context.dataStore.edit { prefs ->
            map["reminders_enabled"]?.toBooleanStrictOrNull()?.let { prefs[remindersKey] = it }
            map["remind_day_before"]?.toBooleanStrictOrNull()?.let { prefs[dayBeforeKey] = it }
            map["remind_hour_before"]?.toBooleanStrictOrNull()?.let { prefs[hourBeforeKey] = it }
            map["remind_morning"]?.toBooleanStrictOrNull()?.let { prefs[morningKey] = it }
            map["onboarding_done"]?.toBooleanStrictOrNull()?.let { prefs[onboardingKey] = it }
            map["pin_hash"]?.let { prefs[pinHashKey] = it }
            map["clinic_name"]?.let { prefs[clinicNameKey] = it }
            map["clinic_phone"]?.let { prefs[clinicPhoneKey] = it }
            map["clinic_address"]?.let { prefs[clinicAddressKey] = it }
            map["msg_template"]?.let { prefs[msgTemplateKey] = it }
            map["theme_hue"]?.toFloatOrNull()?.let { prefs[themeHueKey] = it }
            map["theme_id"]?.let { prefs[themeIdKey] = it }
            map["is_pro"]?.toBooleanStrictOrNull()?.let { prefs[isProKey] = it }
            map["privacy_accepted"]?.toBooleanStrictOrNull()?.let { prefs[privacyKey] = it }
            map["biometric_enabled"]?.toBooleanStrictOrNull()?.let { prefs[biometricKey] = it }
            map["trial_start_millis"]?.toLongOrNull()?.let { prefs[trialStartKey] = it }
            map["is_org"]?.toBooleanStrictOrNull()?.let { prefs[isOrgKey] = it }
            map["device_bind_id"]?.let { prefs[deviceBindKey] = it }
            map["tips_done"]?.toBooleanStrictOrNull()?.let { prefs[tipsDoneKey] = it }
            map["auto_backup_enabled"]?.toBooleanStrictOrNull()?.let { prefs[autoBackupKey] = it }
            map["default_session_fee"]?.toDoubleOrNull()?.let { prefs[defaultFeeKey] = it }
            map["current_therapist_id"]?.toLongOrNull()?.let { prefs[currentTherapistIdKey] = it }
            map["default_therapist_name"]?.let { prefs[therapistNameKey] = it }
            map["auto_lock_minutes"]?.toIntOrNull()?.let { prefs[autoLockMinutesKey] = it }
        }
    }
}
