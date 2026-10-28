package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.AssessmentTrends
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.util.TherapistWellness
import com.therapist.assistant.viewmodel.AppointmentViewModel

/**
 * داشبورد روند درمان: آزمون‌ها، پیگیری‌ها، بدهی‌ها، سلامت کاری.
 */
@Composable
fun TrendsScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onClient: (Long) -> Unit
) {
    val clients by viewModel.clients.collectAsState()
    val appts by viewModel.allAppointments.collectAsState()
    val followUps by viewModel.pendingFollowUps.collectAsState()
    val unpaid by viewModel.unpaidAppointments.collectAsState()
    val now = System.currentTimeMillis()
    val fourteenDays = 14L * 24 * 60 * 60 * 1000

    val wellness = remember(appts) { TherapistWellness.weekStats(appts, now) }

    var allQuestionnaires by remember { mutableStateOf<List<com.therapist.assistant.data.QuestionnaireResult>>(emptyList()) }
    LaunchedEffect(Unit) {
        allQuestionnaires = viewModel.getAllQuestionnairesOnce()
    }

    val trendCards = remember(allQuestionnaires, clients) {
        clients.filter { !it.isArchived }.mapNotNull { c ->
            val trends = AssessmentTrends.forClient(allQuestionnaires, c.id)
            if (trends.isEmpty()) null else c to trends
        }.take(15)
    }

    val inactiveClients = remember(clients, appts) {
        clients.filter { !it.isArchived }.mapNotNull { c ->
            val last = appts.filter { it.clientId == c.id }.maxByOrNull { it.dateTimeMillis }
            val lastMs = last?.dateTimeMillis ?: c.createdAt
            if (now - lastMs > fourteenDays) c to lastMs else null
        }.sortedBy { it.second }
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(title = "روند درمان و پیگیری", subtitle = "داشبورد تجاری", onBack = onBack)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SoftCard {
                        Text("سلامت کاری این هفته", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("جلسات انجام‌شده: ${JalaliDate.toFa(wellness.completedCount.toString())}")
                        Text("لغوشده: ${JalaliDate.toFa(wellness.cancelledCount.toString())} · برنامه‌ریزی: ${JalaliDate.toFa(wellness.scheduledCount.toString())}")
                        Text("درآمد وصول‌شده: ${JalaliDate.toFa(wellness.incomePaid.toLong().toString())} تومان")
                        Text("بدهی هفته: ${JalaliDate.toFa(wellness.unpaidTotal.toLong().toString())} تومان")
                        Text("شلوغ‌ترین روز: ${wellness.busiestDayLabel}")
                        Text(wellness.suggestionFa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                item {
                    SoftCard {
                        Text("آمار سالانه و مقایسه ماهانه", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        val yearStats = remember(appts) {
                            val cal = java.util.Calendar.getInstance()
                            val y = cal.get(java.util.Calendar.YEAR)
                            (0..11).map { m ->
                                cal.set(y, m, 1, 0, 0, 0); cal.set(java.util.Calendar.MILLISECOND, 0)
                                val start = cal.timeInMillis
                                cal.add(java.util.Calendar.MONTH, 1)
                                val end = cal.timeInMillis
                                val monthAp = appts.filter { it.dateTimeMillis in start until end && it.status != com.therapist.assistant.data.AppointmentStatus.CANCELLED }
                                val paid = monthAp.filter { it.isPaid }.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
                                m to (monthAp.size to paid)
                            }
                        }
                        yearStats.forEach { (m, pair) ->
                            val (cnt, paid) = pair
                            if (cnt > 0) {
                                Text(
                                    "ماه ${m + 1}: ${JalaliDate.toFa(cnt.toString())} جلسه · وصول ${JalaliDate.toFa(paid.toLong().toString())} ت",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        val totalYear = yearStats.sumOf { it.second.second }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "جمع وصول سال میلادی جاری: ${JalaliDate.toFa(totalYear.toLong().toString())} تومان",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                item {
                    SoftCard {
                        Text("پیگیری غیبت (۲ نوبت لغو متوالی)", fontWeight = FontWeight.Bold)
                        val absent = remember(clients, appts) { viewModel.consecutiveAbsentClients() }
                        if (absent.isEmpty()) Text("موردی نیست", style = MaterialTheme.typography.bodySmall)
                        else absent.take(12).forEach {
                            Text("• ${it.client.name} — آخرین: ${JalaliDate.formatDate(it.lastSessionMillis)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                item {
                    SoftCard {
                        Text("خلاصه", fontWeight = FontWeight.Bold)
                        Text("مراجع فعال: ${clients.count { !it.isArchived }}")
                        Text("پیگیری باز: ${followUps.size}")
                        Text("نوبت بدهکار: ${unpaid.size}")
                        Text("بدون جلسه بیش از ۱۴ روز: ${inactiveClients.size}")
                    }
                }
                item {
                    SoftCard {
                        Row {
                            Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("روند نمرات آزمون", fontWeight = FontWeight.Bold)
                        }
                        if (trendCards.isEmpty()) {
                            Text("هنوز آزمونی ثبت نشده", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                items(trendCards, key = { it.first.id }) { (c, trends) ->
                    SoftCard(onClick = { onClient(c.id) }) {
                        Text(c.name, fontWeight = FontWeight.SemiBold)
                        trends.take(3).forEach { t ->
                            val spark = AssessmentTrends.sparkline(t.points)
                            val delta = AssessmentTrends.deltaLabelFa(t.delta)
                            Text(
                                "${t.type}: $spark  (${t.latest ?: "—"}) · $delta",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                item {
                    SoftCard {
                        Text("پیگیری‌های موعددار", fontWeight = FontWeight.Bold)
                        if (followUps.isEmpty()) {
                            Text("موردی نیست", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                items(followUps, key = { it.id }) { f ->
                    SoftCard(onClick = { onClient(f.clientId) }) {
                        Text(f.clientName, fontWeight = FontWeight.SemiBold)
                        Text(f.reason, style = MaterialTheme.typography.bodySmall)
                        Text("موعد: ${JalaliDate.formatDate(f.dueMillis)}", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = { viewModel.completeFollowUp(f.id) }) {
                            Text("انجام شد")
                        }
                    }
                }
                item {
                    SoftCard {
                        Row {
                            Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("مراجع بدون جلسه ≥ ۱۴ روز", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                items(inactiveClients.take(20), key = { it.first.id }) { (c, last) ->
                    SoftCard(onClick = { onClient(c.id) }) {
                        Text(c.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "آخرین فعالیت: ${JalaliDate.formatDate(last)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            onClick = {
                                viewModel.addFollowUp(
                                    c.id,
                                    c.name,
                                    "پیگیری قطع ارتباط بیش از ۱۴ روز",
                                    now + 3L * 24 * 60 * 60 * 1000
                                )
                            }
                        ) { Text("ثبت پیگیری ۳ روزه") }
                    }
                }
            }
        }
    }
}
