package com.therapist.assistant.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import com.therapist.assistant.ui.theme.Sky500
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.*

private enum class SessionFilter {
    ALL, TODAY, WEEK, UPCOMING, COMPLETED, CANCELLED, UNPAID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsDashboardScreen(
    viewModel: AppointmentViewModel,
    onBack: (() -> Unit)? = null,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onClientsClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    onDebtsClick: () -> Unit = {}
) {
    val all by viewModel.allAppointments.collectAsState()
    val unpaid by viewModel.unpaidAppointments.collectAsState()
    val upcoming by viewModel.upcomingAppointments.collectAsState()

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(SessionFilter.UPCOMING) }
    val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }
    val now = remember { System.currentTimeMillis() }

    val dayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val dayEnd = dayStart + 24L * 60 * 60 * 1000
    val weekEnd = dayStart + 7L * 24 * 60 * 60 * 1000
    val monthStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayList = remember(all, dayStart) {
        all.filter { it.dateTimeMillis in dayStart until dayEnd }.sortedBy { it.dateTimeMillis }
    }
    val weekList = remember(all, dayStart, weekEnd) {
        all.filter { it.dateTimeMillis in dayStart until weekEnd && it.status == AppointmentStatus.SCHEDULED }
    }
    val completedMonth = remember(all, monthStart) {
        all.count { it.status == AppointmentStatus.COMPLETED && it.dateTimeMillis >= monthStart }
    }
    val cancelledMonth = remember(all, monthStart) {
        all.count { it.status == AppointmentStatus.CANCELLED && it.dateTimeMillis >= monthStart }
    }
    val monthRevenue = remember(all, monthStart) {
        all.filter { it.isPaid && it.dateTimeMillis >= monthStart }.sumOf { it.fee }
    }
    val unpaidTotal = remember(unpaid) { unpaid.sumOf { it.fee } }
    val nextSessions = remember(upcoming) { upcoming.take(5) }

    val filtered = remember(all, query, filter, dayStart, dayEnd, weekEnd, now) {
        all.filter { a ->
            val matchQuery = query.isBlank() ||
                a.clientName.contains(query, true) || a.notes.contains(query, true) ||
                a.sessionNotes.contains(query, true) || a.noteTags.contains(query, true) ||
                a.soapSubjective.contains(query, true) || a.soapAssessment.contains(query, true) ||
                a.invoiceSerial.contains(query, true)
            val matchFilter = when (filter) {
                SessionFilter.ALL -> true
                SessionFilter.TODAY -> a.dateTimeMillis in dayStart until dayEnd
                SessionFilter.WEEK -> a.dateTimeMillis in dayStart until weekEnd && a.status == AppointmentStatus.SCHEDULED
                SessionFilter.UPCOMING -> a.status == AppointmentStatus.SCHEDULED && a.dateTimeMillis >= now
                SessionFilter.COMPLETED -> a.status == AppointmentStatus.COMPLETED
                SessionFilter.CANCELLED -> a.status == AppointmentStatus.CANCELLED
                SessionFilter.UNPAID -> !a.isPaid && a.status != AppointmentStatus.CANCELLED && a.fee > 0
            }
            matchQuery && matchFilter
        }.sortedWith(
            when (filter) {
                SessionFilter.COMPLETED, SessionFilter.CANCELLED, SessionFilter.ALL ->
                    compareByDescending { it.dateTimeMillis }
                else -> compareBy { it.dateTimeMillis }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("جلسه جدید", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                GradientTopBar(
                    title = "داشبورد جلسات",
                    subtitle = "${JalaliDate.toFa(all.size)} جلسه در سیستم",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = onCalendarClick) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Color.White)
                        }
                        IconButton(onClick = onClientsClick) {
                            Icon(Icons.Default.People, null, tint = Color.White)
                        }
                        IconButton(onClick = onReportsClick) {
                            Icon(Icons.Default.Assessment, null, tint = Color.White)
                        }
                    }
                )
            }

            item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiTile(Modifier.weight(1f), "امروز", "${todayList.size}", Icons.Default.Event, MaterialTheme.colorScheme.primary)
                        KpiTile(Modifier.weight(1f), "این هفته", "${weekList.size}", Icons.Default.DateRange, Sky500)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiTile(Modifier.weight(1f), "انجام ماه", "$completedMonth", Icons.Default.CheckCircle, Emerald500)
                        KpiTile(Modifier.weight(1f), "لغو ماه", "$cancelledMonth", Icons.Default.Cancel, MaterialTheme.colorScheme.error)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiTile(Modifier.weight(1f), "درآمد ماه", currency.format(monthRevenue), Icons.Default.AttachMoney, MaterialTheme.colorScheme.primary)
                        KpiTile(Modifier.weight(1f), "بدهی", currency.format(unpaidTotal), Icons.Default.AccountBalanceWallet, Amber500, onDebtsClick)
                    }
                }
            }

            if (nextSessions.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader("نوبت‌های بعدی", "همه پیش‌رو") { filter = SessionFilter.UPCOMING }
                    }
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nextSessions, key = { it.id }) { a ->
                            SoftCard(Modifier.width(156.dp), onClick = { onEditClick(a.id) }) {
                                Text(JalaliDate.formatTime(a.dateTimeMillis), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(JalaliDate.formatDate(a.dateTimeMillis), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Text(a.clientName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (a.fee > 0) {
                                    Text("${currency.format(a.fee)} ت", style = MaterialTheme.typography.labelMedium, color = if (a.isPaid) Emerald500 else Amber500)
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("جستجوی نام، تگ، یادداشت، فاکتور...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, null) }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf(
                        SessionFilter.UPCOMING to "پیش‌رو (${upcoming.size})",
                        SessionFilter.TODAY to "امروز (${todayList.size})",
                        SessionFilter.WEEK to "هفته (${weekList.size})",
                        SessionFilter.UNPAID to "بدهکار (${unpaid.size})",
                        SessionFilter.COMPLETED to "انجام‌شده",
                        SessionFilter.CANCELLED to "لغو‌شده",
                        SessionFilter.ALL to "همه (${all.size})"
                    )
                    items(chips.size) { i ->
                        val (f, label) = chips[i]
                        FilterChip(
                            selected = filter == f,
                            onClick = { filter = f },
                            label = { Text(label) },
                            leadingIcon = if (filter == f) ({ Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }) else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionHeader("نتایج", "${JalaliDate.toFa(filtered.size)}")
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Box(Modifier.padding(16.dp)) {
                        EmptyState(Icons.Default.EventNote, "جلسه‌ای در این فیلتر نیست", actionLabel = "ثبت جلسه", onAction = onAddClick)
                    }
                }
            }

            items(filtered, key = { it.id }) { appt ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    SessionCard(
                        appt = appt,
                        currency = currency,
                        onClick = { onEditClick(appt.id) },
                        onComplete = { viewModel.updateStatus(appt, AppointmentStatus.COMPLETED) },
                        onCancel = { viewModel.updateStatus(appt, AppointmentStatus.CANCELLED, "لغو از داشبورد") },
                        onTogglePaid = { viewModel.togglePaid(appt) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    appt: Appointment,
    currency: NumberFormat,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onTogglePaid: () -> Unit
) {
    val statusColor = when (appt.status) {
        AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        AppointmentStatus.COMPLETED -> Emerald500
        AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
        AppointmentStatus.RESCHEDULED -> Sky500
    }
    val statusLabel = when (appt.status) {
        AppointmentStatus.SCHEDULED -> "زمان‌بندی"
        AppointmentStatus.COMPLETED -> "انجام‌شده"
        AppointmentStatus.CANCELLED -> "لغو"
        AppointmentStatus.RESCHEDULED -> "جابجا"
    }

    SoftCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.clip(RoundedCornerShape(12.dp)).background(statusColor.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(JalaliDate.formatTime(appt.dateTimeMillis), fontWeight = FontWeight.Bold, color = statusColor)
                Text(JalaliDate.formatDate(appt.dateTimeMillis), style = MaterialTheme.typography.labelSmall, color = statusColor.copy(alpha = 0.85f))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(appt.clientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(JalaliDate.formatDateTime(appt.dateTimeMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusPill(statusLabel, statusColor)
        }
        if (appt.fee > 0 || appt.noteTags.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (appt.fee > 0) {
                    AssistChip(
                        onClick = onTogglePaid,
                        label = { Text("${currency.format(appt.fee)} — ${if (appt.isPaid) "پرداخت" else "بدهکار"}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (appt.isPaid) Emerald500.copy(alpha = 0.12f) else Amber500.copy(alpha = 0.12f)
                        )
                    )
                }
                if (appt.noteTags.isNotBlank()) {
                    Text("🏷 ${appt.noteTags}", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (appt.status == AppointmentStatus.SCHEDULED) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onComplete, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("انجام")
                }
                OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("لغو")
                }
            }
        }
    }
}
