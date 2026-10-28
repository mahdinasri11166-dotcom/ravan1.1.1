package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.JalaliDatePickerDialog
import com.therapist.assistant.ui.components.PlanLimitBanner
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(
    viewModel: AppointmentViewModel,
    clientId: Long?,
    onBack: () -> Unit,
    onUpgrade: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var birthDateMillis by remember { mutableLongStateOf(0L) }
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthError by remember { mutableStateOf<String?>(null) }
    var education by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var nationalIdError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val isEdit = clientId != null
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("fa"))

    LaunchedEffect(clientId) {
        if (clientId != null) {
            viewModel.getClient(clientId)?.let {
                name = it.name
                phone = it.phone
                nationalId = it.nationalId
                birthDateMillis = it.birthDateMillis
                if (it.birthDateMillis > 0) {
                    val j = JalaliDate.fromMillis(it.birthDateMillis)
                    birthYear = j.year.toString()
                    birthMonth = j.month.toString()
                    birthDay = j.day.toString()
                }
                address = it.address
                education = it.education
                notes = it.notes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "ویرایش مراجع" else "افزودن مراجع") },
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
        val limitMsg = remember { viewModel.planLimitMessage() }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!isEdit && limitMsg.isNotBlank()) {
                PlanLimitBanner(
                    message = limitMsg,
                    onUpgrade = onUpgrade,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("نام مراجع *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 11) phone = it
                },
                label = { Text("شماره تلفن (اختیاری)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("۰۹۱۲...") }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nationalId,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 10) {
                        nationalId = it
                        nationalIdError = null
                    }
                },
                label = { Text("کد ملی (اختیاری)") },
                isError = nationalIdError != null,
                supportingText = nationalIdError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("۱۰ رقم") }
            )
            Spacer(Modifier.height(12.dp))
            Text("تاریخ تولد (اختیاری)", style = MaterialTheme.typography.labelLarge)
            Text("روز / ماه / سال شمسی را دستی وارد کنید یا از انتخابگر استفاده کنید", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = birthDay,
                    onValueChange = {
                        if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                            birthDay = it
                            birthError = null
                        }
                    },
                    label = { Text("روز") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("۱–۳۱") }
                )
                OutlinedTextField(
                    value = birthMonth,
                    onValueChange = {
                        if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                            birthMonth = it
                            birthError = null
                        }
                    },
                    label = { Text("ماه") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("۱–۱۲") }
                )
                OutlinedTextField(
                    value = birthYear,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            birthYear = it
                            birthError = null
                        }
                    },
                    label = { Text("سال") },
                    modifier = Modifier.weight(1.3f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("۱۳۷۰") }
                )
            }
            birthError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (birthDateMillis > 0) {
                Text("ثبت‌شده: ${JalaliDate.formatDate(birthDateMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Row {
                TextButton(onClick = {
                    val y = birthYear.toIntOrNull()
                    val m = birthMonth.toIntOrNull()
                    val d = birthDay.toIntOrNull()
                    if (y == null || m == null || d == null) {
                        birthError = "روز، ماه و سال را کامل وارد کنید"
                        return@TextButton
                    }
                    if (y !in 1300..1410 || m !in 1..12 || d !in 1..31) {
                        birthError = "مقادیر تاریخ نامعتبر است"
                        return@TextButton
                    }
                    try {
                        birthDateMillis = JalaliDate.jalaliToGregorianMillis(y, m, d)
                        birthError = null
                    } catch (_: Exception) {
                        birthError = "تاریخ قابل تبدیل نیست"
                    }
                }) { Text("اعمال تاریخ دستی") }
                TextButton(onClick = { showDatePicker = true }) { Text("انتخاب از تقویم") }
                if (birthDateMillis > 0 || birthYear.isNotEmpty()) {
                    TextButton(onClick = {
                        birthDateMillis = 0L
                        birthYear = ""; birthMonth = ""; birthDay = ""
                        birthError = null
                    }) { Text("پاک") }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("آدرس محل زندگی (اختیاری)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 3
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = education,
                onValueChange = { education = it },
                label = { Text("مدرک تحصیلی (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("دیپلم، کارشناسی، ...") }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("یادداشت") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        nameError = "نام الزامی است"
                        return@Button
                    }
                    if (phone.isNotEmpty() && !phone.matches(Regex("^0\\d{10}$"))) {
                        nameError = "شماره تلفن باید ۱۱ رقم و با ۰ شروع شود"
                        return@Button
                    }
                    if (nationalId.isNotEmpty() && nationalId.length != 10) {
                        nationalIdError = "کد ملی باید ۱۰ رقم باشد"
                        return@Button
                    }
                    var birthMs = birthDateMillis
                    val y = birthYear.toIntOrNull()
                    val m = birthMonth.toIntOrNull()
                    val d = birthDay.toIntOrNull()
                    if (y != null && m != null && d != null && y in 1300..1410 && m in 1..12 && d in 1..31) {
                        try {
                            birthMs = JalaliDate.jalaliToGregorianMillis(y, m, d)
                        } catch (_: Exception) {
                            birthError = "تاریخ تولد نامعتبر است"
                            return@Button
                        }
                    } else if (birthYear.isNotEmpty() || birthMonth.isNotEmpty() || birthDay.isNotEmpty()) {
                        if (!(birthYear.isEmpty() && birthMonth.isEmpty() && birthDay.isEmpty())) {
                            birthError = "روز، ماه و سال تولد را کامل وارد کنید"
                            return@Button
                        }
                    }
                    viewModel.saveClient(
                        id = clientId,
                        name = name,
                        phone = phone,
                        nationalId = nationalId,
                        birthDateMillis = birthMs,
                        address = address,
                        education = education,
                        notes = notes,
                        onSaved = { onBack() },
                        onLimitReached = { nameError = "سقف نسخه رایگان: حداکثر ۵۰ مراجع" }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "ذخیره تغییرات" else "ثبت مراجع")
            }
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialMillis = if (birthDateMillis > 0) birthDateMillis else System.currentTimeMillis(),
            onDismiss = { showDatePicker = false },
            onConfirm = {
                birthDateMillis = it
                val j = JalaliDate.fromMillis(it)
                birthYear = j.year.toString()
                birthMonth = j.month.toString()
                birthDay = j.day.toString()
                showDatePicker = false
            }
        )
    }
}
