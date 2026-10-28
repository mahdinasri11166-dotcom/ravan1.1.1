package com.therapist.assistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.PaymentMethod
import com.therapist.assistant.data.RecurrenceType
import com.therapist.assistant.ui.components.JalaliDatePickerDialog
import com.therapist.assistant.ui.components.ClientPickerField
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    viewModel: AppointmentViewModel,
    appointmentId: Long?,
    preselectedClientId: Long?,
    onBack: () -> Unit
) {
    val clients by viewModel.clients.collectAsState()
    var selectedClientId by remember { mutableStateOf(preselectedClientId) }
    var clientName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sessionNotes by remember { mutableStateOf("") }
    var soapS by remember { mutableStateOf("") }
    var soapO by remember { mutableStateOf("") }
    var soapA by remember { mutableStateOf("") }
    var soapP by remember { mutableStateOf("") }
    var noteTags by remember { mutableStateOf("") }
    var useSoap by remember { mutableStateOf(true) }
    var feeText by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("") }
    var isPaid by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.NONE) }
    var cancelReason by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(AppointmentStatus.SCHEDULED) }
    var recurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableIntStateOf(10) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val apptsAll by viewModel.allAppointments.collectAsState()
    val isNew = appointmentId == null || appointmentId <= 0L
    var showTimePicker by remember { mutableStateOf(false) }
    var expandedClient by remember { mutableStateOf(false) }
    val isEdit = appointmentId != null
    val defaultFee by viewModel.defaultSessionFee.collectAsState()
    val therapistName = viewModel.currentTherapistName()

    // تاریخ/ساعت از ساعت گوشی؛ هزینه از تعرفه پیش‌فرض
    LaunchedEffect(Unit) {
        if (appointmentId == null) {
            val cal = java.util.Calendar.getInstance()
            selectedDateMillis = cal.timeInMillis
            selectedHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            selectedMinute = cal.get(java.util.Calendar.MINUTE)
            if (feeText.isEmpty() && defaultFee > 0) {
                feeText = defaultFee.toLong().toString()
            }
        }
    }

    LaunchedEffect(appointmentId) {
        if (appointmentId != null) {
            viewModel.getAppointment(appointmentId)?.let { a ->
                selectedClientId = a.clientId
                clientName = a.clientName
                notes = a.notes
                sessionNotes = a.sessionNotes
                soapS = a.soapSubjective
                soapO = a.soapObjective
                soapA = a.soapAssessment
                soapP = a.soapPlan
                noteTags = a.noteTags
                useSoap = a.soapSubjective.isNotBlank() || a.soapObjective.isNotBlank() ||
                    a.soapAssessment.isNotBlank() || a.soapPlan.isNotBlank() || a.sessionNotes.isBlank()
                feeText = if (a.fee > 0) a.fee.toLong().toString() else ""
                discountText = if (a.discount > 0) a.discount.toLong().toString() else ""
                isPaid = a.isPaid
                paymentMethod = a.paymentMethod
                cancelReason = a.cancelReason
                status = a.status
                recurrence = a.recurrence
                selectedDateMillis = a.dateTimeMillis
                val cal = Calendar.getInstance().apply { timeInMillis = a.dateTimeMillis }
                selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                selectedMinute = cal.get(Calendar.MINUTE)
            }
        } else if (preselectedClientId != null) {
            viewModel.getClient(preselectedClientId)?.let {
                selectedClientId = it.id
                clientName = it.name
            }
        }
    }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("fa")) // picker internal
    val timeFormat = SimpleDateFormat("HH:mm", Locale("fa"))

    fun buildMillis(): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "ویرایش نوبت" else "نوبت جدید") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val selectedClient = clients.find { it.id == selectedClientId }
            ClientPickerField(
                clients = clients,
                selected = selectedClient,
                query = clientName,
                onQueryChange = {
                    clientName = it
                    nameError = null
                    if (selectedClientId != null && clients.find { c -> c.id == selectedClientId }?.name != it) {
                        selectedClientId = null
                    }
                },
                onSelect = {
                    selectedClientId = it.id
                    clientName = it.name
                    nameError = null
                },
                onCreateQuick = { name ->
                    viewModel.quickCreateClient(name) { id ->
                        selectedClientId = id
                        clientName = name
                        nameError = null
                    }
                }
            )
            nameError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = JalaliDate.formatDate(selectedDateMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("تاریخ") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) { Text("انتخاب") }
                }
            )
            val freeSlots = remember(apptsAll) { viewModel.suggestFreeSlots(6) }
            if (freeSlots.isNotEmpty() && isNew) {
                Text(
                    "پیشنهاد زمان خالی",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                freeSlots.forEach { slot ->
                    AssistChip(
                        onClick = {
                            selectedDateMillis = slot.startMillis
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = slot.startMillis }
                            selectedHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                            selectedMinute = cal.get(java.util.Calendar.MINUTE)
                        },
                        label = { Text(slot.labelFa) },
                        modifier = Modifier.padding(end = 6.dp, top = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            val tCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            OutlinedTextField(
                value = JalaliDate.formatTime(tCal.timeInMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("ساعت") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showTimePicker = true }) { Text("انتخاب") }
                }
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = feeText,
                onValueChange = { if (it.all { c -> c.isDigit() }) feeText = it },
                label = { Text("مبلغ جلسه / ویزیت (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("تعرفه کامل قبل از تخفیف") }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = discountText,
                onValueChange = { if (it.all { c -> c.isDigit() }) discountText = it },
                label = { Text("تخفیف (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    val fee = feeText.toDoubleOrNull() ?: 0.0
                    val disc = discountText.toDoubleOrNull() ?: 0.0
                    val net = (fee - disc).coerceAtLeast(0.0)
                    Text("مبلغ قابل پرداخت پس از تخفیف: ${net.toLong()} تومان")
                }
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isPaid, onCheckedChange = { isPaid = it })
                Text("پرداخت‌شده (مبلغ نهایی)")
            }
            if (isPaid) {
                Text("روش پرداخت", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        PaymentMethod.CASH to "نقد",
                        PaymentMethod.CARD to "کارت",
                        PaymentMethod.TRANSFER to "کارت‌به‌کارت",
                        PaymentMethod.OTHER to "سایر"
                    ).forEach { (m, label) ->
                        FilterChip(
                            selected = paymentMethod == m,
                            onClick = { paymentMethod = m },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
            if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.RESCHEDULED) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = cancelReason,
                    onValueChange = { cancelReason = it },
                    label = { Text("دلیل لغو / جابجایی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(12.dp))
            Text("وضعیت", style = MaterialTheme.typography.labelLarge)
            Row {
                listOf(
                    AppointmentStatus.SCHEDULED to "زمان‌بندی",
                    AppointmentStatus.COMPLETED to "انجام‌شده",
                    AppointmentStatus.CANCELLED to "لغو",
                    AppointmentStatus.RESCHEDULED to "جابجا"
                ).forEach { (s, label) ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(label) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            if (!isEdit) {
                Spacer(Modifier.height(12.dp))
                Text("تکرار", style = MaterialTheme.typography.labelLarge)
                Row {
                    listOf(
                        RecurrenceType.NONE to "بدون تکرار",
                        RecurrenceType.WEEKLY to "هفتگی",
                        RecurrenceType.BIWEEKLY to "دو هفته‌ای"
                    ).forEach { (r, label) ->
                        FilterChip(
                            selected = recurrence == r,
                            onClick = { recurrence = r },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("یادداشت کوتاه") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(Modifier.height(16.dp))
            Text("یادداشت جلسه", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                FilterChip(
                    selected = useSoap,
                    onClick = { useSoap = true },
                    label = { Text("قالب SOAP") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = !useSoap,
                    onClick = { useSoap = false },
                    label = { Text("یادداشت آزاد") }
                )
            }
            Spacer(Modifier.height(8.dp))

            if (useSoap) {
                OutlinedTextField(
                    value = soapS,
                    onValueChange = { soapS = it },
                    label = { Text("S — گفته‌های مراجع (Subjective)") },
                    placeholder = { Text("آنچه مراجع بیان کرده...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 5
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = soapO,
                    onValueChange = { soapO = it },
                    label = { Text("O — مشاهدات درمانگر (Objective)") },
                    placeholder = { Text("ظاهر، رفتار، علائم قابل مشاهده...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 5
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = soapA,
                    onValueChange = { soapA = it },
                    label = { Text("A — ارزیابی درمانگر (Assessment)") },
                    placeholder = { Text("تفسیر، فرضیه، تشخیص بالینی...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 5
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = soapP,
                    onValueChange = { soapP = it },
                    label = { Text("P — برنامه جلسه بعد (Plan)") },
                    placeholder = { Text("تکالیف، اهداف، برنامه پیگیری...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 5
                )
            } else {
                
            Text("قالب یادداشت سریع", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "جلسه اول" to "ارزیابی اولیه و تعیین اهداف درمان",
                    "پیگیری" to "مرور تکلیف و بررسی پیشرفت از جلسه قبل",
                    "لغو با پیام" to "مراجع اطلاع داد؛ نوبت لغو/جابجا شد"
                ).forEach { (label, body) ->
                    AssistChip(
                        onClick = {
                            sessionNotes = if (sessionNotes.isBlank()) body else sessionNotes.trim() + "\n" + body
                            if (notes.isBlank()) notes = label
                        },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("تگ سریع", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("اضطراب", "افسردگی", "خواب", "زوج", "تکلیف", "CBT", "BA").forEach { tag ->
                    val selected = noteTags.split(",").map { it.trim() }.contains(tag)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val parts = noteTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                            if (selected) parts.remove(tag) else parts.add(tag)
                            noteTags = parts.joinToString(",")
                        },
                        label = { Text(tag) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
OutlinedTextField(
                    value = sessionNotes,
                    onValueChange = { sessionNotes = it },
                    label = { Text("یادداشت آزاد جلسه") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    maxLines = 8
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = noteTags,
                onValueChange = { noteTags = it },
                label = { Text("تگ موضوعی (با ویرگول جدا کنید)") },
                placeholder = { Text("اضطراب, خواب, خانواده") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            // تگ‌های آماده
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("اضطراب", "افسردگی", "خواب", "زوج‌درمانی", "تروما", "خودمراقبتی").forEach { tag ->
                    AssistChip(
                        onClick = {
                            val tags = noteTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                            if (tag !in tags) {
                                tags.add(tag)
                                noteTags = tags.joinToString(", ")
                            }
                        },
                        label = { Text(tag) },
                        modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val clientIdSafe = selectedClientId
                    if (clientIdSafe == null || clientName.isBlank()) {
                        nameError = "انتخاب مراجع الزامی است"
                        return@Button
                    }
                    val fee = feeText.toDoubleOrNull() ?: 0.0
                    val discount = (discountText.toDoubleOrNull() ?: 0.0).coerceAtMost(fee).coerceAtLeast(0.0)
                    viewModel.saveAppointment(
                        id = appointmentId,
                        clientId = clientIdSafe,
                        clientName = clientName,
                        dateTimeMillis = buildMillis(),
                        notes = notes,
                        sessionNotes = if (useSoap) "" else sessionNotes,
                        soapS = if (useSoap) soapS else "",
                        soapO = if (useSoap) soapO else "",
                        soapA = if (useSoap) soapA else "",
                        soapP = if (useSoap) soapP else "",
                        noteTags = noteTags,
                        fee = fee,
                        discount = discount,
                        isPaid = isPaid,
                        paymentMethod = paymentMethod,
                        status = status,
                        cancelReason = cancelReason,
                        recurrence = recurrence,
                        generateInvoice = (fee - discount) > 0
                    ) { onBack() }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "ذخیره تغییرات" else "ثبت نوبت")
            }
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialMillis = selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                selectedDateMillis = it
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(selectedHour, selectedMinute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = state.hour
                    selectedMinute = state.minute
                    showTimePicker = false
                }) { Text("تأیید") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("لغو") }
            },
            text = { TimePicker(state = state) }
        )
    }
}
