package com.therapist.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.therapist.assistant.data.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * پشتیبان کامل JSON: مراجعین، نوبت‌ها، پرسشنامه‌ها، کارگاه‌ها، تنظیمات.
 */
object BackupHelper {

    const val BACKUP_VERSION = 5
    private const val MIME = "application/json"

    fun buildJson(
        clients: List<Client>,
        appointments: List<Appointment>,
        questionnaires: List<QuestionnaireResult>,
        workshops: List<Workshop>,
        participants: List<WorkshopParticipant>,
        settings: Map<String, String>
    ): String {
        val root = JSONObject()
        root.put("app", "درمانگرنو")
        root.put("version", BACKUP_VERSION)
        root.put("exported", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

        val clientsArr = JSONArray()
        clients.forEach { c ->
            clientsArr.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("nationalId", c.nationalId)
                put("birthDateMillis", c.birthDateMillis)
                put("address", c.address)
                put("education", c.education)
                put("isArchived", c.isArchived)
                put("notes", c.notes)
                put("createdAt", c.createdAt)
            })
        }
        root.put("clients", clientsArr)

        val apptArr = JSONArray()
        appointments.forEach { a ->
            apptArr.put(JSONObject().apply {
                put("id", a.id)
                put("clientId", a.clientId)
                put("clientName", a.clientName)
                put("dateTimeMillis", a.dateTimeMillis)
                put("notes", a.notes)
                put("sessionNotes", a.sessionNotes)
                put("soapSubjective", a.soapSubjective)
                put("soapObjective", a.soapObjective)
                put("soapAssessment", a.soapAssessment)
                put("soapPlan", a.soapPlan)
                put("noteTags", a.noteTags)
                put("fee", a.fee)
                put("isPaid", a.isPaid)
                put("paymentMethod", a.paymentMethod.name)
                put("status", a.status.name)
                put("cancelReason", a.cancelReason)
                put("recurrence", a.recurrence.name)
                put("recurrenceEndMillis", a.recurrenceEndMillis)
                put("parentRecurringId", a.parentRecurringId ?: JSONObject.NULL)
                put("invoiceSerial", a.invoiceSerial)
            })
        }
        root.put("appointments", apptArr)

        val qArr = JSONArray()
        questionnaires.forEach { q ->
            qArr.put(JSONObject().apply {
                put("id", q.id)
                put("clientId", q.clientId)
                put("type", q.type)
                put("score", q.score)
                put("detailsJson", q.detailsJson)
                put("createdAt", q.createdAt)
            })
        }
        root.put("questionnaires", qArr)

        val wArr = JSONArray()
        workshops.forEach { w ->
            wArr.put(JSONObject().apply {
                put("id", w.id)
                put("title", w.title)
                put("province", w.province)
                put("city", w.city)
                put("centerName", w.centerName)
                put("location", w.location)
                put("durationHours", w.durationHours)
                put("topics", w.topics)
                put("trainerName", w.trainerName)
                put("dateMillis", w.dateMillis)
                put("createdAt", w.createdAt)
            })
        }
        root.put("workshops", wArr)

        val pArr = JSONArray()
        participants.forEach { p ->
            pArr.put(JSONObject().apply {
                put("id", p.id)
                put("workshopId", p.workshopId)
                put("clientId", p.clientId ?: JSONObject.NULL)
                put("fullName", p.fullName)
                put("birthDateText", p.birthDateText)
                put("education", p.education)
                put("nationalId", p.nationalId)
                put("preTestScore", p.preTestScore)
                put("postTestScore", p.postTestScore)
                put("phone", p.phone)
            })
        }
        root.put("workshopParticipants", pArr)

        val sObj = JSONObject()
        settings.forEach { (k, v) -> sObj.put(k, v) }
        root.put("settings", sObj)

