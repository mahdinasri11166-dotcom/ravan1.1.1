package com.therapist.assistant.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * رندر کارت ویزیت به Bitmap و اشتراک به‌صورت تصویر PNG.
 */
object BusinessCardImage {

    data class CardPalette(
        val primary: Int,
        val secondary: Int,
        val accent: Int,
        val onPrimary: Int = 0xFFFFFFFF.toInt(),
        val textDark: Int = 0xFF0F172A.toInt(),
        val textMuted: Int = 0xFF475569.toInt()
    )

    fun render(
        name: String,
        role: String,
        clinic: String,
        phone: String,
        email: String,
        website: String,
        address: String,
        tagline: String,
        palette: CardPalette,
        qrBitmap: Bitmap?,
        width: Int = 1080,
        height: Int = 640
    ): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val density = width / 360f
        fun dp(v: Float): Float = v * density

        val card = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val clip = Path().apply { addRoundRect(card, dp(28f), dp(28f), Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(clip)

        canvas.drawColor(0xFFF8FAFC.toInt())

        val headerH = height * 0.42f
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), headerH,
                intArrayOf(palette.primary, palette.secondary),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), headerH, headerPaint)

        val deco = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33FFFFFF
        }
        canvas.drawCircle(width * 0.92f, dp(20f), dp(90f), deco)
        canvas.drawCircle(width * 0.05f, headerH + dp(10f), dp(50f), deco)

        val avR = dp(34f)
        val avCx = dp(40f) + avR
        val avCy = dp(36f) + avR
        canvas.drawCircle(avCx, avCy, avR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x44FFFFFF })
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xAAFFFFFF.toInt()
            setStyle(android.graphics.Paint.Style.STROKE)
            strokeWidth = dp(2.5f)
        }
        canvas.drawCircle(avCx, avCy, avR, strokePaint)

        val initial = name.trim().firstOrNull()?.toString() ?: "د"
        val initialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.onPrimary
            textSize = dp(28f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val fm = initialPaint.fontMetrics
        canvas.drawText(initial, avCx, avCy - (fm.ascent + fm.descent) / 2f, initialPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.onPrimary
            textSize = dp(26f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val rolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xEEFFFFFF.toInt()
            textSize = dp(14f)
            textAlign = Paint.Align.RIGHT
        }
        val clinicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCCFFFFFF.toInt()
            textSize = dp(13f)
            textAlign = Paint.Align.RIGHT
        }
        val right = width - dp(28f)
        var ty = dp(48f)
        canvas.drawText(name.ifBlank { "درمانگر" }, right, ty, namePaint)
        ty += dp(28f)
        canvas.drawText(role.ifBlank { "روان‌شناس" }, right, ty, rolePaint)
        if (clinic.isNotBlank()) {
            ty += dp(22f)
            canvas.drawText(clinic, right, ty, clinicPaint)
        }
        if (tagline.isNotBlank()) {
            ty += dp(22f)
            canvas.drawText(
                tagline,
                right,
                ty,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xBBFFFFFF.toInt()
                    textSize = dp(12f)
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        val bodyY0 = headerH + dp(28f)
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.primary }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.textDark
            textSize = dp(14f)
            textAlign = Paint.Align.RIGHT
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.textMuted
            textSize = dp(12f)
            textAlign = Paint.Align.RIGHT
        }
        var by = bodyY0
        fun drawInfo(value: String, muted: Boolean = false) {
            if (value.isBlank()) return
            canvas.drawCircle(width - dp(28f), by - dp(5f), dp(4f), dotPaint)
            canvas.drawText(value, width - dp(42f), by, if (muted) mutedPaint else linePaint)
            by += dp(26f)
        }
        drawInfo(phone)
        drawInfo(email)
        drawInfo(website)
        drawInfo(address, muted = true)

        canvas.drawText(
            "درمانگرنو · کارت ویزیت دیجیتال",
            width - dp(28f),
            height - dp(22f),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = palette.textMuted
                textSize = dp(11f)
                textAlign = Paint.Align.RIGHT
            }
        )

        qrBitmap?.let { qr ->
            val qrSize = dp(120f).toInt().coerceAtLeast(120)
            val scaled = Bitmap.createScaledBitmap(qr, qrSize, qrSize, true)
            val qx = dp(24f)
            val qy = height - qrSize - dp(28f)
            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
            val r = RectF(qx - dp(8f), qy - dp(8f), qx + qrSize + dp(8f), qy + qrSize + dp(8f))
            canvas.drawRoundRect(r, dp(12f), dp(12f), bg)
            canvas.drawBitmap(scaled, qx, qy, null)
            if (scaled !== qr) scaled.recycle()
        }

        canvas.restore()
        return bmp
    }

    fun saveToCache(context: Context, bitmap: Bitmap, fileName: String = "business_card.png"): File {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    fun shareImage(context: Context, file: File, title: String = "کارت ویزیت") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "ارسال کارت ویزیت به‌صورت تصویر").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (t: Throwable) {
            android.util.Log.e("BusinessCardImage", "share failed", t)
            android.widget.Toast.makeText(
                context,
                "امکان اشتراک تصویر نیست: ${t.message ?: "خطا"}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}
