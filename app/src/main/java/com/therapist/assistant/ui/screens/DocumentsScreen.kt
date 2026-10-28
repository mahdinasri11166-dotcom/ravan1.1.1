package com.therapist.assistant.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.DocumentTemplates
import com.therapist.assistant.util.PdfExporter
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.io.File

@Composable
fun DocumentsScreen(
    viewModel: AppointmentViewModel,
    clientId: Long?,
    onBack: () -> Unit
) {
    val clients by viewModel.clients.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val context = LocalContext.current
    var selectedId by remember { mutableStateOf(clientId) }
    val client = clients.find { it.id == selectedId }
    val appts by viewModel.allAppointments.collectAsState()
    val sessionCount = appts.count { it.clientId == selectedId }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<String?>(null) }
    val categories = remember { DocumentTemplates.catalog.map { it.category }.distinct() }

    val filteredClients = remember(clients, query) {
        val q = query.trim()
        if (q.isEmpty()) clients.filter { !it.isArchived }
        else clients.filter { !it.isArchived && it.name.contains(q, true) }
    }

    val templates = remember(filter, query) {
        val base = if (filter == null) DocumentTemplates.catalog
        else DocumentTemplates.catalog.filter { it.category == filter }
        val q = query.trim()
        if (q.isEmpty()) base
        else base.filter {
            it.title.contains(q, true) ||
                it.id.contains(q, true) ||
                it.title.split(" ").any { w -> w.startsWith(q, true) } ||
                it.title.take(q.length).equals(q, true)
        }
    }

    fun textOf(docId: String): String {
        val c = client ?: return ""
        return DocumentTemplates.buildText(
            id = docId,
            clientName = c.name,
            phone = c.phone,
            clinicName = clinicName,
            sessionCount = sessionCount
        )
    }

    fun shareText(docId: String, title: String) {
        if (client == null) {
            Toast.makeText(context, "ابتدا مراجع را انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, textOf(docId))
                },
                "اشتراک $title"
            )
        )
    }

    fun printPdf(docId: String, title: String) {
        if (client == null) {
            Toast.makeText(context, "ابتدا مراجع را انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val c = client ?: return
            val body = textOf(docId)
            val file = File(context.cacheDir, "doc_${docId}_${System.currentTimeMillis()}.pdf")
            PdfExporter.writeSimplePdf(
                file = file,
                title = "$title — ${c.name}",
                lines = body.lines()
            )
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, title)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "چاپ / اشتراک PDF"
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(
                title = "قالب اسناد",
                subtitle = client?.name ?: "مراجع را انتخاب یا جستجو کنید",
                onBack = onBack
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SoftCard {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("جستجوی سند یا نام مراجع") },
                            placeholder = { Text("حروف سند یا نام مراجع…") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true
                        )
                    }
                }
                item {
                    SoftCard {
                        Text("مراجع", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        filteredClients.take(30).forEach { c ->
                            FilterChip(
                                selected = selectedId == c.id,
                                onClick = { selectedId = c.id },
                                label = { Text(c.name) },
                                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
                item {
                    SoftCard {
                        Text("دسته سند", fontWeight = FontWeight.Bold)
                        FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("همه") })
                        categories.forEach { cat ->
                            FilterChip(
                                selected = filter == cat,
                                onClick = { filter = cat },
                                label = { Text(cat) },
                                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
                items(templates, key = { it.id }) { def ->
                    SoftCard {
                        Text(def.title, fontWeight = FontWeight.Bold)
                        Text(def.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { printPdf(def.id, def.title) },
                                enabled = client != null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Print, null)
                                Spacer(Modifier.width(4.dp))
                                Text("چاپ PDF")
                            }
                            OutlinedButton(
                                onClick = { shareText(def.id, def.title) },
                                enabled = client != null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, null)
                                Spacer(Modifier.width(4.dp))
                                Text("متن")
                            }
                        }
                    }
                }
            }
        }
    }
}
