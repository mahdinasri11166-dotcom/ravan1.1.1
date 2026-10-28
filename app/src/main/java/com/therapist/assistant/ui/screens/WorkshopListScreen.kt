package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.EmptyState
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun WorkshopListScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onOpen: (Long) -> Unit,
    onCreate: () -> Unit
) {
    val list by viewModel.workshops.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("کارگاه جدید", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(
                title = "کارگاه‌های آموزشی",
                subtitle = "فرم خودکار برای مراکز",
                onBack = onBack
            )
            if (list.isEmpty()) {
                Box(Modifier.padding(16.dp)) {
                    EmptyState(
                        Icons.Default.Groups,
                        "هنوز کارگاهی ثبت نشده",
                        "شرکت‌کنندگان را از لیست مراجعین انتخاب کنید و اکسل بگیرید",
                        "ایجاد کارگاه",
                        onCreate
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { w ->
                        SoftCard(onClick = { onOpen(w.id) }) {
                            Text(w.title, fontWeight = FontWeight.Bold)
                            Text("${JalaliDate.formatDate(w.dateMillis)} · ${w.durationHours} ساعت · ${w.trainerName.ifBlank { "—" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${w.city} — ${w.centerName}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
