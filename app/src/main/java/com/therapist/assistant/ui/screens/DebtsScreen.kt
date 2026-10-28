package com.therapist.assistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.Appointment
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.util.MessageHelper
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DebtsScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onClientClick: (Long) -> Unit
) {
    val unpaid by viewModel.unpaidAppointments.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val context = LocalContext.current
    val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }
    val total = remember(unpaid) { unpaid.sumOf { it.fee } }
    val byClient = remember(unpaid) {
        unpaid.groupBy { it.clientId }
            .map { (clientId, list) ->
                Triple(clientId, list.firstOrNull()?.clientName ?: "—", list.sortedBy { it.dateTimeMillis } to list.sumOf { it.fee })
            }
            .sortedByDescending { it.third.second }
    }
    val twoMonthsAgo = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000
    val overdue = unpaid.filter { it.dateTimeMillis < twoMonthsAgo }

    Scaffold { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                GradientTopBar(
                    title = "مدیریت بدهی‌ها",
                    subtitle = "${JalaliDate.toFa(unpaid.size)} جلسه تسویه‌نشده",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = {
                            val sb = StringBuilder()
                            sb.appendLine("گزارش بدهی‌ها — $clinicName")
                            sb.appendLine("تاریخ: ${JalaliDate.formatDateLong(System.currentTimeMillis())}")
                            sb.appendLine("جمع کل: ${currency.format(total)} تومان")
                            sb.appendLine()
                            byClient.forEach { (_, name, pair) ->
                                val (list, sum) = pair
                                sb.appendLine("• $name — ${currency.format(sum)} تومان (${list.size} جلسه)")
                                list.forEach { a ->
                                    sb.appendLine("   ${JalaliDate.formatDate(a.dateTimeMillis)}: ${currency.format(a.fee)}")
                                }
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "گزارش بدهی‌ها")
                                        putExtra(Intent.EXTRA_TEXT, sb.toString())
                                    },
                                    "اشتراک گزارش بدهی"
                                )
                            )
                        }) { Icon(Icons.Default.Share, null, tint = Color.White) }
                    }
                )
            }

            item {
                Box(Modifier.padding(16.dp)) {
                    SoftCard {
                        Text("جمع بدهی", style = MaterialTheme.typography.labelLarge, color = Amber500)
                        Text("${currency.format(total)} تومان", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Amber500)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill("${JalaliDate.toFa(byClient.size)} بدهکار", Amber500)
                            if (overdue.isNotEmpty()) {
                                StatusPill("${JalaliDate.toFa(overdue.size)} معوقه (+۲ ماه)", MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            if (unpaid.isEmpty()) {
                item {
                    Box(Modifier.padding(16.dp)) {
                        EmptyState(Icons.Default.CheckCircle, "بدهی ثبت‌شده‌ای وجود ندارد", "همه جلسات تسویه شده‌اند")
                    }
                }
            }

            items(byClient, key = { it.first }) { (clientId, name, pair) ->
                val (list, sum) = pair
                val phone = clients.find { it.id == clientId }?.phone.orEmpty()
                Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(name, Amber500)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                TextButton(onClick = { onClientClick(clientId) }, contentPadding = PaddingValues(0.dp)) {
                                    Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                Text("${JalaliDate.toFa(list.size)} جلسه — ${currency.format(sum)} تومان",
                                    style = MaterialTheme.typography.bodySmall, color = Amber500, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        list.forEach { a ->
                            DebtRow(a, currency, { onSessionClick(a.id) }, { viewModel.togglePaid(a) })
                            Spacer(Modifier.height(6.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (phone.isNotBlank()) {
                                val remind = "سلام $name، یادآوری بدهی جلسات نزد $clinicName: ${currency.format(sum)} تومان."
                                AssistChip(onClick = { MessageHelper.openSms(context, phone, remind) }, label = { Text("پیامک") })
                                AssistChip(onClick = { MessageHelper.openWhatsApp(context, phone, remind) }, label = { Text("واتساپ") })
                            }
                            TextButton(onClick = { list.forEach { if (!it.isPaid) viewModel.togglePaid(it) } }) {
                                Text("تسویه همه")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtRow(appt: Appointment, currency: NumberFormat, onOpen: () -> Unit, onMarkPaid: () -> Unit) {
    val isOverdue = appt.dateTimeMillis < System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(12.dp),
        color = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(JalaliDate.formatDateTime(appt.dateTimeMillis), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (isOverdue) Text("معوقه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            Text("${currency.format(appt.fee)} ت", fontWeight = FontWeight.Bold, color = Amber500)
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(onClick = onMarkPaid, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp), shape = RoundedCornerShape(10.dp)) {
                Text("تسویه")
            }
        }
    }
}
