package com.therapist.assistant.util

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrUtil {

    fun encode(
        text: String,
        size: Int = 640,
        moduleColor: Int = 0xFF0C4A6E.toInt(),
        backgroundColor: Int = AndroidColor.WHITE
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) moduleColor else backgroundColor)
            }
        }
        return bmp
    }

    /** vCard 3.0 برای ذخیره مستقیم در مخاطبین گوشی */
    fun vCard(
        fullName: String,
        organization: String = "",
        title: String = "",
        phone: String = "",
        email: String = "",
        address: String = "",
        url: String = "",
        note: String = ""
    ): String = buildString {
        appendLine("BEGIN:VCARD")
        appendLine("VERSION:3.0")
        appendLine("FN:${escape(fullName)}")
        if (organization.isNotBlank()) appendLine("ORG:${escape(organization)}")
        if (title.isNotBlank()) appendLine("TITLE:${escape(title)}")
        if (phone.isNotBlank()) appendLine("TEL;TYPE=CELL:${phone.filter { it.isDigit() || it == '+' }}")
        if (email.isNotBlank()) appendLine("EMAIL:${escape(email)}")
        if (address.isNotBlank()) appendLine("ADR;TYPE=WORK:;;${escape(address)};;;;")
        if (url.isNotBlank()) appendLine("URL:${escape(url)}")
        if (note.isNotBlank()) appendLine("NOTE:${escape(note)}")
        appendLine("END:VCARD")
    }

    private fun escape(s: String) = s
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")
}
