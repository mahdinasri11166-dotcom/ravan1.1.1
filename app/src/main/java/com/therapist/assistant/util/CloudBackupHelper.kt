package com.therapist.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CloudBackupHelper {

    data class BackupEntry(
        val name: String,
        val uri: Uri,
        val sizeBytes: Long,
        val lastModified: Long
    )

    fun shareFile(context: Context, file: File, subject: String, mime: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, subject)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "ارسال به فضای ابری / ایمیل").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (t: Throwable) {
            android.util.Log.e("CloudBackup", "share failed", t)
        }
    }

    fun openTreeIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
    }

    fun createDocumentIntent(fileName: String, mime: String = "application/json"): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
    }

    fun takePersistablePermission(context: Context, uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (t: Throwable) {
            android.util.Log.w("CloudBackup", "persist permission: ${t.message}")
        }
    }

    fun defaultBackupFileName(prefix: String = "darmanagarno_backup"): String {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "${prefix}_$ts.json"
    }

    fun saveToAppBackupsDir(context: Context, bytes: ByteArray, fileName: String = defaultBackupFileName()): File {
        val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "backups").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        return file
    }

    fun writeToUri(context: Context, uri: Uri, bytes: ByteArray) {
        context.contentResolver.openOutputStream(uri, "w")?.use { out: OutputStream ->
            out.write(bytes)
            out.flush()
        } ?: error("باز کردن مسیر ذخیره ممکن نشد")
    }

    fun writeFileToUri(context: Context, uri: Uri, file: File) {
        writeToUri(context, uri, file.readBytes())
    }

    fun writeToTreeFolder(
        context: Context,
        treeUri: Uri,
        bytes: ByteArray,
        fileName: String = defaultBackupFileName(),
        subFolderName: String = "DarmanagarNo-Backups"
    ): String {
        val tree = DocumentFile.fromTreeUri(context, treeUri)
            ?: error("دسترسی به پوشه انتخابی ممکن نیست")
        val folder = tree.findFile(subFolderName)?.takeIf { it.isDirectory }
            ?: tree.createDirectory(subFolderName)
            ?: error("ساخت پوشه $subFolderName ممکن نشد")
        folder.findFile(fileName)?.delete()
        val mime = if (fileName.endsWith(".json")) "application/json" else "application/octet-stream"
        val doc = folder.createFile(mime, fileName)
            ?: error("ساخت فایل پشتیبان ممکن نشد")
        writeToUri(context, doc.uri, bytes)
        return fileName
    }

    fun treeDisplayName(context: Context, treeUriString: String?): String {
        if (treeUriString.isNullOrBlank()) return "انتخاب نشده"
        return try {
            val uri = Uri.parse(treeUriString)
            val doc = DocumentFile.fromTreeUri(context, uri)
            doc?.name ?: uri.lastPathSegment ?: "پوشه انتخابی"
        } catch (_: Exception) {
            "پوشه انتخابی"
        }
    }

    fun listBackupsInTree(
        context: Context,
        treeUri: Uri,
        subFolderName: String = "DarmanagarNo-Backups"
    ): List<BackupEntry> {
        val tree = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        val folder = tree.findFile(subFolderName)?.takeIf { it.isDirectory } ?: tree
        return folder.listFiles()
            .filter { f ->
                f.isFile && (
                    f.name?.endsWith(".json", true) == true ||
                        f.name?.contains("backup", true) == true ||
                        f.name?.endsWith(".enc", true) == true
                    )
            }
            .map {
                BackupEntry(
                    name = it.name ?: "backup",
                    uri = it.uri,
                    sizeBytes = it.length(),
                    lastModified = it.lastModified()
                )
            }
            .sortedByDescending { it.lastModified }
    }

    fun readUriText(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        } ?: error("خواندن فایل ممکن نشد")
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
