package com.therapist.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun SearchScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onClient: (Long) -> Unit,
    onSession: (Long) -> Unit
) {
    var q by remember { mutableStateOf("") }
    val clients by viewModel.clients.collectAsState()
    val appts by viewModel.allAppointments.collectAsState()

    val clientHits = remember(q, clients) {
        if (q.isBlank()) emptyList()
        else clients.filter {
            it.name.contains(q, true) || it.phone.contains(q) ||
                it.nationalId.contains(q) || it.address.contains(q, true) || it.notes.contains(q, true)
        }.take(20)
    }
    val apptHits = remember(q, appts) {
        if (q.isBlank()) emptyList()
        else appts.filter {
            it.clientName.contains(q, true) || it.notes.contains(q, true) ||
                it.sessionNotes.contains(q, true) || it.noteTags.contains(q, true) ||
                it.soapSubjective.contains(q, true) || it.soapObjective.contains(q, true) || it.soapAssessment.contains(q, true) || it.soapPlan.contains(q, true) || it.therapistName.contains(q, true) ||
                it.invoiceSerial.contains(q, true)
        }.take(30)
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(title = "جستجوی سراسری", onBack = onBack)
            Text(
                "نام مراجع، تلفن، یادداشت جلسه، SOAP، تگ، شماره فاکتور",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("نام، تلفن، تگ، SOAP، فاکتور...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (clientHits.isNotEmpty()) {
                    item { Text("مراجعین (${clientHits.size})", fontWeight = FontWeight.Bold) }
                    items(clientHits, key = { "c${it.id}" }) { c ->
                        SoftCard(onClick = { onClient(c.id) }) {
                            Text(c.name, fontWeight = FontWeight.Bold)
                            if (c.phone.isNotBlank()) Text(JalaliDate.toFa(c.phone), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (apptHits.isNotEmpty()) {
                    item { Text("جلسات (${apptHits.size})", fontWeight = FontWeight.Bold) }
                    items(apptHits, key = { "a${it.id}" }) { a ->
                        SoftCard(onClick = { onSession(a.id) }) {
                            Text(a.clientName, fontWeight = FontWeight.Bold)
                            Text(JalaliDate.formatDateTime(a.dateTimeMillis), style = MaterialTheme.typography.bodySmall)
                            if (a.noteTags.isNotBlank()) Text("🏷 ${a.noteTags}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (q.isNotBlank() && clientHits.isEmpty() && apptHits.isEmpty()) {
                    item { Text("نتیجه‌ای یافت نشد", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}
