package com.therapist.assistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.therapist.assistant.assessment.AssessmentCatalog
import com.therapist.assistant.util.ClinicalHints
import com.therapist.assistant.data.Client
import com.therapist.assistant.ui.components.ClientPickerField
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.AssessmentExporter
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.io.File

@Composable
fun AssessmentRunScreen(
    viewModel: AppointmentViewModel,
    assessmentId: String,
    preselectedClientId: Long? = null,
    onBack: () -> Unit,
    clientFacing: Boolean = false
) {
    val def = AssessmentCatalog.byId(assessmentId)
    if (def == null) {
        Text("آزمون یافت نشد")
        return
    }
    if (!viewModel.canUseAssessment(assessmentId)) {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                GradientTopBar(title = "آزمون قفل است", onBack = onBack)
                SoftCard {
                    Text("این آزمون در نسخه رایگان در دسترس نیست.", fontWeight = FontWeight.Bold)
                    Text("با دوره آزمایشی ۱۴روزه یا نسخه حرفه‌ای می‌توانید همه آزمون‌ها را اجرا کنید.")
                }
            }
        }
        return
    }

    val clients by viewModel.clients.collectAsState()
    val clinicName by viewModel.clinicName.collectAsState()
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val context = LocalContext.current

    var clientQuery by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf(preselectedClientId) }
    var answers by remember { mutableStateOf(List(def.items.size) { -1 }) }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var resultTotal by remember { mutableIntStateOf(0) }
    var resultInterp by remember { mutableStateOf("") }

    LaunchedEffect(preselectedClientId) {
        if (preselectedClientId != null) {
            viewModel.getClient(preselectedClientId)?.let {
                selectedClientId = it.id
                clientQuery = it.name
            }
        }
    }

    val selectedClient = clients.find { it.id == selectedClientId }
    val allAnswered = answers.all { it >= 0 }
    var clientFacingLocal by remember { mutableStateOf(clientFacing) }

    fun export(kind: String) {
        val name = selectedClient?.name ?: clientQuery.trim()
        if (name.isBlank()) return
        val total = AssessmentCatalog.score(def, answers)
        val clinicalHint = ClinicalHints.forScore(def.id, total) // نمایش در کارت نتیجه
        val interp = def.interpret(total)
        val dateText = JalaliDate.formatDateTime(System.currentTimeMillis())
        if (kind == "pdf") {
            val file = File(context.cacheDir, "assessment_${def.id}_${System.currentTimeMillis()}.pdf")
            AssessmentExporter.buildPdf(
                file, clinicName, clinicPhone, clinicAddress, name,
                def, answers, total, interp, dateText
            )
            shareFile(context, file, "application/pdf", "گزارش ${def.id}")
        } else {
            val file = File(context.cacheDir, "assessment_${def.id}_${System.currentTimeMillis()}.xlsx")
            AssessmentExporter.buildXlsx(
                file, clinicName, clinicPhone, clinicAddress, name,
                def, answers, total, interp, dateText
            )
            shareFile(
                context, file,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "گزارش ${def.id}"
            )
        }
    }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (saved) {
                        Text("ثبت شد — نمره: $resultTotal · $resultInterp", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            ClinicalHints.forScore(def.id, resultTotal),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { export("pdf") }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PictureAsPdf, null)
                                Spacer(Modifier.width(4.dp))
                                Text("PDF")
                            }
                            Button(onClick = { export("xlsx") }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.TableChart, null)
                                Spacer(Modifier.width(4.dp))
                                Text("اکسل")
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                val name = selectedClient?.name ?: clientQuery.trim()
                                if (name.isBlank()) {
                                    error = "نام مراجع را انتخاب یا وارد کنید"
                                    return@Button
                                }
                                if (!allAnswered) {
                                    error = "همه سؤالات را پاسخ دهید"
                                    return@Button
                                }
                                error = null
                                val scoredAnswers = answers.toList()
                                val total = AssessmentCatalog.score(def, scoredAnswers)
                                val interp = def.interpret(total)
                                resultTotal = total
                                resultInterp = interp
                                val details = scoredAnswers.mapIndexed { i, v -> "${i + 1}=$v" }.joinToString(";")
                                fun persist(clientId: Long) {
                                    // hint shown in result area via clinicalHint
                                viewModel.saveQuestionnaire(
                                        clientId = clientId,
                                        type = def.id,
                                        score = total,
                                        details = details + "|interp=" + interp
                                    )
                                    saved = true
                                }
                                if (selectedClientId != null) {
                                    selectedClientId?.let { persist(it) }
                                } else {
                                    viewModel.quickCreateClient(name) { id ->
                                        selectedClientId = id
                                        persist(id)
                                    }
                                }
                            },
                            enabled = allAnswered,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("ذخیره در پرونده") }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("حالت مراجع (فونت بزرگ)", style = MaterialTheme.typography.labelLarge)
                Switch(checked = clientFacingLocal, onCheckedChange = { clientFacingLocal = it })
            }
            GradientTopBar(
                title = def.titleFa,
                subtitle = def.titleEn,
                onBack = onBack
            )
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftCard {
                    Text(def.description, style = MaterialTheme.typography.bodyMedium)
                }
                SoftCard {
                    Text("مراجع", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    ClientPickerField(
                        clients = clients,
                        selected = selectedClient,
                        query = clientQuery,
                        onQueryChange = {
                            clientQuery = it
                            if (selectedClientId != null &&
                                clients.find { c -> c.id == selectedClientId }?.name != it
                            ) {
                                selectedClientId = null
                            }
                        },
                        onSelect = {
                            selectedClientId = it.id
                            clientQuery = it.name
                        },
                        onCreateQuick = { name ->
                            viewModel.quickCreateClient(name) { id ->
                                selectedClientId = id
                                clientQuery = name
                            }
                        },
                        label = "نام مراجع / جستجوی پرونده"
                    )
                }

                def.items.forEachIndexed { index, question ->
                    SoftCard {
                        Text("${index + 1}. $question", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        def.options.forEach { (label, value) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = answers[index] == value,
                                        onClick = {
                                            answers = answers.toMutableList().also { it[index] = value }
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = answers[index] == value,
                                    onClick = {
                                        answers = answers.toMutableList().also { it[index] = value }
                                    }
                                )
                                Text("$label ($value)")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

private fun shareFile(context: android.content.Context, file: File, mime: String, subject: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "ذخیره / اشتراک"
        )
    )
}
