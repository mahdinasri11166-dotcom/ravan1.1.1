package com.therapist.assistant.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.QuestionnaireResult
import com.therapist.assistant.util.ProtocolCatalog
import com.therapist.assistant.util.ClinicalWorksheets
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import com.therapist.assistant.assessment.AssessmentCatalog
import com.therapist.assistant.util.ClientFileExporter
import com.therapist.assistant.util.JalaliDate
import kotlinx.coroutines.launch
import java.io.File
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.*

private sealed class TimelineItem(val time: Long) {
    data class Session(val appt: Appointment) : TimelineItem(appt.dateTimeMillis)
    data class Questionnaire(val q: QuestionnaireResult) : TimelineItem(q.createdAt)
    data class Homework(val h: com.therapist.assistant.data.HomeworkTask) : TimelineItem(h.createdAt)
}

private data class TrendSeries(
    val type: String,
    val label: String,
    val scores: List<Int>,
    val times: List<Long>,
    val color: Color
)

@Composable
private fun MultiTrendChart(
    series: List<TrendSeries>,
    modifier: Modifier = Modifier
) {
    if (series.isEmpty()) {
        Text("آزمونی برای نمودار نیست", style = MaterialTheme.typography.bodySmall)
        return
    }
    val allScores = series.flatMap { it.scores }
    val minY = (allScores.minOrNull() ?: 0).toFloat()
    val maxY = (allScores.maxOrNull() ?: 1).toFloat().coerceAtLeast(minY + 1f)
    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(8.dp)
        ) {
            val w = size.width
            val h = size.height
            val grid = Color(0xFFE2E8F0)
            for (i in 0..4) {
                val y = h * i / 4f
                drawLine(grid, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
            // محور y برچسب‌ها با خطوط
            series.forEach { s ->
                if (s.scores.isEmpty()) return@forEach
                if (s.scores.size == 1) {
                    val x = w / 2f
                    val y = h - (s.scores[0] - minY) / (maxY - minY) * h
                    drawCircle(s.color, radius = 7f, center = Offset(x, y))
                    return@forEach
                }
                val step = w / (s.scores.size - 1).coerceAtLeast(1)
                for (i in 0 until s.scores.size - 1) {
                    val x1 = i * step
                    val x2 = (i + 1) * step
                    val y1 = h - (s.scores[i] - minY) / (maxY - minY) * h
                    val y2 = h - (s.scores[i + 1] - minY) / (maxY - minY) * h
                    drawLine(s.color, Offset(x1, y1), Offset(x2, y2), strokeWidth = 4.5f)
                }
                s.scores.forEachIndexed { i, sc ->
                    val x = i * step
                    val y = h - (sc - minY) / (maxY - minY) * h
                    drawCircle(Color.White, radius = 6f, center = Offset(x, y))
                    drawCircle(s.color, radius = 4.5f, center = Offset(x, y))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        series.forEach { s ->
            val first = s.scores.firstOrNull()
            val last = s.scores.lastOrNull()
            val delta = if (first != null && last != null && s.scores.size >= 2) last - first else null
            val deltaFa = when {
                delta == null -> "—"
                delta > 0 -> "↑ +" + delta
                delta < 0 -> "↓ " + delta
                else -> "بدون تغییر"
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(s.color))
                Spacer(Modifier.width(8.dp))
                Text(s.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text(
                    "آخرین: ${last ?: "—"} · $deltaFa · ${s.scores.size} نمره",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    viewModel: AppointmentViewModel,
    clientId: Long,
    onBack: () -> Unit,
    onEditClient: () -> Unit,
    onAddAppointment: () -> Unit,
    onEditAppointment: (Long) -> Unit,
    onPhq9: () -> Unit = {},
    onDass21: () -> Unit = {},
    onDocuments: () -> Unit = {}
) {
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var birthDateMillis by remember { mutableLongStateOf(0L) }
    var address by remember { mutableStateOf("") }
    var clientNotes by remember { mutableStateOf("") }

    val appointments by viewModel.getAppointmentsForClient(clientId)
        .collectAsState(initial = emptyList())
    val questionnaires by viewModel.getQuestionnaires(clientId)
        .collectAsState(initial = emptyList())
    val homeworkList by viewModel.homeworkForClient(clientId)
        .collectAsState(initial = emptyList())
    val clinicName by viewModel.clinicName.collectAsState()
    val isSecretary = remember { viewModel.isSecretaryRole() }
    // clinical UI gated by isSecretary in pattern/alerts section already
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

        val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }

    LaunchedEffect(clientId) {
        viewModel.getClient(clientId)?.let { clientNotes = it.notes }
    }
    LaunchedEffect(clientId) {
        viewModel.getClient(clientId)?.let {
            clientName = it.name
            clientPhone = it.phone
            nationalId = it.nationalId
            birthDateMillis = it.birthDateMillis
            address = it.address
            clientNotes = it.notes
        }
    }

    val timeline = remember(appointments, questionnaires, homeworkList) {
        buildList {
            appointments.forEach { add(TimelineItem.Session(it)) }
            questionnaires.forEach { add(TimelineItem.Questionnaire(it)) }
            homeworkList.forEach { add(TimelineItem.Homework(it)) }
            // trends computed in UI from questionnaires
        }.sortedByDescending { it.time }
    }

    val patterns = remember(appointments) { detectPatterns(appointments) }
    val smartAdvice = remember(appointments, clientId) {
        viewModel.smartReminderAdvice(clientId)
    }
    val trendSeries = remember(questionnaires) {
        val colors = listOf(
            Color(0xFF0284C7), // sky
            Color(0xFF059669), // emerald
            Color(0xFF7C3AED), // violet
            Color(0xFFEA580C), // orange
            Color(0xFFDB2777), // pink
            Color(0xFF0D9488), // teal
            Color(0xFFCA8A04), // amber
            Color(0xFF4F46E5)  // indigo
        )
        questionnaires
            .groupBy { it.type }
            .entries
            .mapIndexed { index, (type, list) ->
                val sorted = list.sortedBy { it.createdAt }.takeLast(12)
                TrendSeries(
                    type = type,
                    label = AssessmentCatalog.byId(type)?.titleFa ?: type,
                    scores = sorted.map { it.score },
                    times = sorted.map { it.createdAt },
                    color = colors[index % colors.size]
                )
            }
            .filter { it.scores.isNotEmpty() }
            .sortedByDescending { it.scores.size }
    }

    val totalPaid = appointments.filter { it.isPaid }.sumOf { it.fee }
    val totalUnpaid = appointments.filter { !it.isPaid && it.status != AppointmentStatus.CANCELLED }.sumOf { it.fee }
    val completedCount = appointments.count { it.status == AppointmentStatus.COMPLETED }
    val cancelledCount = appointments.count { it.status == AppointmentStatus.CANCELLED }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddAppointment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("نوبت جدید", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                GradientTopBar(
                    title = clientName.ifEmpty { "مراجع" },
                    subtitle = clientPhone.ifBlank { null },
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                val client = viewModel.getClient(clientId) ?: return@launch
                                val appts = appointments
                                val qs = questionnaires
                                val workshops = viewModel.getClientWorkshopLinks(clientId)
                                val file = File(context.cacheDir, "client_file_${clientId}_${System.currentTimeMillis()}.pdf")
                                try {
                                    ClientFileExporter.exportPdf(
                                        file = file,
                                        clinicName = clinicName,
                                        clinicPhone = clinicPhone,
                                        clinicAddress = clinicAddress,
                                        client = client,
                                        appointments = appts,
                                        questionnaires = qs,
                                        workshopLinks = workshops
                                    )
                                    val uri = FileProvider.getUriForFile(
                                        context, "${context.packageName}.fileprovider", file
                                    )
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                putExtra(Intent.EXTRA_SUBJECT, "پرونده ${client.name}")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            },
                                            "چاپ / اشتراک پرونده کامل"
                                        )
                                    )
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "خطا در ساخت PDF: ${e.message}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Print, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = onPhq9) {
                            Icon(Icons.Default.Assignment, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = onDass21) {
                            Icon(Icons.Default.Psychology, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = onDocuments) {
                            Icon(Icons.Default.Description, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = onEditClient) {
                            Icon(Icons.Default.Edit, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                )
            }
            // Hero profile card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(56.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    clientName.take(1).ifEmpty { "؟" },
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(clientName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                if (nationalId.isNotBlank()) Text("کد ملی: $nationalId", style = MaterialTheme.typography.bodyMedium)
                                if (birthDateMillis > 0) Text("تولد: ${JalaliDate.formatDate(birthDateMillis)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        if (address.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text(address, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        if (clientNotes.isNotBlank()) {
                            Text(clientNotes, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMini(Modifier.weight(1f), "$completedCount", "انجام‌شده", MaterialTheme.colorScheme.secondary)
                    StatMini(Modifier.weight(1f), "$cancelledCount", "لغو", MaterialTheme.colorScheme.error)
                    StatMini(Modifier.weight(1f), currency.format(totalPaid), "دریافت", MaterialTheme.colorScheme.primary)
                    StatMini(Modifier.weight(1f), currency.format(totalUnpaid), "بدهی", Color(0xFFE65100))
                }
            }

            // Pattern alerts + smart reminder
            if (patterns.isNotEmpty() || smartAdvice.highRisk) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (smartAdvice.highRisk) {
                            SoftCard {
                                Text("یادآوری هوشمند", fontWeight = FontWeight.Bold)
                                Text(smartAdvice.messageFa, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        patterns.forEach { pattern ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(10.dp))
                                    Text(pattern, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // PHQ-9 trend chart
            if (trendSeries.isNotEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShowChart, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("روند آزمون‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                val jumps = trendSeries.mapNotNull { s ->
                                    if (s.scores.size < 2) return@mapNotNull null
                                    val d = s.scores.last() - s.scores[s.scores.size - 2]
                                    if (d >= 5) s.label to d else null
                                }
                                if (jumps.isNotEmpty()) {
                                    Text(
                                        "⚠️ پیشنهاد پیگیری (غیرتشخیصی): افزایش قابل توجه نمره — " +
                                            jumps.joinToString { "${it.first} (+${it.second})" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Text("${trendSeries.size} آزمون", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(12.dp))
                            MultiTrendChart(series = trendSeries, modifier = Modifier.fillMaxWidth().height(180.dp))
                            Spacer(Modifier.height(8.dp))
                            trendSeries.take(6).forEach { s ->
                                val last = s.scores.last()
                                val delta = if (s.scores.size >= 2) s.scores.last() - s.scores[s.scores.size - 2] else 0
                                val deltaText = when {
                                    s.scores.size < 2 -> "—"
                                    delta < 0 -> "↓ ${-delta}"
                                    delta > 0 -> "↑ $delta"
                                    else -> "="
                                }
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(10.dp).clip(CircleShape).background(s.color))
                                    Spacer(Modifier.width(8.dp))
                                    Text(s.label, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                    Text("آخرین: $last  $deltaText", style = MaterialTheme.typography.labelMedium, color = s.color)
                                }
                                ProtocolCatalog.suggestForAssessment(s.type, last)?.let { proto ->
                                    Text(
                                        "پروتکل پیشنهادی: ${proto.title}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 18.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                item {
                    OutlinedCard(
                        onClick = onPhq9,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("ثبت اولین PHQ-9", fontWeight = FontWeight.SemiBold)
                                Text("نمودار روند بعد از ثبت نمرات نمایش داده می‌شود",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronLeft, null)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Timeline header
            
            item {
                SoftCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("تکالیف خانه", fontWeight = FontWeight.Bold)
                    val pending = homeworkList.count { !it.isDone }
                    val done = homeworkList.count { it.isDone }
                    Text("باز: $pending  ·  انجام‌شده: $done", style = MaterialTheme.typography.bodySmall)
                    homeworkList.filter { !it.isDone }.take(5).forEach { h ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(h.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { viewModel.completeHomework(h.id) }) { Text("انجام شد") }
                        }
                    }
                    if (homeworkList.isEmpty()) {
                        Text("تکلیفی ثبت نشده — از جزئیات جلسه می‌توانید ثبت کنید.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                var showThought by remember { mutableStateOf(false) }
                var showBa by remember { mutableStateOf(false) }
                var showSafety by remember { mutableStateOf(false) }
                SoftCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("ابزار بالینی جلسه", fontWeight = FontWeight.Bold)
                    Text(
                        "فرم فکر CBT · جدول BA · برنامه ایمنی (غیرتشخیصی)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        AssistChip(onClick = { showThought = true }, label = { Text("فرم فکر") })
                        AssistChip(onClick = { showBa = true }, label = { Text("فعالیت BA") })
                        AssistChip(onClick = { showSafety = true }, label = { Text("برنامه ایمنی") })
                    }
                    val safety = remember(clientNotes) { ClinicalWorksheets.extractSafetyPlan(clientNotes) }
                    if (safety != null) {
                        Text(
                            "برنامه ایمنی ثبت‌شده دارد ✓",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                if (showThought) {
                    ClinicalFormDialog(
                        title = "فرم فکر CBT",
                        fields = listOf(
                            "موقعیت", "هیجان", "فکر خودکار", "شواهد له",
                            "شواهد علیه", "پاسخ متعادل", "هیجان بعد"
                        ),
                        onDismiss = { showThought = false },
                        onSave = { vals ->
                            val text = ClinicalWorksheets.thoughtRecord(
                                vals[0], vals[1], vals[2], vals[3], vals[4], vals[5], vals[6]
                            )
                            viewModel.appendClientNotes(clientId, text)
                            showThought = false
                        }
                    )
                }
                if (showBa) {
                    ClinicalFormDialog(
                        title = "جدول فعالیت BA",
                        fields = listOf("روز", "فعالیت", "لذت ۰–۱۰", "تسلط ۰–۱۰", "انجام شد؟ (بله/خیر)"),
                        onDismiss = { showBa = false },
                        onSave = { vals ->
                            val text = ClinicalWorksheets.baActivity(
                                vals[0], vals[1], vals[2], vals[3],
                                vals.getOrNull(4)?.contains("ب") == true
                            )
                            viewModel.appendClientNotes(clientId, text)
                            showBa = false
                        }
                    )
                }
                if (showSafety) {
                    ClinicalFormDialog(
                        title = "برنامه ایمنی",
                        fields = listOf(
                            "نشانه‌های هشدار", "مهارت‌های آرام‌سازی",
                            "افراد قابل تماس", "اورژانس/متخصص", "ایمن‌سازی محیط"
                        ),
                        onDismiss = { showSafety = false },
                        onSave = { vals ->
                            val text = ClinicalWorksheets.safetyPlan(
                                vals[0], vals[1], vals[2], vals[3], vals[4]
                            )
                            viewModel.appendClientNotes(clientId, text)
                            showSafety = false
                        }
                    )
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("خط‌زمانی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text("${timeline.size}", Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (timeline.isEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Timeline, null, Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("هنوز رویدادی ثبت نشده", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(timeline, key = {
                when (it) {
                    is TimelineItem.Session -> "s-${it.appt.id}"
                    is TimelineItem.Questionnaire -> "q-${it.q.id}"
                    is TimelineItem.Homework -> "h-${it.h.id}"
                }
            }) { item ->
                when (item) {
                    is TimelineItem.Session -> SessionTimelineCard(
                        appt = item.appt,
                        currency = currency,
                        onClick = { onEditAppointment(item.appt.id) }
                    )
                    is TimelineItem.Questionnaire -> {
                        val prev = questionnaires.filter { it.type == item.q.type && it.createdAt < item.q.createdAt }
                            .maxByOrNull { it.createdAt }?.score
                        QuestionnaireTimelineCard(
                            item.q,
                            prevScore = prev,
                            onFollowUp = {
                                viewModel.addFollowUp(
                                    clientId,
                                    clientName,
                                    "جهش نمره ${item.q.type}: ${prev} → ${item.q.score}",
                                    System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000
                                )
                            }
                        )
                    }
                    is TimelineItem.Homework -> HomeworkTimelineCard(
                        item.h,
                        onDone = { viewModel.completeHomework(item.h.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMini(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun SessionTimelineCard(appt: Appointment, currency: NumberFormat, onClick: () -> Unit) {
    val statusColor = when (appt.status) {
        AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
        AppointmentStatus.RESCHEDULED -> Color(0xFF6A1B9A)
    }
    val statusLabel = when (appt.status) {
        AppointmentStatus.SCHEDULED -> "زمان‌بندی"
        AppointmentStatus.COMPLETED -> "انجام‌شده"
        AppointmentStatus.CANCELLED -> "لغو"
        AppointmentStatus.RESCHEDULED -> "جابجا"
    }

    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        // Timeline rail
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(statusColor))
            Box(Modifier.width(2.dp).height(8.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(10.dp))
        Card(
            onClick = onClick,
            modifier = Modifier.weight(1f).animateContentSize(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(JalaliDate.formatDateTime(appt.dateTimeMillis), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(statusLabel, Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
                if (appt.fee > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (appt.isPaid) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            null, Modifier.size(16.dp),
                            tint = if (appt.isPaid) MaterialTheme.colorScheme.secondary else Color(0xFFE65100)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${currency.format(appt.fee)} تومان — ${if (appt.isPaid) "پرداخت‌شده" else "بدهکار"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (appt.noteTags.isNotBlank()) {
                    Text("🏷 ${appt.noteTags}", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                val soapBits = listOfNotNull(
                    appt.soapSubjective.takeIf { it.isNotBlank() }?.let { "S: ${it.take(80)}" },
                    appt.soapAssessment.takeIf { it.isNotBlank() }?.let { "A: ${it.take(80)}" },
                    appt.soapPlan.takeIf { it.isNotBlank() }?.let { "P: ${it.take(60)}" },
                    appt.sessionNotes.takeIf { it.isNotBlank() }?.take(100)
                )
                if (soapBits.isNotEmpty()) {
                    Text(soapBits.joinToString("\n"), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
                if (appt.cancelReason.isNotBlank()) {
                    Text("دلیل: ${appt.cancelReason}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun QuestionnaireTimelineCard(q: QuestionnaireResult, prevScore: Int? = null, onFollowUp: (() -> Unit)? = null) {
    SoftCard {
        val deltaText = when {
            prevScore == null -> "اولین نمره این آزمون"
            q.score > prevScore -> "▲ +${q.score - prevScore} نسبت به قبل ($prevScore)"
            q.score < prevScore -> "▼ ${q.score - prevScore} نسبت به قبل ($prevScore)"
            else -> "بدون تغییر نسبت به قبل ($prevScore)"
        }
        val suggest = if (prevScore != null && q.score - prevScore >= 5) {
            ProtocolCatalog.suggestForAssessment(q.type, q.score)
        } else if (prevScore == null) {
            ProtocolCatalog.suggestForAssessment(q.type, q.score)
        } else null
        Text(q.type, fontWeight = FontWeight.Bold)
        Text("نمره: ${q.score}  ·  $deltaText", style = MaterialTheme.typography.bodyMedium)
        Text(JalaliDate.formatDateTime(q.createdAt), style = MaterialTheme.typography.labelSmall)
        if (suggest != null) {
            Text(
                "پیشنهاد: ${suggest.title} — پیگیری در جلسه بعد توصیه می‌شود (غیرتشخیصی)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (prevScore != null && q.score - prevScore >= 5 && onFollowUp != null) {
            TextButton(onClick = { onFollowUp() }) {
                Text("ثبت پیگیری")
            }
        }
    }
}


private fun detectPatterns(appointments: List<Appointment>): List<String> {
    val result = mutableListOf<String>()
    val sorted = appointments.sortedByDescending { it.dateTimeMillis }

    // 3 consecutive cancellations
    var streak = 0
    for (a in sorted) {
        if (a.status == AppointmentStatus.CANCELLED) {
            streak++
            if (streak >= 3) {
                result.add("۳ جلسه لغو پشت‌سرهم — نیاز به پیگیری")
                break
            }
        } else break
    }

    // unpaid older than ~2 months
    val twoMonthsAgo = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000
    val oldDebt = sorted.filter {
        !it.isPaid && it.status != AppointmentStatus.CANCELLED && it.dateTimeMillis < twoMonthsAgo && it.fee > 0
    }
    if (oldDebt.isNotEmpty()) {
        val sum = oldDebt.sumOf { it.fee }
        val fmt = NumberFormat.getNumberInstance(Locale("fa"))
        result.add("بدهی بیش از ۲ ماه: ${fmt.format(sum)} تومان (${oldDebt.size} جلسه)")
    }

    // high cancel rate
    if (appointments.size >= 5) {
        val rate = appointments.count { it.status == AppointmentStatus.CANCELLED }.toFloat() / appointments.size
        if (rate >= 0.4f) {
            result.add("نرخ لغو بالا (${(rate * 100).toInt()}٪) — بررسی برنامه زمانی")
        }
    }
    return result
}


@Composable
private fun HomeworkTimelineCard(h: com.therapist.assistant.data.HomeworkTask, onDone: () -> Unit) {
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (h.isDone) "✓ تکلیف انجام‌شده" else "○ تکلیف باز",
                    fontWeight = FontWeight.SemiBold,
                    color = if (h.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
                Text(h.title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    JalaliDate.formatDateTime(h.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!h.isDone) {
                TextButton(onClick = onDone) { Text("انجام شد") }
            }
        }
    }
}


@Composable
private fun ClinicalFormDialog(
    title: String,
    fields: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val values = remember { fields.map { "" }.toMutableStateList() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fields.forEachIndexed { i, label ->
                    OutlinedTextField(
                        value = values[i],
                        onValueChange = { values[i] = it },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = i != 0
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(values.toList()) }) { Text("ذخیره در پرونده") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
