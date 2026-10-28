package com.therapist.assistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.util.MonthlyReportExporter
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: AppointmentViewModel, onBack: () -> Unit) {
    val unpaid by viewModel.unpaidAppointments.collectAsState()
    val allAppts by viewModel.allAppointments.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val context = LocalContext.current
    val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }
    val cal = remember { Calendar.getInstance() }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    var monthlyIncome by remember { mutableDoubleStateOf(0.0) }
    var weeklyIncome by remember { mutableDoubleStateOf(0.0) }
    var assessmentCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val totalUnpaid = remember(unpaid) { unpaid.sumOf { it.fee } }
    val monthName = JalaliDate.formatMonthYear(cal.timeInMillis)

    LaunchedEffect(Unit) {
        monthlyIncome = viewModel.getMonthlyIncome(year, month)
        weeklyIncome = viewModel.getWeeklyIncome()
        assessmentCount = viewModel.getAssessmentCountRecent(30)
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            GradientTopBar(
                title = "گزارش‌ها",
                subtitle = monthName,
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        val sb = StringBuilder()
                        sb.appendLine("گزارش مالی — $clinicName")
                        sb.appendLine("تاریخ: ${JalaliDate.formatDateLong(System.currentTimeMillis())}")
                        sb.appendLine("درآمد $monthName: ${currency.format(monthlyIncome)} تومان")
                        sb.appendLine("مجموع بدهکاری: ${currency.format(totalUnpaid)} تومان")
                        sb.appendLine()
                        allAppts.sortedByDescending { it.dateTimeMillis }.take(50).forEach {
                            sb.appendLine("${JalaliDate.formatDateTime(it.dateTimeMillis)} | ${it.clientName} | ${it.fee.toLong()} | ${if (it.isPaid) "پرداخت" else "بدهکار"}")
                        }
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "گزارش مالی")
                                    putExtra(Intent.EXTRA_TEXT, sb.toString())
                                },
                                "اشتراک گزارش"
                            )
                        )
                    }) { Icon(Icons.Default.Share, null, tint = Color.White) }
                }
            )

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftCard {
                    Text("درآمد این ماه", style = MaterialTheme.typography.labelLarge, color = Emerald500)
                    Text("${currency.format(monthlyIncome)} تومان", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Emerald500)
                }
                SoftCard {
                    Text("مجموع بدهی‌ها", style = MaterialTheme.typography.labelLarge, color = Amber500)
                    Text("${currency.format(totalUnpaid)} تومان", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Amber500)
                    Text("${JalaliDate.toFa(unpaid.size)} جلسه تسویه‌نشده", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SoftCard {
                    Text("بدهکاران", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (unpaid.isEmpty()) {
                        Text("بدهکاری ثبت نشده", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        unpaid.groupBy { it.clientName }.forEach { (name, list) ->
                            val sum = list.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name, fontWeight = FontWeight.Medium)
                                Text("${currency.format(sum)} ت", color = Amber500, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                SoftCard {
                    Text("داشبورد مدیر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("درآمد هفتگی: ${currency.format(weeklyIncome)} ت")
                    Text("درآمد ماه: ${currency.format(monthlyIncome)} ت")
                    val cancelRate = if (allAppts.isEmpty()) 0 else
                        allAppts.count { it.status.name == "CANCELLED" } * 100 / allAppts.size
                    Text("نرخ لغو کل: $cancelRate٪ · آزمون ۳۰روز: $assessmentCount")
                    Button(
                        onClick = {
                            scope.launch {
                                val file = File(context.cacheDir, "monthly_report_${System.currentTimeMillis()}.pdf")
                                val qs = viewModel.getAllQuestionnairesOnce()
                                MonthlyReportExporter.export(
                                    file = file,
                                    clinicName = clinicName,
                                    clinicPhone = clinicPhone,
                                    clinicAddress = clinicAddress,
                                    appointments = allAppts,
                                    questionnaires = qs,
                                    year = year,
                                    month = month
                                )
                                val uri = FileProvider.getUriForFile(
                                    context, "${context.packageName}.fileprovider", file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "گزارش ماهانه PDF"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) { Text("PDF گزارش ماهانه (سربرگ مرکز)") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val file = viewModel.exportMonthlyFinancePdf(year, month)
                                    val uri = FileProvider.getUriForFile(
                                        context, "${context.packageName}.fileprovider", file
                                    )
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            },
                                            "فاکتور/گزارش مالی ماهانه"
                                        )
                                    )
                                } catch (t: Throwable) {
                                    android.widget.Toast.makeText(context, t.message ?: "خطا", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("PDF مالی ماهانه (جدول جلسات)") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val file = viewModel.exportMonthlyFinanceExcel(year, month)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            },
                                            "Excel مالی ماهانه"
                                        )
                                    )
                                } catch (t: Throwable) {
                                    android.widget.Toast.makeText(context, t.message ?: "خطا", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Excel مالی ماهانه") }
                }
                SoftCard {
                    Text("گزارش ماهانه یک‌کلیکه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("متن آماده برای اشتراک در پیام‌رسان یا ایمیل", style = MaterialTheme.typography.bodySmall)
                    val ctx = LocalContext.current
                    Button(
                        onClick = {
                            val done = allAppts.count { it.status.name == "COMPLETED" }
                            val cancel = allAppts.count { it.status.name == "CANCELLED" }
                            val body = buildString {
                                appendLine("گزارش مدیریتی — $clinicName")
                                appendLine("درآمد ماه: ${currency.format(monthlyIncome)} تومان")
                                appendLine("بدهی: ${currency.format(totalUnpaid)} تومان")
                                appendLine("جلسات انجام‌شده: $done")
                                appendLine("لغو: $cancel")
                                appendLine("نوبت تسویه‌نشده: ${unpaid.size}")
                            }
                            val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "گزارش ماهانه درمانگرنو")
                                putExtra(android.content.Intent.EXTRA_TEXT, body)
                            }
                            ctx.startActivity(android.content.Intent.createChooser(send, "اشتراک گزارش"))
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) { Text("اشتراک گزارش ماهانه") }
                }
                SoftCard {
                    Text("خلاصه جلسات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("کل جلسات: ${JalaliDate.toFa(allAppts.size)}")
                    Text("انجام‌شده: ${JalaliDate.toFa(allAppts.count { it.status.name == "COMPLETED" })}")
                    Text("لغو‌شده: ${JalaliDate.toFa(allAppts.count { it.status.name == "CANCELLED" })}")
                    val cancelRate = if (allAppts.isEmpty()) 0 else
                        (allAppts.count { it.status.name == "CANCELLED" } * 100 / allAppts.size)
                    Text("نرخ لغو: ${JalaliDate.toFa(cancelRate)}٪")
                }
                SoftCard {
                    Text("گزارش سازمانی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "درآمد و تعداد جلسه به‌تفکیک درمانگر (پلن سازمانی)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    val byTherapist = allAppts.groupBy { it.therapistName.ifBlank { "بدون نام" } }
                    if (byTherapist.isEmpty()) {
                        Text("داده‌ای نیست")
                    } else {
                        byTherapist.forEach { (name, list) ->
                            val income = list.filter { it.isPaid }.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
                            val done = list.count { it.status.name == "COMPLETED" }
                            Text("$name — ${done} جلسه انجام‌شده — ${currency.format(income)} ت", fontWeight = FontWeight.Medium)
                        }
                    }
                }
                SoftCard {
                    Text("نتایج بالینی (Outcome)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("روند PHQ-9 و DASS-21 را در پرونده هر مراجع (خط‌زمانی) ببینید. برای گزارش تجمیعی، از اشتراک گزارش مالی استفاده کنید.")
                }
            }
        }
    }
}
