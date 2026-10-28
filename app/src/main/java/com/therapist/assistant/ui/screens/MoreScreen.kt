package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun MoreScreen(
    viewModel: AppointmentViewModel,
    onSearch: () -> Unit,
    onCalendar: () -> Unit,
    onReports: () -> Unit,
    onDebts: () -> Unit,
    onDocuments: () -> Unit,
    onSelfCare: () -> Unit,
    onBusinessCard: () -> Unit,
    onProtocols: () -> Unit,
    onTrends: () -> Unit,
    onWorkshops: () -> Unit,
    onAssessments: () -> Unit,
    onUpgrade: () -> Unit,
    onPrivacy: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    val isPro by viewModel.isPro.collectAsState()

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(
                title = "منو",
                subtitle = if (isPro) "نسخه حرفه‌ای" else "نسخه رایگان — ارتقا"
            )
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    SoftCard(onClick = onUpgrade) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (isPro) "اشتراک حرفه‌ای فعال" else "ارتقا به حرفه‌ای", fontWeight = FontWeight.Bold)
                                Text(if (isPro) "همه قابلیت‌ها باز است" else "مراجع نامحدود و ابزارهای بیشتر")
                            }
                            Icon(Icons.Default.ChevronLeft, null)
                        }
                    }
                }
                item { SectionLabel("روزمره") }
                item { MenuRow(Icons.Default.Search, "جستجو", onSearch) }
                item { MenuRow(Icons.Default.CalendarMonth, "تقویم", onCalendar) }
                item { MenuRow(Icons.Default.AccountBalanceWallet, "بدهی‌ها", onDebts) }
                item { MenuRow(Icons.Default.Assessment, "گزارش‌ها", onReports) }
                item { SectionLabel("حرفه‌ای") }
                item { MenuRow(Icons.Default.Description, "قالب اسناد", onDocuments) }
                item { MenuRow(Icons.Default.Checklist, "پروتکل‌های جلسه", onProtocols) }
                item { MenuRow(Icons.Default.TrendingUp, "روند درمان و پیگیری", onTrends) }
                item { MenuRow(Icons.Default.Groups, "کارگاه‌های آموزشی", onWorkshops) }
                item { MenuRow(Icons.Default.Quiz, "آزمون‌های حرفه‌ای", onAssessments) }
                item { MenuRow(Icons.Default.Spa, "خودمراقبتی", onSelfCare) }
                item { MenuRow(Icons.Default.CreditCard, "کارت ویزیت", onBusinessCard) }
                item { SectionLabel("حساب و امنیت") }
                item { MenuRow(Icons.Default.PrivacyTip, "حریم خصوصی", onPrivacy) }
                item { MenuRow(Icons.Default.Settings, "تنظیمات", onSettings) }
                item { MenuRow(Icons.Default.Info, "درباره ما", onAbout) }
            }
        }
    }
}

@Composable
private fun SectionLabel(t: String) {
    Text(t, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    SoftCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
