package com.therapist.assistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.therapist.assistant.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: AppointmentViewModel,
    onFinished: () -> Unit
) {
    data class Page(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val desc: String)

    val pages = listOf(
        Page(Icons.Default.Favorite, "به درمانگرنو خوش آمدید", "ابزار حرفه‌ای مدیریت نوبت، پرونده و آزمون برای روان‌شناسان و مشاوران"),
        Page(Icons.Default.EventNote, "نوبت و یادآوری", "زمان‌بندی دقیق، نوبت تکراری و یادآوری پیامکی / واتساپ"),
        Page(Icons.Default.AccountBalanceWallet, "دفتر مالی", "تعرفه، تخفیف، بدهی و گزارش درآمد"),
        Page(Icons.Default.Folder, "پرونده سبک", "SOAP، پرسشنامه‌ها، اسناد و خروجی PDF"),
        Page(Icons.Default.Business, "برندینگ مطب", "نام مرکز را وارد کنید — اختیاری است؛ پیش‌فرض «مرکز گل سرخ» است."),
        Page(Icons.Default.PrivacyTip, "حریم خصوصی", "داده‌ها روی گوشی شماست. با شروع، سیاست حریم خصوصی را می‌پذیرید.")
    )
    val brandingPageIndex = 4
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    var nameEdit by remember { mutableStateOf("مرکز گل سرخ") }
    var phoneEdit by remember { mutableStateOf("") }
    var addressEdit by remember { mutableStateOf("") }

    fun finish(withSample: Boolean) {
        val name = nameEdit.trim().ifBlank { "مرکز گل سرخ" }
        viewModel.setClinicName(name)
        viewModel.setClinicPhone(phoneEdit.trim())
        viewModel.setClinicAddress(addressEdit.trim())
        viewModel.setPrivacyAccepted(true)
        if (withSample) viewModel.seedSampleData()
        viewModel.setOnboardingDone(true); viewModel.markTipsTourDone()
        viewModel.markTipsTourDone()
        onFinished()
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val item = pages[page]
            if (page == brandingPageIndex) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(item.icon, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        item.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = nameEdit,
                        onValueChange = { nameEdit = it },
                        label = { Text("نام مرکز / مطب") },
                        placeholder = { Text("مرکز گل سرخ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = { Text("اختیاری — اگر خالی بگذارید «مرکز گل سرخ» ذخیره می‌شود") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneEdit,
                        onValueChange = { phoneEdit = it },
                        label = { Text("تلفن مطب (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = addressEdit,
                        onValueChange = { addressEdit = it },
                        label = { Text("آدرس (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(item.icon, null, Modifier.size(96.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(24.dp))
                    Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(item.desc, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) { i ->
                val selected = pagerState.currentPage == i
                Surface(
                    modifier = Modifier.padding(4.dp).size(if (selected) 10.dp else 8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ) {}
            }
        }
        Spacer(Modifier.height(20.dp))
        if (pagerState.currentPage < pages.lastIndex) {
            Button(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("بعدی") }
            if (pagerState.currentPage >= brandingPageIndex) {
                TextButton(
                    onClick = { finish(withSample = false) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("رد شدن و شروع با پیش‌فرض مرکز گل سرخ") }
            }
        } else {
            Button(
                onClick = { finish(withSample = true) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("شروع با داده نمونه") }
            OutlinedButton(
                onClick = { finish(withSample = false) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("شروع بدون داده نمونه") }
            Text(
                "نام مرکز: ${nameEdit.trim().ifBlank { "مرکز گل سرخ" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
