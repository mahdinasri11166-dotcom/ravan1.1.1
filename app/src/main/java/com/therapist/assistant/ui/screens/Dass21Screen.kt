package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.viewmodel.AppointmentViewModel

// Shortened representative DASS-21 items (7 per scale) for practical use
private data class DassItem(val text: String, val scale: Char) // D A S

private val items = listOf(
    DassItem("احساس می‌کردم سخت است آرام بگیرم", 'S'),
    DassItem("دهانم خشک می‌شد", 'A'),
    DassItem("هیچ احساس مثبتی نمی‌توانستم داشته باشم", 'D'),
    DassItem("تنفسم مشکل داشت (مثلاً تند تند)", 'A'),
    DassItem("شروع کارها برایم سخت بود", 'D'),
    DassItem("به موقعیت‌های مختلف بیش از حد واکنش نشان می‌دادم", 'S'),
    DassItem("لرزش داشتم (مثلاً دست‌ها)", 'A'),
    DassItem("احساس می‌کردم انرژی زیادی صرف کرده‌ام", 'S'),
    DassItem("نگران موقعیت‌هایی بودم که ممکن است وحشت‌زده شوم", 'A'),
    DassItem("چیزی برای امیدواری نمی‌دیدم", 'D'),
    DassItem("خودم را آشفته احساس می‌کردم", 'S'),
    DassItem("آرام شدن برایم سخت بود", 'S'),
    DassItem("دل‌مرده و افسرده بودم", 'D'),
    DassItem("نسبت به هر چیزی که مرا از کار بازمی‌داشت ناشکیبا بودم", 'S'),
    DassItem("احساس ترس داشتم", 'A'),
    DassItem("برای هیچ چیز نمی‌توانستم اشتیاق داشته باشم", 'D'),
    DassItem("احساس می‌کردم به عنوان فرد ارزشمند نیستم", 'D'),
    DassItem("حس می‌کردم بسیار حساس و زودرنج هستم", 'S'),
    DassItem("بدون فعالیت بدنی متوجه تپش قلب می‌شدم", 'A'),
    DassItem("بدون دلیل موجه ترسیده بودم", 'A'),
    DassItem("احساس می‌کردم زندگی بی‌معناست", 'D')
)

private val options = listOf("اصلاً" to 0, "کم" to 1, "متوسط" to 2, "زیاد" to 3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dass21Screen(viewModel: AppointmentViewModel, clientId: Long, onBack: () -> Unit) {
    val answers = remember { mutableStateListOf<Int?>().apply { repeat(21) { add(null) } } }
    val d = items.mapIndexed { i, it -> if (it.scale == 'D') answers[i] ?: 0 else 0 }.sum() * 2
    val a = items.mapIndexed { i, it -> if (it.scale == 'A') answers[i] ?: 0 else 0 }.sum() * 2
    val s = items.mapIndexed { i, it -> if (it.scale == 'S') answers[i] ?: 0 else 0 }.sum() * 2
    val allDone = answers.all { it != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DASS-21", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(Modifier.padding(16.dp)) {
                    Text("D=${d} · A=${a} · S=${s}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(
                        onClick = {
                            viewModel.saveQuestionnaire(clientId, "DASS21", d + a + s, "D=$d,A=$a,S=$s")
                            onBack()
                        },
                        enabled = allDone,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ذخیره در پرونده") }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("طی هفته گذشته هر مورد چقدر برایتان صدق می‌کرد؟", color = MaterialTheme.colorScheme.onSurfaceVariant)
            items.forEachIndexed { index, item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${index + 1}. ${item.text} (${item.scale})", fontWeight = FontWeight.Medium)
                        options.forEach { (label, value) ->
                            Row(
                                Modifier.fillMaxWidth().selectable(selected = answers[index] == value, onClick = { answers[index] = value }, role = Role.RadioButton).padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = answers[index] == value, onClick = { answers[index] = value })
                                Text("$label ($value)")
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(72.dp))
        }
    }
}
