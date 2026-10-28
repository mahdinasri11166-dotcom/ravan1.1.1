package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.ProtocolCatalog

@Composable
fun ProtocolsScreen(onBack: () -> Unit) {
    var filter by remember { mutableStateOf<String?>(null) }
    var expandedId by remember { mutableStateOf<String?>(null) }
    val all = remember { ProtocolCatalog.all() }
    val cats = remember { all.map { it.category }.distinct() }
    val list = remember(filter) { ProtocolCatalog.byCategory(filter) }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(
                title = "پروتکل‌های جلسه",
                subtitle = "سوالات راهنما · قابل اجرا از جزئیات جلسه",
                onBack = onBack
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SoftCard {
                        Text("فیلتر دسته", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        FilterChip(
                            selected = filter == null,
                            onClick = { filter = null },
                            label = { Text("همه (${all.size})") }
                        )
                        cats.forEach { c ->
                            val n = all.count { it.category == c }
                            FilterChip(
                                selected = filter == c,
                                onClick = { filter = c },
                                label = { Text("$c ($n)") },
                                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
                items(list, key = { it.title }) { p ->
                    val open = expandedId == p.title
                    SoftCard(onClick = { expandedId = if (open) null else p.title }) {
                        Text(p.title, fontWeight = FontWeight.Bold)
                        Text(
                            "${p.category} · ${p.durationHint} · ${p.steps.size} گام",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (p.linkedAssessments.isNotBlank() && p.linkedAssessments != "—") {
                            Text(
                                "آزمون پیشنهادی: ${p.linkedAssessments}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            if (open) "بستن ▲" else "سوالات راهنما و جزئیات ▼",
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (open) {
                            Spacer(Modifier.height(12.dp))
                            p.steps.forEachIndexed { i, step ->
                                Text("گام ${i + 1}: ${step.title}", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "سوالات راهنما:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                step.guideQuestions.forEach { q ->
                                    Text(
                                        "  • $q",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (step.note.isNotBlank()) {
                                    Text(
                                        "  📝 ${step.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
