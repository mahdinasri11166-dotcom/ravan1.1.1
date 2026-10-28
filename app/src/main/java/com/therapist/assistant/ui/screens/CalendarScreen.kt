package com.therapist.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onAppointmentClick: (Long) -> Unit
) {
    val cal = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(cal.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(cal.get(Calendar.DAY_OF_MONTH)) }
    var pendingMoveApptId by remember { mutableStateOf<Long?>(null) }
    var pendingMoveTimeOfDay by remember { mutableLongStateOf(0L) }
    var filterTherapistId by remember { mutableStateOf<Long?>(null) }
    val therapists by viewModel.therapists.collectAsState()
    val dailyCap by viewModel.dailyAppointmentCap.collectAsState()

    val monthStart = remember(year, month) {
        Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val monthEnd = remember(year, month) {
        Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
        }.timeInMillis
    }
    val appointments by viewModel.getAppointmentsBetween(monthStart, monthEnd)
        .collectAsState(initial = emptyList())
    val filteredAppts = remember(appointments, filterTherapistId) {
        if (filterTherapistId == null) appointments
        else appointments.filter { it.therapistId == filterTherapistId || it.therapistId == 0L }
    }
    val daysWithAppts = remember(filteredAppts) {
        filteredAppts.map {
            Calendar.getInstance().apply { timeInMillis = it.dateTimeMillis }.get(Calendar.DAY_OF_MONTH)
        }.toSet()
    }
    val selectedDayAppts = remember(filteredAppts, selectedDay) {
        filteredAppts.filter {
            Calendar.getInstance().apply { timeInMillis = it.dateTimeMillis }.get(Calendar.DAY_OF_MONTH) == selectedDay
        }.sortedBy { it.dateTimeMillis }
    }
    val firstDayOfWeek = remember(year, month) {
        Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK).let { (it + 5) % 7 }
    }
    val daysInMonth = remember(year, month) {
        Calendar.getInstance().apply { set(year, month, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val monthName = remember(year, month) {
        val mid = Calendar.getInstance().apply { set(year, month, 15) }.timeInMillis
        JalaliDate.formatMonthYear(mid)
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(title = if (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600) "تقویم (حالت عریض)" else "تقویم", subtitle = monthName, onBack = onBack)
            if (therapists.size > 1) {
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = filterTherapistId == null, onClick = { filterTherapistId = null }, label = { Text("همه") })
                    therapists.take(6).forEach { th ->
                        FilterChip(
                            selected = filterTherapistId == th.id,
                            onClick = { filterTherapistId = th.id },
                            label = { Text(th.name.take(10)) }
                        )
                    }
                }
            }
            if (dailyCap > 0) {
                val dayStart = remember(year, month, selectedDay) {
                    java.util.Calendar.getInstance().apply {
                        set(year, month, selectedDay, 0, 0, 0); set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                val cnt = viewModel.countAppointmentsOnDay(dayStart)
                if (cnt >= dailyCap) {
                    Text(
                        "سقف نوبت امروز پر است ($cnt/$dailyCap)",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            SoftCard(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = {
                        if (month == 0) { month = 11; year-- } else month--
                        selectedDay = 1
                    }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "ماه قبل") }
                    Text(monthName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        if (month == 11) { month = 0; year++ } else month++
                        selectedDay = 1
                    }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "ماه بعد") }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    listOf("د", "س", "چ", "پ", "ج", "ش", "ی").forEach {
                        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(4.dp))
                val cells = firstDayOfWeek + daysInMonth
                val rows = (cells + 6) / 7
                for (row in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val idx = row * 7 + col
                            val day = idx - firstDayOfWeek + 1
                            if (day in 1..daysInMonth) {
                                val selected = day == selectedDay
                                val hasAppt = day in daysWithAppts
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                selected -> MaterialTheme.colorScheme.primary
                                                hasAppt -> MaterialTheme.colorScheme.primaryContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { selectedDay = day
                                pendingMoveApptId?.let { moveId ->
                                    val target = Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis + pendingMoveTimeOfDay
                                    viewModel.rescheduleAppointment(moveId, target) {
                                        pendingMoveApptId = null
                                    }
                                } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        JalaliDate.toFa(day),
                                        color = when {
                                            selected -> Color.White
                                            hasAppt -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (selected || hasAppt) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            } else {
                                Spacer(Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            Text(
                "جلسات روز ${JalaliDate.toFa(selectedDay)}",
                Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedDayAppts.isEmpty()) {
                    item {
                        SoftCard {
                            Text("جلسه‌ای در این روز نیست", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    if (pendingMoveApptId != null) {
                        SoftCard {
                            Text("حالت جابجایی فعال", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("روی یک روز در تقویم بالا بزنید تا نوبت به آن روز منتقل شود. ساعت جلسه حفظ می‌شود.", style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { pendingMoveApptId = null }) { Text("انصراف") }
                        }
                    }
                }
                items(selectedDayAppts, key = { it.id }) { appt ->
                    SoftCard(onClick = { onAppointmentClick(appt.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                JalaliDate.formatTime(appt.dateTimeMillis),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(appt.clientName, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("جابجایی:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(
                                onClick = {
                                    val dayStart = Calendar.getInstance().apply {
                                        timeInMillis = appt.dateTimeMillis
                                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    pendingMoveTimeOfDay = appt.dateTimeMillis - dayStart
                                    pendingMoveApptId = appt.id
                                },
                                label = { Text("انتخاب و انتقال به روز دیگر") }
                            )
                            AssistChip(
                                onClick = {
                                    viewModel.rescheduleAppointment(appt.id, appt.dateTimeMillis + 24L * 60 * 60 * 1000)
                                },
                                label = { Text("+۱ روز") }
                            )
                            AssistChip(
                                onClick = {
                                    viewModel.rescheduleAppointment(appt.id, appt.dateTimeMillis + 7L * 24 * 60 * 60 * 1000)
                                },
                                label = { Text("+۱ هفته") }
                            )
                            AssistChip(
                                onClick = {
                                    viewModel.rescheduleAppointment(appt.id, appt.dateTimeMillis + 60L * 60 * 1000)
                                },
                                label = { Text("+۱ ساعت") }
                            )
                        }
                    }
                }
            }
        }
    }
}
