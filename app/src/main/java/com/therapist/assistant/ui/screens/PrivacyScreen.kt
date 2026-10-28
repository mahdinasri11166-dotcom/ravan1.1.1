package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun PrivacyScreen(viewModel: AppointmentViewModel, onBack: () -> Unit, requireAccept: Boolean = false) {
    val accepted by viewModel.privacyAccepted.collectAsState()
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            GradientTopBar(
                title = "حریم خصوصی و سیاست داده",
                subtitle = "مناسب انتشار کافه‌بازار / مایکت / Google Play",
                onBack = if (requireAccept && !accepted) null else onBack
            )
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftCard {
                    Text("۱. ماهیت برنامه", fontWeight = FontWeight.Bold)
                    Text("درمانگرنو ابزار مدیریت مطب است. داده‌ها به‌صورت پیش‌فرض محلی روی دستگاه ذخیره می‌شود و به سرور خارجی ارسال نمی‌گردد.")
                }
                SoftCard {
                    Text("۲. داده‌های ذخیره‌شده", fontWeight = FontWeight.Bold)
                    Text("اطلاعات مراجع، نوبت، یادداشت جلسه، آزمون، مالی جلسه و تنظیمات. پایگاه‌داده با SQLCipher رمزنگاری می‌شود.")
                }
                SoftCard {
                    Text("۳. دسترسی‌ها و اشتراک", fontWeight = FontWeight.Bold)
                    Text("اعلان برای یادآوری نوبت. اشتراک بکاپ/فاکتور/اکسل از طریق اپ‌های سیستم‌عامل و با انتخاب شما انجام می‌شود.")
                }
                SoftCard {
                    Text("۴. مسئولیت حرفه‌ای", fontWeight = FontWeight.Bold)
                    Text("رعایت محرمانگی و قوانین بر عهده کاربر است. سازنده: مهدی طریری — Madhitariri@gmail.com")
                }
                SoftCard {
                    Text("نسخه سیاست: ۱.۱.۱ — اکتبر ۲۰۲۶", style = MaterialTheme.typography.bodySmall)
                }
                if (!accepted) {
                    Button(
                        onClick = {
                            viewModel.setPrivacyAccepted(true)
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("می‌پذیرم و ادامه می‌دهم") }
                } else {
                    Text("شما این سیاست را پذیرفته‌اید.", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
