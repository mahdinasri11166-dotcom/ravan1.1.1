package com.therapist.assistant.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.domain.AppChangelog

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val appVersion = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.1.1"
    } catch (_: Exception) {
        "1.1.1"
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            GradientTopBar(
                title = "درباره درمانگرنو",
                subtitle = "سامانه هوشمند کلینیک روان‌شناسی",
                onBack = onBack
            )
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalFlorist,
                        contentDescription = null,
                        Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "درمانگرنو",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "پلتفرم یکپارچه مدیریت نوبت، پرونده بالینی، آزمون‌های استاندارد و امور مالی کلینیک",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ) {
                    Text(
                        "نسخه $appVersion",
                        Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "هویت برند",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "کار ما نیست شناسایی راز گل سرخ\nکار ما شاید این است که در افسون گل سرخ شناور باشیم",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "طراحی اختصاصی با الهام از «مرکز گل سرخ» — ترکیبی از دقت بالینی و تجربه کاربری حرفه‌ای برای روان‌شناسان و مراکز مشاوره.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("تغییرات نسخه‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    AppChangelog.entries.take(3).forEach { entry ->
                        Text("نسخه ${entry.version}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        entry.items.forEach { line ->
                            Text("• $line", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }

                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("چرا درمانگرنو؟",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    val points = listOf(
                        "مدیریت نوبت و یادآوری دقیق جلسات",
                        "پرونده بالینی سبک با SOAP، خط‌زمانی و چاپ کامل",
                        "بانک آزمون‌های غربالگری و بالینی استاندارد",
                        "دفتر مالی، بدهی‌ها و صورتحساب قابل اشتراک",
                        "کارگاه آموزشی با خروجی اکسل رسمی",
                        "ذخیره‌سازی محلی و تمرکز بر حریم خصوصی"
                    )
                    points.forEach { line ->
                        Text("• $line", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                    }
                }

                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "توسعه‌دهنده",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("مهدی طریری", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "طراحی محصول، توسعه اندروید و برندینگ کلینیکی",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:Mahditariri@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "درمانگرنو — پشتیبانی / همکاری")
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "ارسال ایمیل"))
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Email, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mahditariri@gmail.com")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("https://t.me/new_development_ir")
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Send, null)
                        Spacer(Modifier.width(8.dp))
                        Text("کانال تیم توسعهٔ نو: @new_development_ir")
                    }
                }

                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "بخش حقوقی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "مالکیت معنوی",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "کلیه حقوق مادی و معنوی نرم‌افزار «درمانگرنو»، نام تجاری، رابط کاربری و مستندات آن متعلق به توسعه‌دهنده (مهدی طریری) است. هرگونه نسخه‌برداری، مهندسی معکوس، توزیع مجدد یا استفاده تجاری بدون مجوز کتبی ممنوع است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "مسئولیت حرفه‌ای و بالینی",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "درمانگرنو یک ابزار کمکی مدیریتی و ثبت اطلاعات است و جایگزین تشخیص، درمان یا قضاوت بالینی متخصص نیست. تفسیر آزمون‌ها جنبه راهنما دارد. مسئولیت صحت داده‌ها، رضایت آگاهانه مراجع، نگهداری مدارک و رعایت اخلاق حرفه‌ای بر عهده کاربر (درمانگر / مرکز) است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "حریم خصوصی و داده",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "داده‌های مراجع به‌صورت پیش‌فرض روی دستگاه شما نگهداری می‌شود. اشتراک‌گذاری خروجی‌ها (PDF، اکسل، پیام) تنها با اقدام صریح کاربر انجام می‌شود. توصیه می‌شود از قفل دستگاه، نسخه پشتیبان امن و رعایت مقررات محلی حفاظت از داده پیروی کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "محدودیت مسئولیت",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "در حدود قوانین قابل اجرا، توسعه‌دهنده بابت خسارات غیرمستقیم، از دست رفتن داده ناشی از عملکرد نادرست دستگاه کاربر، یا تصمیمات بالینی مبتنی بر خروجی نرم‌افزار مسئولیتی ندارد. استفاده از برنامه به منزله پذیرش این شرایط است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "تماس حقوقی و پشتیبانی",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "برای مجوز تجاری، گزارش سوءاستفاده یا درخواست حذف داده: Mahditariri@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "تعهد محصول",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "تمرکز ما بر پایداری، سادگی برای درمانگر پرمشغله، و احترام به محرمانگی رابطه درمانی است. بازخورد حرفه‌ای شما مسیر توسعه نسخه‌های بعدی را شکل می‌دهد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    "© درمانگرنو — همه حقوق محفوظ است",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
