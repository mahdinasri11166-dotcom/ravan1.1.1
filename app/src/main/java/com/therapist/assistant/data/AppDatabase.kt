package com.therapist.assistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.MessageDigest

class Converters {
    @TypeConverter fun fromStatus(s: AppointmentStatus): String = s.name
    @TypeConverter fun toStatus(v: String): AppointmentStatus =
        try { AppointmentStatus.valueOf(v) } catch (_: Exception) { AppointmentStatus.SCHEDULED }

    @TypeConverter fun fromRecurrence(r: RecurrenceType): String = r.name
    @TypeConverter fun toRecurrence(v: String): RecurrenceType =
        try { RecurrenceType.valueOf(v) } catch (_: Exception) { RecurrenceType.NONE }

    @TypeConverter fun fromPayment(p: PaymentMethod): String = p.name
    @TypeConverter fun toPayment(v: String): PaymentMethod =
        try { PaymentMethod.valueOf(v) } catch (_: Exception) { PaymentMethod.NONE }
    @TypeConverter fun fromRole(r: UserRole): String = r.name
    @TypeConverter fun toRole(v: String): UserRole =
        try { UserRole.valueOf(v) } catch (_: Exception) { UserRole.THERAPIST }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf(
            "ALTER TABLE appointments ADD COLUMN soapSubjective TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN soapObjective TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN soapAssessment TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN soapPlan TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN noteTags TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN paymentMethod TEXT NOT NULL DEFAULT 'NONE'",
            "ALTER TABLE appointments ADD COLUMN cancelReason TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE appointments ADD COLUMN recurrenceEndMillis INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE appointments ADD COLUMN invoiceSerial TEXT NOT NULL DEFAULT ''"
        ).forEach { sql -> try { db.execSQL(sql) } catch (_: Exception) {} }
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS questionnaires (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                clientId INTEGER NOT NULL,
                type TEXT NOT NULL,
                score INTEGER NOT NULL DEFAULT 0,
                detailsJson TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS app_settings (
                key TEXT PRIMARY KEY NOT NULL,
                value TEXT NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf(
            "ALTER TABLE clients ADD COLUMN nationalId TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE clients ADD COLUMN birthDateMillis INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE clients ADD COLUMN address TEXT NOT NULL DEFAULT ''"
        ).forEach { sql -> try { db.execSQL(sql) } catch (_: Exception) {} }
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try { db.execSQL("ALTER TABLE clients ADD COLUMN education TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS workshops (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                province TEXT NOT NULL DEFAULT 'خوزستان',
                city TEXT NOT NULL DEFAULT 'اهواز',
                centerName TEXT NOT NULL DEFAULT 'گل سرخ',
                location TEXT NOT NULL DEFAULT '',
                durationHours INTEGER NOT NULL DEFAULT 4,
                topics TEXT NOT NULL DEFAULT '',
                trainerName TEXT NOT NULL DEFAULT '',
                dateMillis INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS workshop_participants (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                workshopId INTEGER NOT NULL,
                clientId INTEGER,
                fullName TEXT NOT NULL,
                birthDateText TEXT NOT NULL DEFAULT '',
                education TEXT NOT NULL DEFAULT '',
                nationalId TEXT NOT NULL DEFAULT '',
                preTestScore TEXT NOT NULL DEFAULT '',
                postTestScore TEXT NOT NULL DEFAULT '',
                phone TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(workshopId) REFERENCES workshops(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_workshop_participants_workshopId ON workshop_participants(workshopId)")
    }
}


val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try { db.execSQL("ALTER TABLE clients ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS homework_tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    clientId INTEGER NOT NULL,
                    clientName TEXT NOT NULL,
                    appointmentId INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    isDone INTEGER NOT NULL,
                    dueMillis INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_homework_tasks_clientId ON homework_tasks(clientId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_homework_tasks_isDone ON homework_tasks(isDone)")
        } catch (_: Exception) {}
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    entityType TEXT NOT NULL,
                    entityId TEXT NOT NULL,
                    action TEXT NOT NULL,
                    detail TEXT NOT NULL,
                    actorName TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_createdAt ON audit_logs(createdAt)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_entityType ON audit_logs(entityType)")
        } catch (_: Exception) {}
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS follow_ups (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    clientId INTEGER NOT NULL,
                    clientName TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    dueMillis INTEGER NOT NULL,
                    isDone INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
        } catch (_: Exception) {}
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try { db.execSQL("ALTER TABLE appointments ADD COLUMN discount REAL NOT NULL DEFAULT 0") } catch (_: Exception) {}
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS therapists (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    isActive INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
        } catch (_: Exception) {}
        try { db.execSQL("ALTER TABLE clients ADD COLUMN therapistId INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
        try { db.execSQL("ALTER TABLE appointments ADD COLUMN therapistId INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
        try { db.execSQL("ALTER TABLE appointments ADD COLUMN therapistName TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
        try { db.execSQL("ALTER TABLE questionnaires ADD COLUMN therapistId INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
    }
}

@Database(
    entities = [
        Client::class, Appointment::class, QuestionnaireResult::class, AppSetting::class,
        Workshop::class, WorkshopParticipant::class, Therapist::class, AuditLog::class, FollowUp::class, HomeworkTask::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun questionnaireDao(): QuestionnaireDao
    abstract fun workshopDao(): WorkshopDao
    abstract fun therapistDao(): TherapistDao
    abstract fun auditDao(): AuditDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun homeworkDao(): HomeworkDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private fun passphrase(context: Context): ByteArray {
            // فرمول پایدار — تغییر آن دیتابیس کاربران فعلی را غیرقابل‌خواندن می‌کند
            val seed = "golsorkh-" + context.packageName
            return MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let { return it }
                try {
                    System.loadLibrary("sqlcipher")
                } catch (t: Throwable) {
                    // اگر native قبلاً لود شده یا در تست JVM باشد، کرش نکند
                    android.util.Log.w("AppDatabase", "sqlcipher load: ${t.message}")
                }
                val appCtx = context.applicationContext
                val factory = SupportOpenHelperFactory(passphrase(appCtx))
                val instance = Room.databaseBuilder(
                    appCtx,
                    AppDatabase::class.java,
                    "therapist_assistant_db_enc"
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
