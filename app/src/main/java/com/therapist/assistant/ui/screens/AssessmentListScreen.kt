package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.assessment.AssessmentCatalog
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.PlanLimitBanner
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun AssessmentListScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
    onUpgrade: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val isPro by viewModel.isPro.collectAsState()
    val trialActive = viewModel.isTrialActive()
    val proAccess = isPro || trialActive
    val filtered = remember(query) { AssessmentCatalog.search(query) }
    val freeIds = AppointmentViewModel.FREE_ASSESSMENT_IDS
    val chips = listOf(
        "غربالگری", "وسواس", "اضطراب", "افسردگی", "شخصیت",
        "PTSD", "ADHD", "تاب‌آوری", "بالینی"
    )

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(
                title = "بانک آزمون",
                subtitle = if (proAccess) "${filtered.size} ابزار — دسترسی کامل"
                else "${freeIds.size} غربالگری رایگان · بقیه با Pro",
                onBack = onBack
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!proAccess) {
                    item {
                        SoftCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("نسخه رایگان", fontWeight = FontWeight.Bold)
                                    Text(
                                        "آزمون‌های غربالگری پایه آزادند. برای بانک کامل و گزارش پیشرفته، آزمایشی ۱۴روزه یا حرفه‌ای را فعال کنید.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
                                Text("مشاهده ارتقا / آزمایشی")
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("جستجو: PHQ، اضطراب، شخصیت…") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chips) { chip ->
                            FilterChip(
                                selected = query == chip,
                                onClick = { query = if (query == chip) "" else chip },
                                label = { Text(chip) }
                            )
                        }
                    }
                }
                items(filtered, key = { it.id }) { def ->
                    val locked = !viewModel.canUseAssessment(def.id)
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (locked) Icons.Default.Lock else Icons.Default.Quiz,
                                contentDescription = null,
                                tint = if (locked) MaterialTheme.colorScheme.outline
                                else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(def.titleFa, fontWeight = FontWeight.Bold)
                                Text(
                                    def.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                                Text(
                                    "${def.items.size} سؤال · ${def.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (locked) onUpgrade() else onSelect(def.id)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (locked) ButtonDefaults.outlinedButtonColors()
                            else ButtonDefaults.buttonColors()
                        ) {
                            Text(if (locked) "قفل — ارتقا برای اجرا" else "شروع آزمون")
                        }
                    }
                }
            }
        }
    }
}
