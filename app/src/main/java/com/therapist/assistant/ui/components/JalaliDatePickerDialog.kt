package com.therapist.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.therapist.assistant.util.JalaliDate

private val monthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
)

private fun daysInJalaliMonth(year: Int, month: Int): Int {
    return when {
        month <= 6 -> 31
        month <= 11 -> 30
        else -> if (isJalaliLeap(year)) 30 else 29
    }
}

private fun isJalaliLeap(year: Int): Boolean {
    val breaks = listOf(1, 5, 9, 13, 17, 22, 26, 30)
    val cy = year % 33
    return cy in breaks
}

@Composable
fun JalaliDatePickerDialog(
    initialMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val initial = JalaliDate.fromMillis(initialMillis)
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.month) }
    var day by remember { mutableIntStateOf(initial.day) }

    val dim = daysInJalaliMonth(year, month)
    if (day > dim) day = dim

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp)) {
                Text("انتخاب تاریخ شمسی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = {
                        if (month == 1) { month = 12; year-- } else month--
                    }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
                    Text("${monthNames[month - 1]} ${JalaliDate.toFa(year)}", fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        if (month == 12) { month = 1; year++ } else month++
                    }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
                }
                Spacer(Modifier.height(8.dp))
                // simple grid 7 columns
                val days = (1..dim).toList()
                days.chunked(7).forEach { row ->
                    Row(Modifier.fillMaxWidth()) {
                        row.forEach { d ->
                            val selected = d == day
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { day = d },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    JalaliDate.toFa(d),
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        repeat(7 - row.size) { Spacer(Modifier.weight(1f).aspectRatio(1f)) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("لغو") }
                    TextButton(onClick = {
                        val millis = JalaliDate.jalaliToGregorianMillis(year, month, day)
                        onConfirm(millis)
                    }) { Text("تأیید") }
                }
            }
        }
    }
}
