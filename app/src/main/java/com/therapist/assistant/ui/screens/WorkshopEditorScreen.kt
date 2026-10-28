package com.therapist.assistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.therapist.assistant.data.Client
import com.therapist.assistant.data.Workshop
import com.therapist.assistant.data.WorkshopParticipant
import com.therapist.assistant.ui.components.JalaliDatePickerDialog
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.util.WorkshopExcelExporter
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.io.File

@Composable
fun WorkshopEditorScreen(
    viewModel: AppointmentViewModel,
    workshopId: Long?,
    onBack: () -> Unit
) {
    val clients by viewModel.clients.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf("پیش از ازدواج") }
    var province by remember { mutableStateOf("خوزستان") }
    var city by remember { mutableStateOf("اهواز") }
    var center by remember { mutableStateOf(clinicName.ifBlank { "گل سرخ" }) }
    var location by remember { mutableStateOf("اهواز") }
    var duration by remember { mutableStateOf("4") }
    var topics by remember { mutableStateOf("") }
    var trainer by remember { mutableStateOf("") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var participants by remember { mutableStateOf<List<WorkshopParticipant>>(emptyList()) }
    var showPicker by remember { mutableStateOf(false) }
    var clientQuery by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf<String?>(null) }
    var showDate by remember { mutableStateOf(false) }

    LaunchedEffect(workshopId) {
        if (workshopId != null && workshopId > 0) {
            val w = viewModel.getWorkshop(workshopId)
            if (w != null) {
                title = w.title
                province = w.province
                city = w.city
                center = w.centerName
                location = w.location
                duration = w.durationHours.toString()
                topics = w.topics
                trainer = w.trainerName
                dateMillis = w.dateMillis
                participants = viewModel.getWorkshopParticipants(workshopId)
            }
        }
    }

    fun clientToParticipant(c: Client): WorkshopParticipant {
        return WorkshopParticipant(
            workshopId = workshopId ?: 0,
            clientId = c.id,
            fullName = c.name,
            birthDateText = if (c.birthDateMillis > 0) JalaliDate.formatDate(c.birthDateMillis) else "",
            education = c.education,
            nationalId = c.nationalId,
            phone = c.phone,
            preTestScore = "",
            postTestScore = ""
        )
    }

    fun buildWorkshopModel(): Workshop {
        return Workshop(
            id = workshopId ?: 0,
            title = title,
            province = province,
            city = city,
            centerName = center,
            location = location,
            durationHours = duration.toIntOrNull() ?: 4,
            topics = topics,
            trainerName = trainer,
            dateMillis = dateMillis
        )
    }

    fun exportExcel() {
        if (!viewModel.hasProAccess()) {
            statusMsg = "خروجی اکسل کارگاه در نسخه حرفه‌ای / آزمایشی ۱۴روزه فعال است."
            return
        }
        if (title.isBlank()) {
            statusMsg = "ابتدا عنوان کارگاه را وارد کنید."
            return
        }
        try {
            val w = buildWorkshopModel()
            val safeName = title.map { c -> if (c.isLetterOrDigit() || c == ' ' || c == '-' || c == '_') c else '_' }.joinToString("").trim().ifBlank { "workshop" }.take(40)
            val file = File(context.cacheDir, "workshop_${safeName}_${System.currentTimeMillis()}.xlsx")
            WorkshopExcelExporter.buildWorkbookFile(file, w, participants)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "گزارش کارگاه — $title")
                        putExtra(Intent.EXTRA_TEXT, "خروجی رسمی کارگاه «$title» — ${participants.size} شرکت‌کننده — درمانگرنو")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "اشتراک خروجی اکسل کارگاه"
                )
            )
            statusMsg = "فایل اکسل آماده شد (${participants.size} شرکت‌کننده + خلاصه آماری)"
        } catch (t: Throwable) {
            statusMsg = "خطا در ساخت خروجی: ${t.message ?: "نامشخص"}"
        }
    }
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPicker = true },
                icon = { Icon(Icons.Default.PersonAdd, null) },
                text = { Text("افزودن از مراجعین") },
                shape = RoundedCornerShape(18.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                GradientTopBar(
                    title = if (workshopId == null || workshopId == 0L) "کارگاه جدید" else "ویرایش کارگاه",
                    subtitle = "${participants.size} شرکت‌کننده",
                    onBack = onBack,
                    actions = {
                        TextButton(onClick = {
                            viewModel.saveWorkshop(
                                id = workshopId,
                                title = title,
                                province = province,
                                city = city,
                                centerName = center,
                                location = location,
                                durationHours = duration.toIntOrNull() ?: 4,
                                topics = topics,
                                trainerName = trainer,
                                dateMillis = dateMillis,
                                participants = participants
                            ) { statusMsg = "ذخیره شد" }
                        }) {
                            Text("ذخیره", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            item {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SoftCard {
                        Text("اطلاعات کارگاه", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("عنوان دوره") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = province,
                                onValueChange = { province = it },
                                label = { Text("استان") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("شهرستان") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = center,
                            onValueChange = { center = it },
                            label = { Text("مرکز مجری") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("مکان برگزاری") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it.filter { ch -> ch.isDigit() } },
                                label = { Text("مدت (ساعت)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedButton(
                                onClick = { showDate = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(JalaliDate.formatDate(dateMillis))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = trainer,
                            onValueChange = { trainer = it },
                            label = { Text("نام آموزشگر") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = topics,
                            onValueChange = { topics = it },
                            label = { Text("سرفصل‌ها") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Button(
                        onClick = { exportExcel() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = participants.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(Modifier.width(6.dp))
                        Text("خروجی اکسل حرفه‌ای (خلاصه + شرکت‌کنندگان)")
                    }

                    statusMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }

                    Text("شرکت‌کنندگان", fontWeight = FontWeight.Bold)
                }
            }

            items(
                items = participants,
                key = { p -> p.clientId ?: p.fullName.hashCode().toLong() }
            ) { p ->
                val idx = participants.indexOf(p)
                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.fullName, fontWeight = FontWeight.Bold)
                                Text(
                                    listOfNotNull(
                                        p.nationalId.takeIf { it.isNotBlank() }?.let { "کد ملی: $it" },
                                        p.birthDateText.takeIf { it.isNotBlank() }?.let { "تولد: $it" },
                                        p.education.takeIf { it.isNotBlank() },
                                        p.phone.takeIf { it.isNotBlank() }
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                participants = participants.toMutableList().also { list ->
                                    if (idx >= 0) list.removeAt(idx)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = p.preTestScore,
                                onValueChange = { v ->
                                    if (idx >= 0) {
                                        participants = participants.toMutableList().also {
                                            it[idx] = p.copy(preTestScore = v)
                                        }
                                    }
                                },
                                label = { Text("پیش‌آزمون") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = p.postTestScore,
                                onValueChange = { v ->
                                    if (idx >= 0) {
                                        participants = participants.toMutableList().also {
                                            it[idx] = p.copy(postTestScore = v)
                                        }
                                    }
                                },
                                label = { Text("پس‌آزمون") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        if (p.education.isBlank()) {
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = p.education,
                                onValueChange = { v ->
                                    if (idx >= 0) {
                                        participants = participants.toMutableList().also {
                                            it[idx] = p.copy(education = v)
                                        }
                                    }
                                },
                                label = { Text("مدرک تحصیلی") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDate) {
        JalaliDatePickerDialog(
            initialMillis = dateMillis,
            onDismiss = { showDate = false },
            onConfirm = {
                dateMillis = it
                showDate = false
            }
        )
    }

    if (showPicker) {
        val filtered = clients.filter {
            clientQuery.isBlank() ||
                it.name.contains(clientQuery, ignoreCase = true) ||
                it.phone.contains(clientQuery) ||
                it.nationalId.contains(clientQuery)
        }
        val selectedIds = participants.mapNotNull { it.clientId }.toSet()

        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("انتخاب مراجعین") },
            text = {
                Column {
                    OutlinedTextField(
                        value = clientQuery,
                        onValueChange = { clientQuery = it },
                        placeholder = { Text("جستجوی پرونده یا نام جدید...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    val canCreate = clientQuery.trim().length >= 2 &&
                        clients.none { it.name.equals(clientQuery.trim(), true) }
                    if (canCreate) {
                        TextButton(onClick = {
                            val name = clientQuery.trim()
                            viewModel.quickCreateClient(name) { id ->
                                val c = Client(
                                    id = id,
                                    name = name,
                                    notes = "پرونده سریع — تکمیل نشده"
                                )
                                participants = participants + clientToParticipant(c.copy(id = id))
                                clientQuery = ""
                            }
                        }) {
                            Text("ساخت پرونده «${clientQuery.trim()}» و افزودن")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(items = filtered, key = { it.id }) { c ->
                            val selected = c.id in selectedIds
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selected,
                                    onCheckedChange = { checked ->
                                        participants = if (checked) {
                                            participants + clientToParticipant(c)
                                        } else {
                                            participants.filter { it.clientId != c.id }
                                        }
                                    }
                                )
                                Column {
                                    Text(c.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        listOf(c.nationalId, c.phone)
                                            .filter { it.isNotBlank() }
                                            .joinToString(" · "),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text("تأیید") }
            }
        )
    }
}
