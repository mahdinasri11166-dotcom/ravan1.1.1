package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.ui.components.KpiTile
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import com.therapist.assistant.ui.theme.Sky500
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.util.Calendar

@Composable
fun SelfCareScreen(viewModel: AppointmentViewModel, onBack: () -> Unit) {
    val all by viewModel.allAppointments.collectAsState()
    val now = System.currentTimeMillis()
    val weekStart = remember {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SATURDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
    }
    val weekSessions = remember(all) {
        all.filter {
            it.dateTimeMillis >= weekStart &&
                it.dateTimeMillis < weekStart + 7L * 24 * 60 * 60 * 1000 &&
                it.status != AppointmentStatus.CANCELLED
        }.sortedBy { it.dateTimeMillis }
    }
    val hours = weekSessions.size * 0.75 // ~45min
    val goal = 10
    val gaps = remember(weekSessions) {
        weekSessions.zipWithNext { a, b -> (b.dateTimeMillis - a.dateTimeMillis) / 60000.0 }
            .filter { it > 0 }
    }
    val avgGap = if (gaps.isEmpty()) 0 else gaps.average().toInt()
    val needBreak = gaps.any { it < 15 }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            GradientTopBar(title = "خودمراقبتی درمانگر", subtitle = "هفته جاری", onBack = onBack)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiTile(Modifier.weight(1f), "جلسات هفته", "${weekSessions.size}", Icons.Default.Spa, MaterialTheme.colorScheme.primary)
                    KpiTile(Modifier.weight(1f), "ساعت تقریبی", String.format("%.1f", hours), Icons.Default.Spa, Sky500)
                }
                SoftCard {
                    Text("هدف هفتگی: $goal جلسه", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { (weekSessions.size / goal.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                    Text(
                        if (weekSessions.size >= goal) "هدف محقق شد ✓" else "${goal - weekSessions.size} جلسه تا هدف",
                        color = if (weekSessions.size >= goal) Emerald500 else Amber500
                    )
                }
                SoftCard {
                    Text("میانگین فاصله بین جلسات: ${if (avgGap == 0) "—" else "$avgGap دقیقه"}", fontWeight = FontWeight.Medium)
                    if (needBreak) {
                        Spacer(Modifier.height(8.dp))
                        Text("⚠ چند جلسه با فاصله کمتر از ۱۵ دقیقه دارید — زمان استراحت بگذارید.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text("فاصله‌ها مناسب به‌نظر می‌رسد.", color = Emerald500)
                    }
                }
                SoftCard {
                    Text("یادآوری", fontWeight = FontWeight.Bold)
                    Text("بین جلسات سخت، چند دقیقه تنفس یا حرکت کوتاه برنامه‌ریزی کنید. کیفیت حضور شما بخشی از درمان است.")
                    Text("تاریخ امروز: ${JalaliDate.formatDateLong(now)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
