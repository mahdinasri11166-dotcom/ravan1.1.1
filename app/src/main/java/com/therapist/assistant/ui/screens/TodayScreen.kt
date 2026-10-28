package com.therapist.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.therapist.assistant.domain.AppChangelog
import com.therapist.assistant.util.MessageHelper
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: AppointmentViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onClientsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAllAppointments: () -> Unit,
    onDebtsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSelfCareClick: () -> Unit = {},
    onDocumentsClick: () -> Unit = {},
    onBusinessCardClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    val todayList by viewModel.getTodayAppointments().collectAsState(initial = emptyList())
    val unpaid by viewModel.unpaidAppointments.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val upcoming by viewModel.upcomingAppointments.collectAsState()
    val template by viewModel.msgTemplate.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val needsBackup by viewModel.needsBackupAfterUpdate.collectAsState()
    val context = LocalContext.current
    val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }
    val dateLabel = remember { JalaliDate.formatDateLong(System.currentTimeMillis()) }
    var showMsgPreview by remember { mutableStateOf(false) }
    var msgPreviewText by remember { mutableStateOf("") }
    var msgPreviewPhone by remember { mutableStateOf("") }
    var msgPreviewChannel by remember { mutableStateOf("sms") }
    val nextUp = remember(upcoming) { upcoming.take(4) }
    val homework by viewModel.pendingHomework.collectAsState()
    val clientCount by viewModel.clientCount.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val isOrg by viewModel.isOrg.collectAsState()
    val trialActive = viewModel.isTrialActive()
    val planLimit = viewModel.clientLimitForPlan()
    val showQuota = !isPro && !isOrg && !trialActive && planLimit < Int.MAX_VALUE / 2

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("نوبت جدید", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (needsBackup) {
                item {
                    SoftCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("به‌روزرسانی نصب شد — لطفاً بکاپ بگیرید", fontWeight = FontWeight.Bold)
                        Text("برای جلوگیری از از دست رفتن داده، یک نسخه پشتیبان ذخیره یا ارسال کنید.", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { viewModel.dismissBackupReminder() }) { Text("بعداً") }
                            Button(onClick = { onSettingsClick() }) { Text("بکاپ در تنظیمات") }
                        }
                    }
                }
            }
            // Quota + homework + next session shortcuts
            if (showQuota) {
                item {
                    SoftCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("سقف نسخه رایگان", fontWeight = FontWeight.Bold)
                        Text("$clientCount از $planLimit مراجع")
                        LinearProgressIndicator(
                            progress = { (clientCount.toFloat() / planLimit).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                }
            }
            if (homework.isNotEmpty()) {
                item {
                    SoftCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text("تکالیف باز (${homework.size})", fontWeight = FontWeight.Bold)
                        homework.take(5).forEach { h ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${h.clientName}: ${h.title}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = { viewModel.completeHomework(h.id) }) { Text("انجام شد") }
                            }
                        }
                    }
                }
            }
            nextUp.firstOrNull()?.let { n ->
                item {
                    SoftCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        onClick = { onEditClick(n.id) }
                    ) {
                        Text("شروع جلسه بعدی", fontWeight = FontWeight.Bold)
                        Text("${n.clientName} — ${JalaliDate.formatDateTime(n.dateTimeMillis)}")
                    }
                }
            }
            // Hero header — جذاب‌تر
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.92f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                )
                            )
                        )
                ) {
                    // تزئین پس‌زمینه
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 28.dp, y = (-20).dp)
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-24).dp, y = 30.dp)
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocalFlorist,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "سلام، امروز",
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    clinicName.ifBlank { "گل سرخ" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    dateLabel,
                                    color = Color.White.copy(alpha = 0.88f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(onClick = onSearchClick) {
                                Icon(Icons.Default.Search, "جستجو", tint = Color.White)
                            }
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Default.Settings, "تنظیمات", tint = Color.White)
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HeroStat(
                                Modifier.weight(1f),
                                JalaliDate.toFa(todayList.size.toString()),
                                "جلسه امروز",
                                Icons.Default.Event
                            )
                            HeroStat(
                                Modifier.weight(1f),
                                JalaliDate.toFa(upcoming.size.toString()),
                                "آینده",
                                Icons.Default.Schedule
                            )
                            HeroStat(
                                Modifier.weight(1f),
                                JalaliDate.toFa(unpaid.size.toString()),
                                "بدهکار",
                                Icons.Default.AccountBalanceWallet
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onAddClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.95f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("نوبت جدید", fontWeight = FontWeight.SemiBold)
                            }
                            FilledTonalButton(
                                onClick = onCalendarClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.18f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("تقویم")
                            }
                        }
                    }
                }
            }


            // Quick navigation
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { NavChip("جلسات", Icons.Default.EventNote, onAllAppointments) }
                    item { NavChip("مراجعین", Icons.Default.People, onClientsClick) }
                    item { NavChip("بدهی‌ها", Icons.Default.AccountBalanceWallet, onDebtsClick) }
                    item { NavChip("جستجو", Icons.Default.Search, onSearchClick) }
                    item { NavChip("بیشتر", Icons.Default.Apps, onMoreClick) }
                }
            }

            // Next up strip
            if (nextUp.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader("نوبت بعدی", "همه", onAllAppointments)
                    }
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nextUp, key = { it.id }) { a ->
                            SoftCard(
                                modifier = Modifier.width(168.dp),
                                onClick = { onEditClick(a.id) }
                            ) {
                                Text(
                                    JalaliDate.formatTime(a.dateTimeMillis),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    JalaliDate.formatDate(a.dateTimeMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    a.clientName,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (a.fee > 0) {
                                    Text(
                                        "${currency.format(a.fee)} ت",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (a.isPaid) Emerald500 else Amber500
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Today's sessions
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionHeader(
                        "جلسات امروز",
                        if (todayList.isNotEmpty()) "${todayList.size} جلسه" else null
                    )
                }
            }

            if (todayList.isEmpty()) {
                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        EmptyState(
                            icon = Icons.Default.EventAvailable,
                            title = "امروز جلسه‌ای ندارید",
                            subtitle = "زمان خوبی برای برنامه‌ریزی یا استراحت است",
                            actionLabel = "ثبت نوبت",
                            onAction = onAddClick
                        )
                    }
                }
            } else {
                items(todayList, key = { it.id }) { appt ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        TodaySessionCard(
                            appt = appt,
                            currency = currency,
                            onOpen = { onEditClick(appt.id) },
                            onComplete = { viewModel.updateStatus(appt, AppointmentStatus.COMPLETED) },
                            onCancel = {
                                viewModel.updateStatus(appt, AppointmentStatus.CANCELLED, "لغو سریع")
                            },
                            onSms = {
                                val phone = clients.find { it.id == appt.clientId }?.phone.orEmpty()
                                msgPreviewPhone = phone
                                msgPreviewChannel = "sms"
                                msgPreviewText = MessageHelper.buildReminderText(
                                    template, appt.clientName, appt.dateTimeMillis, clinicName
                                )
                                showMsgPreview = true
                            },
                            onWhatsApp = {
                                val phone = clients.find { it.id == appt.clientId }?.phone.orEmpty()
                                msgPreviewPhone = phone
                                msgPreviewChannel = "wa"
                                msgPreviewText = MessageHelper.buildReminderText(
                                    template, appt.clientName, appt.dateTimeMillis, clinicName
                                )
                                showMsgPreview = true
                            }
                        )
                    }
                }
            }

            // Wellness teaser
            item {
                val wellness = remember(upcoming, unpaid, todayList) { viewModel.wellnessWeekStats() }
                SoftCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text("سلامت کاری این هفته", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "انجام‌شده: ${JalaliDate.toFa(wellness.completedCount.toString())} · لغو: ${JalaliDate.toFa(wellness.cancelledCount.toString())} · شلوغ‌ترین روز: ${wellness.busiestDayLabel}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(wellness.suggestionFa, color = Amber500, style = MaterialTheme.typography.labelMedium)
                }
            }

            // Debts teaser
            if (unpaid.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SectionHeader("بدهکاران", "مدیریت", onDebtsClick)
                    }
                }
                items(unpaid.take(4), key = { "d-${it.id}" }) { appt ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        SoftCard(onClick = { onEditClick(appt.id) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarCircle(appt.clientName, Amber500, 42)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(appt.clientName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        JalaliDate.formatDate(appt.dateTimeMillis),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${currency.format(appt.fee)} ت",
                                        fontWeight = FontWeight.Bold,
                                        color = Amber500
                                    )
                                    TextButton(
                                        onClick = { viewModel.togglePaid(appt) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) { Text("تسویه") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMsgPreview) {
        AlertDialog(
            onDismissRequest = { showMsgPreview = false },
            title = { Text("پیش‌نمایش پیام یادآور") },
            text = {
                Column {
                    Text(
                        "متن را ویرایش کنید، سپس ارسال را بزنید.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = msgPreviewText,
                        onValueChange = { msgPreviewText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when (msgPreviewChannel) {
                        "wa" -> {
                            if (msgPreviewPhone.isNotBlank())
                                MessageHelper.openWhatsApp(context, msgPreviewPhone, msgPreviewText)
                            else MessageHelper.openEitaa(context, "", msgPreviewText)
                        }
                        else -> MessageHelper.openSms(context, msgPreviewPhone, msgPreviewText)
                    }
                    showMsgPreview = false
                }) { Text("ارسال") }
            },
            dismissButton = {
                TextButton(onClick = { showMsgPreview = false }) { Text("انصراف") }
            }
        )
    }

}

@Composable
private fun HeroStat(
    modifier: Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            label,
            color = Color.White.copy(alpha = 0.88f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}


@Composable
private fun TodaySessionCard(
    appt: Appointment,
    currency: NumberFormat,
    onOpen: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onSms: () -> Unit,
    onWhatsApp: () -> Unit
) {
    SoftCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    JalaliDate.formatTime(appt.dateTimeMillis),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    appt.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (appt.notes.isNotBlank()) {
                    Text(
                        appt.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (appt.fee > 0) {
                    Text(
                        "${currency.format(appt.fee)} تومان · ${if (appt.isPaid) "پرداخت‌شده" else "بدهکار"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (appt.isPaid) Emerald500 else Amber500
                    )
                }
            }
            StatusPill(
                when (appt.status) {
                    AppointmentStatus.SCHEDULED -> "زمان‌بندی"
                    AppointmentStatus.COMPLETED -> "انجام"
                    AppointmentStatus.CANCELLED -> "لغو"
                    AppointmentStatus.RESCHEDULED -> "جابجا"
                },
                when (appt.status) {
                    AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
                    AppointmentStatus.COMPLETED -> Emerald500
                    AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                    AppointmentStatus.RESCHEDULED -> Sky500
                }
            )
        }
        if (appt.status == AppointmentStatus.SCHEDULED) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("شروع جلسه (پروتکل قبلی)")
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onComplete,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("انجام")
                }
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("لغو")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSms) {
                    Icon(Icons.Default.Sms, "پیامک", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onWhatsApp) {
                    Icon(Icons.Default.Chat, "واتساپ", tint = Emerald500)
                }
            }
        }
    }
}
