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

private data class PhqQuestion(val text: String)

private val phqQuestions = listOf(
    PhqQuestion("علاقه یا لذت کم در انجام کارها"),
    PhqQuestion("احساس افسردگی، ناامیدی یا خلق پایین"),
    PhqQuestion("اختلال در به خواب رفتن، ماندن در خواب، یا خواب زیاد"),
    PhqQuestion("احساس خستگی یا کمبود انرژی"),
    PhqQuestion("کاهش اشتها یا پرخوری"),
    PhqQuestion("احساس بد نسبت به خود؛ یا اینکه شکست‌خورده‌اید یا خود/خانواده را ناامید کرده‌اید"),
    PhqQuestion("مشکل در تمرکز روی کارها (مثل خواندن یا تماشای تلویزیون)"),
    PhqQuestion("حرکت یا صحبت کردن چنان کند که دیگران متوجه شوند؛ یا برعکس بی‌قراری"),
    PhqQuestion("افکار مربوط به اینکه بهتر است بمیرید یا آسیب به خود")
)

private val options = listOf(
    "اصلاً" to 0,
    "چند روز" to 1,
    "بیش از نیمی از روزها" to 2,
    "تقریباً هر روز" to 3
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phq9Screen(
    viewModel: AppointmentViewModel,
    clientId: Long,
    onBack: () -> Unit
) {
    val answers = remember { mutableStateListOf<Int?>().apply { repeat(9) { add(null) } } }
    val score = answers.sumOf { it ?: 0 }
    val allAnswered = answers.all { it != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("پرسشنامه PHQ-9", fontWeight = FontWeight.Bold)
                        Text("طی ۲ هفته گذشته", style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "نمره کل: $score — ${severityLabel(score)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val details = answers.mapIndexed { i, v -> "Q${i + 1}:${v ?: 0}" }.joinToString(",")
                            viewModel.saveQuestionnaire(clientId, "PHQ9", score, details)
                            onBack()
                        },
                        enabled = allAnswered,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ذخیره نمره در پرونده")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "در هر مورد مشخص کنید در دو هفته گذشته چقدر اذیت شده‌اید:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            phqQuestions.forEachIndexed { index, q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (answers[index] != null)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "${index + 1}. ${q.text}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(10.dp))
                        options.forEach { (label, value) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = answers[index] == value,
                                        onClick = { answers[index] = value },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = answers[index] == value,
                                    onClick = { answers[index] = value }
                                )
                                Spacer(Modifier.width(8.dp))
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

private fun severityLabel(score: Int): String = when {
    score <= 4 -> "حداقل"
    score <= 9 -> "خفیف"
    score <= 14 -> "متوسط"
    score <= 19 -> "نسبتاً شدید"
    else -> "شدید"
}
