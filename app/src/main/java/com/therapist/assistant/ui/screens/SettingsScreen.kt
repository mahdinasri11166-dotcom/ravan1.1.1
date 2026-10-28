package com.therapist.assistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.ui.theme.AppColorTheme
import com.therapist.assistant.viewmodel.AppointmentViewModel
import com.therapist.assistant.util.CloudBackupHelper
import com.therapist.assistant.util.BackupHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit = {},
    onUpgrade: () -> Unit = {},
    sharedBackupText: String? = null,
    onSharedBackupConsumed: () -> Unit = {}
) {
    val remindersEnabled by viewModel.remindersEnabled.collectAsState()
    val dayBefore by viewModel.remindDayBefore.collectAsState()
    val hourBefore by viewModel.remindHourBefore.collectAsState()
    val morning by viewModel.remindMorning.collectAsState()
    val pinHash by viewModel.pinHash.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val autoBackup by viewModel.autoBackupEnabled.collectAsState()
    val backupTreeUri by viewModel.backupTreeUri.collectAsState()
    val lastBackupPath by viewModel.lastBackupPath.collectAsState()
    val lastBackupSize by viewModel.lastBackupSize.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val backupPassphrase by viewModel.backupPassphrase.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val dailyCap by viewModel.dailyAppointmentCap.collectAsState()
    val ongoingNotif by viewModel.ongoingNextSessionNotif.collectAsState()
    val driveTipDone by viewModel.driveTipDone.collectAsState()
    val msgDebt by viewModel.msgTemplateDebt.collectAsState()
    val msgBirthday by viewModel.msgTemplateBirthday.collectAsState()
    val msgSession by viewModel.msgTemplate.collectAsState()
    val autoLockMinutes by viewModel.autoLockMinutes.collectAsState()
    val therapists by viewModel.therapists.collectAsState()
    val currentTherapistId by viewModel.currentTherapistId.collectAsState()
    val defaultFee by viewModel.defaultSessionFee.collectAsState()
    var feeEdit by remember(defaultFee) { mutableStateOf(if (defaultFee > 0) defaultFee.toLong().toString() else "") }
    var newTherapistName by remember { mutableStateOf("") }
    var newTherapistSecretary by remember { mutableStateOf(false) }
    val clinicName by viewModel.clinicName.collectAsState()
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val themeId by viewModel.themeId.collectAsState()
    val msgTemplate by viewModel.msgTemplate.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // همه stateها باید قبل از LaunchedEffect / launcher تعریف شوند
    var nameEdit by remember(clinicName) { mutableStateOf(clinicName.ifBlank { "مرکز گل سرخ" }) }
    var phoneEdit by remember(clinicPhone) { mutableStateOf(clinicPhone) }
    var addressEdit by remember(clinicAddress) { mutableStateOf(clinicAddress) }
    var templateEdit by remember(msgTemplate) { mutableStateOf(msgTemplate) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showEncBackup by remember { mutableStateOf(false) }
    var showEncRestore by remember { mutableStateOf(false) }
    var backupPassword by remember { mutableStateOf("") }
    var encRestoreText by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var showRestore by remember { mutableStateOf(false) }
    var restoreText by remember { mutableStateOf("") }
    var restoreMsg by remember { mutableStateOf<String?>(null) }
    var passEdit by remember(backupPassphrase) { mutableStateOf(backupPassphrase) }
    var safBackups by remember { mutableStateOf<List<com.therapist.assistant.util.CloudBackupHelper.BackupEntry>>(emptyList()) }
    var showSafList by remember { mutableStateOf(false) }
    var restorePreview by remember { mutableStateOf<com.therapist.assistant.viewmodel.AppointmentViewModel.RestorePreview?>(null) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var debtEdit by remember(msgDebt) { mutableStateOf(msgDebt) }
    var birthEdit by remember(msgBirthday) { mutableStateOf(msgBirthday) }
    var sessionTplEdit by remember(msgSession) { mutableStateOf(msgSession) }
    var capEdit by remember(dailyCap) { mutableStateOf(if (dailyCap > 0) dailyCap.toString() else "") }
    var showDriveTip by remember { mutableStateOf(false) }

    LaunchedEffect(sharedBackupText) {
        val s = sharedBackupText?.trim().orEmpty()
        if (s.isNotBlank()) {
            pendingRestoreJson = s
            restorePreview = viewModel.previewRestoreCounts(s)
            showRestore = true
            onSharedBackupConsumed()
        }
    }
    LaunchedEffect(driveTipDone) {
        if (!driveTipDone) showDriveTip = true
    }

    val treePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            CloudBackupHelper.takePersistablePermission(context, uri)
            viewModel.setBackupTreeUri(uri.toString())
            restoreMsg = "پوشه پشتیبان ذخیره شد. از این پس بکاپ در همین پوشه هم نوشته می‌شود."
        }
    }
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val file = viewModel.saveBackupOnlyToAppDir()
                    CloudBackupHelper.writeFileToUri(context, uri, file)
                    restoreMsg = "پشتیبان در مسیر انتخابی شما ذخیره شد"
                } catch (t: Throwable) {
                    restoreMsg = "خطا در ذخیره: ${t.message}"
                }
            }
        }
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            GradientTopBar(title = "تنظیمات", subtitle = "درمانگرنو", onBack = onBack)

            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SoftCard {
                    Text("ظاهر برنامه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("حالت نمایش", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("system" to "سیستم", "light" to "روشن", "dark" to "تیره").forEach { (id, label) ->
                            FilterChip(
                                selected = darkMode == id,
                                onClick = { viewModel.setDarkMode(id) },
                                label = { Text(label) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "۸ تم رنگی — انتخاب فعلی بلافاصله اعمال می‌شود",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppColorTheme.entries.forEach { theme ->
                            val selected = themeId == theme.id
                            Surface(
                                onClick = { viewModel.setThemeId(theme.id) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                border = if (selected) androidx.compose.foundation.BorderStroke(
                                    2.dp, MaterialTheme.colorScheme.primary
                                ) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    // حلقه رنگی تم
                                    Box(
                                        Modifier
                                            .size(28.dp)
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                    listOf(
                                                        theme.swatch,
                                                        theme.swatch.copy(alpha = 0.65f)
                                                    )
                                                ),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        theme.titleFa,
                                        modifier = Modifier.weight(1f),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (selected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SoftCard {
                    Text("برندینگ مطب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nameEdit, onValueChange = { nameEdit = it },
                        label = { Text("نام مرکز / مطب") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneEdit, onValueChange = { phoneEdit = it },
                        label = { Text("تلفن مطب") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = addressEdit, onValueChange = { addressEdit = it },
                        label = { Text("آدرس مطب / مرکز") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.setClinicName(nameEdit)
                            viewModel.setClinicPhone(phoneEdit)
                            viewModel.setClinicAddress(addressEdit)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("ذخیره برندینگ") }
                }

                SoftCard {
                    Text("تیم درمانگران و منشی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "هر درمانگر فقط پرونده خودش را می‌بیند. منشی به همه دسترسی دارد. همگام‌سازی چند دستگاه: خروجی بکاپ JSON را در گوشی دیگر «بازیابی» کنید (اشتراک از طریق فایل/درایو/ایمیل). همگام لحظه‌ای ابری در نسخه بعدی.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    therapists.forEach { th ->
                        FilterChip(
                            selected = currentTherapistId == th.id,
                            onClick = { viewModel.setCurrentTherapist(th.id) },
                            label = {
                                Text(
                                    th.name + if (th.role == com.therapist.assistant.data.UserRole.SECRETARY) " (منشی — بدون دسترسی یادداشت بالینی)" else ""
                                )
                            },
                            modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTherapistName,
                        onValueChange = { newTherapistName = it },
                        label = { Text("نام درمانگر / منشی جدید") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = newTherapistSecretary, onCheckedChange = { newTherapistSecretary = it })
                        Text("نقش منشی (دسترسی کامل)")
                    }
                    Button(
                        onClick = {
                            if (newTherapistName.isNotBlank()) {
                                viewModel.addTherapist(
                                    newTherapistName,
                                    if (newTherapistSecretary) com.therapist.assistant.data.UserRole.SECRETARY
                                    else com.therapist.assistant.data.UserRole.THERAPIST
                                )
                                newTherapistName = ""
                                newTherapistSecretary = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("افزودن عضو تیم") }
                    Spacer(Modifier.height(12.dp))
                    Text("تعرفه پیش‌فرض جلسه (تومان)", fontWeight = FontWeight.SemiBold)
                    Text("موقع رزرو نوبت خودکار پر می‌شود و قابل تغییر است.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = feeEdit,
                        onValueChange = { if (it.all { c -> c.isDigit() }) feeEdit = it },
                        label = { Text("مبلغ پیش‌فرض") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            viewModel.setDefaultSessionFee(feeEdit.toDoubleOrNull() ?: 0.0)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ذخیره تعرفه") }
                }


                SoftCard {
                    Text("قالب پیام یادآوری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("از {name} و {date} و {time} استفاده کنید", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = templateEdit, onValueChange = { templateEdit = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 4, shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.setMsgTemplate(templateEdit) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("ذخیره قالب پیام")
                    }
                }

                SoftCard {
                    Text("یادآوری‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("فعال‌سازی یادآوری")
                            Text("اعلان سیستم برای نوبت‌ها", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = remindersEnabled, onCheckedChange = { viewModel.setRemindersEnabled(it) })
                    }
                    if (remindersEnabled) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("یک روز قبل", Modifier.weight(1f))
                            Switch(checked = dayBefore, onCheckedChange = { viewModel.setRemindDayBefore(it) })
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("یک ساعت قبل", Modifier.weight(1f))
                            Switch(checked = hourBefore, onCheckedChange = { viewModel.setRemindHourBefore(it) })
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("صبح روز جلسه (۸ صبح)", Modifier.weight(1f))
                            Switch(checked = morning, onCheckedChange = { viewModel.setRemindMorning(it) })
                        }
                    }
                }

                SoftCard {
                    Text("امنیت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (pinHash != null) "قفل پین فعال است" else "قفل پین غیرفعال")
                            Text("برای حفاظت از اطلاعات مراجعین", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (pinHash != null) {
                            TextButton(onClick = { viewModel.setPin(null) }) { Text("حذف پین") }
                        } else {
                            Button(onClick = { showPinDialog = true }, shape = RoundedCornerShape(12.dp)) { Text("تنظیم پین") }
                        }
                    }
                    if (pinHash != null) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("ورود بیومتریک")
                                Text("اثرانگشت یا چهره پس از پین", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = biometricEnabled, onCheckedChange = { viewModel.setBiometricEnabled(it) })
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("قفل خودکار پس از بی‌فعالیتی", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "در صورت فعال بودن پین، بعد از مدت انتخابی دوباره قفل می‌شود",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0 to "خاموش", 5 to "۵ د", 10 to "۱۰ د", 15 to "۱۵ د").forEach { (mins, label) ->
                                FilterChip(
                                    selected = autoLockMinutes == mins,
                                    onClick = { viewModel.setAutoLockMinutes(mins) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                SoftCard {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("پشتیبان خودکار زمان‌بندی‌شده", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Switch(checked = autoBackup, onCheckedChange = { viewModel.setAutoBackupEnabled(it) })
                    }
                    Text(
                        "در پس‌زمینه یادآوری بکاپ محلی؛ اشتراک ابری در پلن حرفه‌ای از منوی بکاپ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SoftCard {
                    Text("سقف نوبت و اعلان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = capEdit,
                        onValueChange = { capEdit = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("سقف نوبت در روز (۰=خاموش)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    TextButton(onClick = {
                        viewModel.setDailyAppointmentCap(capEdit.toIntOrNull() ?: 0)
                        restoreMsg = "سقف روزانه ذخیره شد"
                    }) { Text("ذخیره سقف روزانه") }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("اعلان ماندگار نوبت بعدی", modifier = Modifier.weight(1f))
                        Switch(checked = ongoingNotif, onCheckedChange = { viewModel.setOngoingNextSessionNotif(it) })
                    }
                }
                SoftCard {
                    Text("قالب پیام‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("{name} {date} {time} {clinic} {amount}", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(value = sessionTplEdit, onValueChange = { sessionTplEdit = it }, label = { Text("قبل از جلسه") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    TextButton(onClick = { viewModel.setMsgTemplate(sessionTplEdit); restoreMsg = "قالب جلسه ذخیره شد" }) { Text("ذخیره قالب جلسه") }
                    OutlinedTextField(value = debtEdit, onValueChange = { debtEdit = it }, label = { Text("یادآور بدهی") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    TextButton(onClick = { viewModel.setMsgTemplateDebt(debtEdit); restoreMsg = "قالب بدهی ذخیره شد" }) { Text("ذخیره قالب بدهی") }
                    OutlinedTextField(value = birthEdit, onValueChange = { birthEdit = it }, label = { Text("تبریک تولد") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    TextButton(onClick = { viewModel.setMsgTemplateBirthday(birthEdit); restoreMsg = "قالب تولد ذخیره شد" }) { Text("ذخیره قالب تولد") }
                }
                SoftCard {
                    Text("پشتیبان‌گیری کامل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "همگام با Drive شخصی (بدون سرور)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "۱) در گوشی اپ Google Drive را نصب و وارد حساب شوید.\n" +
                            "۲) اینجا «انتخاب پوشه پشتیبان» را بزنید و پوشه‌ای داخل Drive (یا حافظه) انتخاب کنید.\n" +
                            "۳) با «ذخیره پشتیبان روی گوشی و پوشه» فایل JSON ساخته می‌شود؛ اگر پوشه Drive باشد، خودش همگام می‌شود.\n" +
                            "۴) برای ارسال دستی، از «اشتراک به Drive / ایمیل» استفاده کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "پوشه فعلی: ${CloudBackupHelper.treeDisplayName(context, backupTreeUri)}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (lastBackupPath.isNotBlank()) {
                        Text(
                            "آخرین پشتیبان: ${lastBackupPath.takeLast(48)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "حجم: ${CloudBackupHelper.formatSize(lastBackupSize)}" +
                                if (lastBackupTime > 0) " · ${com.therapist.assistant.util.JalaliDate.formatDateTime(lastBackupTime)}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passEdit,
                        onValueChange = { passEdit = it },
                        label = { Text("رمز پشتیبان (کاملاً اختیاری — خالی = بدون رمز)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    TextButton(onClick = {
                        viewModel.setBackupPassphrase(passEdit.trim())
                        restoreMsg = if (passEdit.isBlank())
                            "رمز برداشته شد — بکاپ بدون رمز ذخیره می‌شود"
                        else
                            "رمز پشتیبان ذخیره شد (اختیاری)"
                    }) { Text("ذخیره / حذف رمز پشتیبان ابری") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { treePickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("انتخاب / تغییر پوشه پشتیبان (SAF)") }
                    Spacer(Modifier.height(6.dp))
                    if (!backupTreeUri.isNullOrBlank()) {
                        TextButton(onClick = {
                            viewModel.setBackupTreeUri(null)
                            restoreMsg = "پوشه پشتیبان پاک شد"
                        }, modifier = Modifier.fillMaxWidth()) { Text("حذف مسیر پوشه ذخیره‌شده") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val (file, folderMsg) = viewModel.saveBackupToPhoneAndOptionalFolder()
                                    restoreMsg = "ذخیره شد: ${file.name}" + (folderMsg?.let { " $it" } ?: " (فقط حافظه برنامه)")
                                } catch (t: Throwable) {
                                    restoreMsg = "خطا: ${t.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("ذخیره پشتیبان روی گوشی و پوشه انتخابی") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val name = CloudBackupHelper.defaultBackupFileName()
                                    createDocLauncher.launch(name)
                                } catch (t: Throwable) {
                                    restoreMsg = t.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("ذخیره با انتخاب مسیر فایل (Create Document)") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val file = viewModel.saveBackupOnlyToAppDir()
                                    CloudBackupHelper.shareFile(context, file, "پشتیبان درمانگرنو", "application/json")
                                    restoreMsg = "آماده اشتراک — Drive یا ایمیل را انتخاب کنید"
                                } catch (t: Throwable) {
                                    restoreMsg = "خطا: ${t.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("اشتراک به Drive / ایمیل") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            restoreText = pendingRestoreJson.orEmpty()
                            if (restoreText.isNotBlank()) restorePreview = viewModel.previewRestoreCounts(restoreText)
                            showRestore = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("بازیابی از متن پشتیبان") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    safBackups = viewModel.listSafBackups()
                                    showSafList = true
                                    if (safBackups.isEmpty()) restoreMsg = "فایلی در پوشه یافت نشد"
                                } catch (t: Throwable) {
                                    restoreMsg = t.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("بازیابی از لیست پشتیبان‌های پوشه SAF") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { showEncBackup = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("ذخیره پشتیبان رمزدار (AES)") }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { showEncRestore = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("بازیابی پشتیبان رمزدار") }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("پشتیبان خودکار زمان‌بندی‌شده", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Switch(checked = autoBackup, onCheckedChange = { viewModel.setAutoBackupEnabled(it) })
                    }
                    restoreMsg?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                

                SoftCard {
                    TextButton(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isPro) "اشتراک حرفه‌ای فعال است" else "ارتقا به نسخه حرفه‌ای")
                    }
                    TextButton(onClick = onPrivacy, modifier = Modifier.fillMaxWidth()) { Text("حریم خصوصی") }
                    TextButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Info, null)
                        Spacer(Modifier.width(8.dp))
                        Text("درباره ما")
                    }
                    Text("نسخه ۱.۱.۱ — درمانگرنو", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showRestore) {
        AlertDialog(
            onDismissRequest = { showRestore = false },
            title = { Text("بازیابی پشتیبان") },
            text = {
                OutlinedTextField(
                    value = restoreText,
                    onValueChange = { restoreText = it },
                    label = { Text("متن کامل JSON پشتیبان را بچسبانید") },
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val r = viewModel.importBackupJson(restoreText)
                        restoreMsg = r.getOrElse { "خطا: ${it.message}" }
                        showRestore = false
                    }
                }) { Text("بازیابی") }
            },
            dismissButton = { TextButton(onClick = { showRestore = false }) { Text("لغو") } }
        )
    }

    
    if (showDriveTip) {
        AlertDialog(
            onDismissRequest = {
                showDriveTip = false
                viewModel.setDriveTipDone(true)
            },
            title = { Text("همگام با Drive — ۳۰ ثانیه") },
            text = {
                Text("۱) رمز پشتیبان اختیاری است.\n۲) «انتخاب پوشه» را بزنید و پوشه داخل Google Drive را انتخاب کنید.\n۳) «ذخیره پشتیبان روی گوشی و پوشه» را بزنید.\n۴) فایل در Drive همگام می‌شود.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDriveTip = false
                    viewModel.setDriveTipDone(true)
                }) { Text("متوجه شدم") }
            }
        )
    }
    if (showSafList) {
        AlertDialog(
            onDismissRequest = { showSafList = false },
            title = { Text("پشتیبان‌های پوشه") },
            text = {
                Column {
                    if (safBackups.isEmpty()) Text("موردی نیست")
                    safBackups.take(15).forEach { b ->
                        TextButton(onClick = {
                            scope.launch {
                                val r = viewModel.restoreFromSafUri(b.uri, passEdit)
                                restoreMsg = r.getOrElse { e -> e.message ?: "خطا" }
                                showSafList = false
                            }
                        }) {
                            Text("${b.name} · ${CloudBackupHelper.formatSize(b.sizeBytes)}")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSafList = false }) { Text("بستن") } }
        )
    }

if (showEncBackup) {
        AlertDialog(
            onDismissRequest = { showEncBackup = false },
            title = { Text("پشتیبان رمزدار") },
            text = {
                Column {
                    Text("اختیاری: اگر رمز بگذارید فایل AES می‌شود؛ بدون رمز همان JSON ساده است.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupPassword,
                        onValueChange = { backupPassword = it },
                        label = { Text("رمز پشتیبان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            val file = viewModel.saveEncryptedBackupToDevice(backupPassword)
                            restoreMsg = if (backupPassword.isBlank()) "پشتیبان بدون رمز ذخیره شد: ${file.name}" else "پشتیبان رمزدار ذخیره شد: ${file.name}"
                            backupPassword = ""
                            showEncBackup = false
                        } catch (e: Exception) {
                            restoreMsg = "خطا: ${e.message}"
                        }
                    }
                }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { showEncBackup = false }) { Text("لغو") } }
        )
    }
    if (showEncRestore) {
        AlertDialog(
            onDismissRequest = { showEncRestore = false },
            title = { Text("بازیابی رمزدار") },
            text = {
                Column {
                    OutlinedTextField(
                        value = encRestoreText,
                        onValueChange = { encRestoreText = it },
                        label = { Text("متن پشتیبان رمزدار (DNG1:...)") },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupPassword,
                        onValueChange = { backupPassword = it },
                        label = { Text("رمز") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val r = viewModel.importEncryptedBackup(encRestoreText, backupPassword)
                        restoreMsg = r.getOrElse { "خطا: ${it.message}" }
                        backupPassword = ""
                        encRestoreText = ""
                        showEncRestore = false
                    }
                }) { Text("بازیابی") }
            },
            dismissButton = { TextButton(onClick = { showEncRestore = false }) { Text("لغو") } }
        )
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("تنظیم پین") },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("پین ۴ تا ۶ رقمی") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPin.length >= 4) {
                        viewModel.setPin(newPin)
                        newPin = ""
                        showPinDialog = false
                    }
                }) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("لغو") }
            }
        )
    }
}
