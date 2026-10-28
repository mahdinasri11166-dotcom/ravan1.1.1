package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.LicensePlan
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun UpgradeScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit
) {
    val isPro by viewModel.isPro.collectAsState()
    val isOrg by viewModel.isOrg.collectAsState()
    val trialStart by viewModel.trialStartMillis.collectAsState()
    val trialActive = viewModel.isTrialActive()
    val daysLeft = viewModel.trialDaysRemaining()
    val plan = viewModel.currentPlan()
    var code by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            GradientTopBar(
                title = "اشتراک و ارتقا",
                subtitle = "پلن فعلی: ${plan.titleFa}",
                onBack = onBack
            )
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftCard {
                    Text("وضعیت لایسنس", fontWeight = FontWeight.Bold)
                    when {
                        isOrg -> Text("سازمانی فعال — تیم کلینیک و گزارش مدیریتی")
                        isPro -> Text("حرفه‌ای فردی فعال")
                        trialActive -> Text("آزمایشی — $daysLeft روز مانده از ۱۴ روز")
                        else -> Text("نسخه رایگان — سقف ${LicensePlan.FREE.clientLimit} مراجع و آزمون‌های غربالگری")
                    }
                }

                listOf(
                    LicensePlan.FREE to "۵۰ مراجع · غربالگری پایه · بکاپ محلی",
                    LicensePlan.TRIAL to "همه قابلیت‌ها · ۱۴ روز · خودکار از نصب",
                    LicensePlan.PRO to "نامحدود · همه آزمون‌ها · خروجی کامل · تا ۳ درمانگر",
                    LicensePlan.ORG to "کلینیک · تا ۵۰ درمانگر · منشی · گزارش سازمانی · پیشوند کد ORG"
                ).forEach { (p, desc) ->
                    SoftCard {
                        Text(p.titleFa, fontWeight = FontWeight.Bold)
                        Text(desc, style = MaterialTheme.typography.bodySmall)
                        if (plan == p) {
                            Text("✓ پلن فعلی", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                SoftCard {
                    Text("فعال‌سازی با کد", fontWeight = FontWeight.Bold)
                    Text(
                        "کد حرفه‌ای یا سازمانی (ORG-…) را وارد کنید. اشتراک پس از تأیید کد فعال می‌شود.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("کد فعال‌سازی") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            msg = if (viewModel.unlockWithCode(code)) {
                                "✓ نسخه ویژه فعال شد"
                            } else {
                                "کد نامعتبر است"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("فعال‌سازی") }
                    msg?.let {
                        Text(
                            it,
                            color = if (it.startsWith("✓")) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "برای دریافت کد فعال‌سازی نسخه ویژه لطفاً یک ایده یا یک مشکل را به ایمیل یا کانال تلگرام تیم توسعهٔ نو (@new_development_ir) در بخش «درباره ما» گزارش دهید تا به بهبود و خدمت برنامه به شما کمک کرده باشید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (trialActive) {
                    SoftCard {
                        Text("دوره آزمایشی", fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { (daysLeft.toFloat() / 14f).coerceIn(0.05f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Text("$daysLeft روز باقی‌مانده — پس از آن به پلن رایگان برمی‌گردید مگر با کد.")
                    }
                }
            }
        }
    }
}
