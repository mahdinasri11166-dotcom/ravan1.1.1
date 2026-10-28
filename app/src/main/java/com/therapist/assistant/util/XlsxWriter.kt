package com.therapist.assistant.util

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * XLSX writer با جدول خط‌کشی‌شده، هدر رنگی و عرض ستون.
 * بدون Apache POI — سازگار با Excel / Google Sheets / LibreOffice.
 */
object XlsxWriter {

    data class Sheet(
        val name: String,
        val rows: List<List<String>>,
        /** شماره ردیف‌هایی که هدر جدول هستند (۱-based). پیش‌فرض: ردیف‌هایی که شبیه هدرند */
        val headerRowIndices: Set<Int> = emptySet(),
        val colWidths: List<Double> = emptyList()
    )

    fun write(file: File, sheets: List<Sheet>) {
        FileOutputStream(file).use { fos ->
            ZipOutputStream(fos).use { zip ->
                fun put(path: String, content: String) {
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(content.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                put("[Content_Types].xml", contentTypes(sheets.size))
                put("_rels/.rels", rootRels())
                put("xl/workbook.xml", workbookXml(sheets))
                put("xl/_rels/workbook.xml.rels", workbookRels(sheets.size))
                put("xl/styles.xml", stylesXml())
                sheets.forEachIndexed { index, sheet ->
                    put("xl/worksheets/sheet${index + 1}.xml", sheetXml(sheet))
                }
            }
        }
    }

    private fun xmlEscape(s: String): String = buildString(s.length + 8) {
        for (c in s) {
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\u0009', '\n', '\r' -> append(c)
                else -> if (c.code in 0x20..0xD7FF || c.code in 0xE000..0xFFFD) append(c)
            }
        }
    }

    private fun contentTypes(n: Int): String {
        val sheets = (1..n).joinToString("") {
            """<Override PartName="/xl/worksheets/sheet$it.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
$sheets
</Types>"""
    }

    private fun rootRels() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbookXml(sheets: List<Sheet>): String {
        val entries = sheets.mapIndexed { i, s ->
            """<sheet name="${xmlEscape(s.name.take(31))}" sheetId="${i + 1}" r:id="rId${i + 1}"/>"""
        }.joinToString("")
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
 xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets>$entries</sheets>
</workbook>"""
    }

    private fun workbookRels(n: Int): String {
        val rels = (1..n).joinToString("") {
            """<Relationship Id="rId$it" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$it.xml"/>"""
        } + """<Relationship Id="rId${n + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>"""
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
$rels
</Relationships>"""
    }

    /**
     * xfId:
     * 0 = normal bordered
     * 1 = header (bold + blue fill + white font + border)
     * 2 = title (bold large, no fill)
     * 3 = meta label (bold)
     * 4 = alt row (light gray + border)
     */
private fun stylesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="4">
    <font><sz val="11"/><name val="Tahoma"/><family val="2"/><charset val="178"/></font>
    <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Tahoma"/><family val="2"/><charset val="178"/></font>
    <font><b/><sz val="14"/><color rgb="FF0C4A6E"/><name val="Tahoma"/><family val="2"/><charset val="178"/></font>
    <font><b/><sz val="11"/><color rgb="FF0F172A"/><name val="Tahoma"/><family val="2"/><charset val="178"/></font>
  </fonts>
  <fills count="4">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF0284C7"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFF1F5F9"/><bgColor indexed="64"/></patternFill></fill>
  </fills>
  <borders count="2">
    <border>
      <left/><right/><top/><bottom/><diagonal/>
    </border>
    <border>
      <left style="medium"><color rgb="FF000000"/></left>
      <right style="medium"><color rgb="FF000000"/></right>
      <top style="medium"><color rgb="FF000000"/></top>
      <bottom style="medium"><color rgb="FF000000"/></bottom>
      <diagonal/>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="5">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1" applyAlignment="1">
      <alignment horizontal="right" vertical="center" wrapText="1" readingOrder="2"/>
    </xf>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center" wrapText="1" readingOrder="2"/>
    </xf>
    <xf numFmtId="0" fontId="2" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="right" vertical="center" wrapText="1" readingOrder="2"/>
    </xf>
    <xf numFmtId="0" fontId="3" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="right" vertical="center" wrapText="1" readingOrder="2"/>
    </xf>
    <xf numFmtId="0" fontId="0" fillId="3" borderId="1" xfId="0" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="right" vertical="center" wrapText="1" readingOrder="2"/>
    </xf>
  </cellXfs>
</styleSheet>"""

    private fun colName(index: Int): String {
        var n = index
        val sb = StringBuilder()
        do {
            sb.insert(0, ('A'.code + n % 26).toChar())
            n = n / 26 - 1
        } while (n >= 0)
        return sb.toString()
    }

    private fun detectHeaderRows(rows: List<List<String>>, explicit: Set<Int>): Set<Int> {
        if (explicit.isNotEmpty()) return explicit
        val found = mutableSetOf<Int>()
        rows.forEachIndexed { idx, row ->
            val joined = row.joinToString(" ")
            val isHeader = row.size >= 3 && (
                joined.contains("ردیف") ||
                    joined.contains("نام و نام") ||
                    joined.contains("کد ملی") ||
                    joined.contains("مدرک تحصیلی") ||
                    joined.contains("نمره") && joined.contains("سؤال") ||
                    joined.contains("برچسب پاسخ") ||
                    (row.firstOrNull() == "ردیف")
                )
            if (isHeader) found.add(idx + 1) // 1-based
        }
        return found
    }

    private fun sheetXml(sheet: Sheet): String {
        val rows = sheet.rows
        val headerRows = detectHeaderRows(rows, sheet.headerRowIndices)
        val maxCols = rows.maxOfOrNull { it.size } ?: 1

        // column widths
        val widths = if (sheet.colWidths.isNotEmpty()) {
            sheet.colWidths
        } else {
            // auto-ish widths based on content
            (0 until maxCols).map { c ->
                val maxLen = rows.maxOfOrNull { row -> row.getOrNull(c)?.length ?: 0 } ?: 8
                (maxLen * 1.2 + 4).coerceIn(10.0, 42.0)
            }
        }

        val colsXml = buildString {
            append("<cols>")
            widths.forEachIndexed { i, w ->
                append("""<col min="${i + 1}" max="${i + 1}" width="$w" customWidth="1"/>""")
            }
            append("</cols>")
        }

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        // شیت راست‌چین (RTL) برای فارسی
        sb.append("""<sheetViews><sheetView rightToLeft="1" workbookViewId="0"/></sheetViews>""")
        sb.append(colsXml)
        sb.append("<sheetData>")

        // track if we're inside a "table body" after a header row for alt coloring
        var afterHeader = false
        var dataRowIndex = 0

        rows.forEachIndexed { rIdx, row ->
            val rowNum = rIdx + 1
            val isTitle = rIdx == 0 && row.size <= 2
            val isHeader = rowNum in headerRows
            if (isHeader) {
                afterHeader = true
                dataRowIndex = 0
            }

            // pad to maxCols so grid lines are complete (like official forms)
            val padTable = afterHeader || isHeader || (row.size >= 4)
            val cells = if (padTable) {
                List(maxCols) { i -> row.getOrNull(i) ?: "" }
            } else {
                row
            }

            val rowHt = when {
                isTitle -> 22
                isHeader -> 20
                cells.any { it.length > 40 } -> 28
                else -> 18
            }
            sb.append("""<row r="$rowNum" ht="$rowHt" customHeight="1">""")
            cells.forEachIndexed { cIdx, value ->
                val ref = "${colName(cIdx)}$rowNum"
                val style = when {
                    isHeader -> 1
                    isTitle -> 2
                    afterHeader && !isHeader -> {
                        if (dataRowIndex % 2 == 1) 4 else 0
                    }
                    row.size >= 2 && cIdx % 2 == 0 && value.isNotBlank() &&
                        (value.contains("استان") || value.contains("مرکز") || value.contains("عنوان") ||
                            value.contains("تلفن") || value.contains("آدرس") || value.contains("تاریخ") ||
                            value.contains("مراجع") || value.contains("نمره") || value.contains("تفسیر") ||
                            value.contains("نام") || value.contains("شهرستان") || value.contains("مدرس") ||
                            value.contains("مدت") || value.contains("سرفصل") || value.contains("محور")) -> 3
                    padTable -> 0
                    else -> 0
                }
                val v = xmlEscape(value)
                sb.append("""<c r="$ref" t="inlineStr" s="$style"><is><t xml:space="preserve">$v</t></is></c>""")
            }
            sb.append("</row>")
            if (afterHeader && !isHeader) dataRowIndex++
        }

        sb.append("</sheetData>")

        // freeze first row optionally - skip for simplicity
        sb.append("</worksheet>")
        return sb.toString()
    }
}