        return root.toString(2)
    }

    fun saveToAppStorage(context: Context, json: String): File {
        val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "backups").apply { mkdirs() }
        val name = "darmanagarno_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
        val file = File(dir, name)
        file.writeText(json, Charsets.UTF_8)
        // keep last 10
        dir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() }
            ?.drop(10)?.forEach { it.delete() }
        return file
    }

    fun fileProviderUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /** اشتراک‌گذاری فایل برای جیمیل، گوگل درایو، ذخیره در Downloads و … */
    fun shareBackupIntent(context: Context, file: File): Intent {
        val uri = fileProviderUri(context, file)
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "پشتیبان درمانگرنو — ${file.name}")
            putExtra(
                Intent.EXTRA_TEXT,
                "فایل پشتیبان کامل درمانگرنو (مراجعین، نوبت‌ها، کارگاه‌ها، تنظیمات).\nتاریخ: ${file.name}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    data class ParsedBackup(
        val clients: List<Client>,
        val appointments: List<Appointment>,
        val questionnaires: List<QuestionnaireResult>,
        val workshops: List<Workshop>,
        val participants: List<WorkshopParticipant>,
        val settings: Map<String, String>
    )

    fun parse(json: String): ParsedBackup {
        val root = JSONObject(json)
        val clients = mutableListOf<Client>()
        val cArr = root.optJSONArray("clients") ?: JSONArray()
        for (i in 0 until cArr.length()) {
            val o = cArr.getJSONObject(i)
            clients.add(
                Client(
                    id = 0, // re-insert
                    name = o.optString("name"),
                    phone = o.optString("phone"),
                    nationalId = o.optString("nationalId"),
                    birthDateMillis = o.optLong("birthDateMillis", o.optLong("birthDate", 0L)),
                    address = o.optString("address"),
                    education = o.optString("education"),
                    isArchived = o.optBoolean("isArchived", false),
                    notes = o.optString("notes"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val appts = mutableListOf<Appointment>()
        val aArr = root.optJSONArray("appointments") ?: JSONArray()
        for (i in 0 until aArr.length()) {
            val o = aArr.getJSONObject(i)
            appts.add(
                Appointment(
                    id = 0,
                    clientId = o.optLong("clientId"),
                    clientName = o.optString("clientName"),
                    dateTimeMillis = o.optLong("dateTimeMillis", o.optLong("dateTime", 0L)),
                    notes = o.optString("notes"),
                    sessionNotes = o.optString("sessionNotes"),
                    soapSubjective = o.optString("soapSubjective"),
                    soapObjective = o.optString("soapObjective"),
                    soapAssessment = o.optString("soapAssessment"),
                    soapPlan = o.optString("soapPlan"),
                    noteTags = o.optString("noteTags"),
                    fee = o.optDouble("fee", 0.0),
                    isPaid = o.optBoolean("isPaid", false),
                    paymentMethod = try {
                        PaymentMethod.valueOf(o.optString("paymentMethod", "NONE"))
                    } catch (_: Exception) {
                        PaymentMethod.NONE
                    },
                    status = try {
                        AppointmentStatus.valueOf(o.optString("status", "SCHEDULED"))
                    } catch (_: Exception) {
                        AppointmentStatus.SCHEDULED
                    },
                    cancelReason = o.optString("cancelReason"),
                    recurrence = try {
                        RecurrenceType.valueOf(o.optString("recurrence", "NONE"))
                    } catch (_: Exception) {
                        RecurrenceType.NONE
                    },
                    recurrenceEndMillis = o.optLong("recurrenceEndMillis", 0L),
                    parentRecurringId = if (o.isNull("parentRecurringId")) null else o.optLong("parentRecurringId"),
                    invoiceSerial = o.optString("invoiceSerial")
                )
            )
        }

        val qs = mutableListOf<QuestionnaireResult>()
        val qArr = root.optJSONArray("questionnaires") ?: JSONArray()
        for (i in 0 until qArr.length()) {
            val o = qArr.getJSONObject(i)
            qs.add(
                QuestionnaireResult(
                    id = 0,
                    clientId = o.optLong("clientId"),
                    type = o.optString("type"),
                    score = o.optInt("score", 0),
                    detailsJson = o.optString("detailsJson"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val workshops = mutableListOf<Workshop>()
        val wArr = root.optJSONArray("workshops") ?: JSONArray()
        for (i in 0 until wArr.length()) {
            val o = wArr.getJSONObject(i)
            workshops.add(
                Workshop(
                    id = o.optLong("id"), // keep for mapping participants
                    title = o.optString("title"),
                    province = o.optString("province", "خوزستان"),
                    city = o.optString("city", "اهواز"),
                    centerName = o.optString("centerName", "گل سرخ"),
                    location = o.optString("location"),
                    durationHours = o.optInt("durationHours", 4),
                    topics = o.optString("topics"),
                    trainerName = o.optString("trainerName"),
                    dateMillis = o.optLong("dateMillis", System.currentTimeMillis()),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val parts = mutableListOf<WorkshopParticipant>()
        val pArr = root.optJSONArray("workshopParticipants") ?: JSONArray()
        for (i in 0 until pArr.length()) {
            val o = pArr.getJSONObject(i)
            parts.add(
                WorkshopParticipant(
                    id = 0,
                    workshopId = o.optLong("workshopId"),
                    clientId = if (o.isNull("clientId")) null else o.optLong("clientId"),
                    fullName = o.optString("fullName"),
                    birthDateText = o.optString("birthDateText"),
                    education = o.optString("education"),
                    nationalId = o.optString("nationalId"),
                    preTestScore = o.optString("preTestScore"),
                    postTestScore = o.optString("postTestScore"),
                    phone = o.optString("phone")
                )
            )
        }

        val settings = mutableMapOf<String, String>()
        val sObj = root.optJSONObject("settings")
        if (sObj != null) {
            val keys = sObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                settings[k] = sObj.optString(k, "")
            }
        }

        return ParsedBackup(clients, appts, qs, workshops, parts, settings)
    }
}
