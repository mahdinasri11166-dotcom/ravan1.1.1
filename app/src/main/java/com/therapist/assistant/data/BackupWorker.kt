package com.therapist.assistant.data

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.therapist.assistant.util.BackupHelper
import com.therapist.assistant.util.CloudBackupHelper
import com.therapist.assistant.util.EncryptedBackup
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class BackupWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val settingsRepo = SettingsRepository(applicationContext)
            if (!settingsRepo.autoBackupEnabled.first()) return Result.success()

            val json = BackupHelper.buildJson(
                db.clientDao().getAllClientsOnce(),
                db.appointmentDao().getAllOnce(),
                db.questionnaireDao().getAllOnce(),
                db.workshopDao().getAllOnce(),
                db.workshopDao().getAllParticipantsOnce(),
                settingsRepo.getAllAsMap()
            )
            val plainFile = CloudBackupHelper.saveToAppBackupsDir(
                applicationContext,
                json.toByteArray(Charsets.UTF_8)
            )
            settingsRepo.setLastBackupMeta(plainFile.absolutePath, plainFile.length())

            val tree = settingsRepo.backupTreeUri.first()
            if (!tree.isNullOrBlank()) {
                val pass = settingsRepo.backupPassphrase.first().trim()
                val useEnc = pass.isNotBlank()
                val payload = if (useEnc) EncryptedBackup.encrypt(json, pass) else json
                val prefix = if (useEnc) "darmanagarno_auto_enc" else "darmanagarno_auto"
                val name = CloudBackupHelper.defaultBackupFileName(prefix)
                CloudBackupHelper.writeToTreeFolder(
                    applicationContext,
                    Uri.parse(tree),
                    payload.toByteArray(Charsets.UTF_8),
                    name
                )
                settingsRepo.setLastBackupMeta("SAF:$name", payload.length.toLong())
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE = "weekly_backup"
        fun schedule(context: Context) {
            val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<BackupWorker>(7, TimeUnit.DAYS).setConstraints(constraints).build()
            )
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE + "_daily",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS).setConstraints(constraints).build()
            )
        }
    }
}
