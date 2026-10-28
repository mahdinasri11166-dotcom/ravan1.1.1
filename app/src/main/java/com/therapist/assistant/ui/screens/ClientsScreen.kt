package com.therapist.assistant.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.Client
import com.therapist.assistant.ui.components.*
import com.therapist.assistant.ui.theme.Amber500
import com.therapist.assistant.ui.theme.Emerald500
import com.therapist.assistant.ui.theme.Sky500
import com.therapist.assistant.util.JalaliDate
import com.therapist.assistant.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.*

private enum class ClientFilter { ALL, DEBTORS, ACTIVE, NEW, INACTIVE, ARCHIVED, HIGH_CANCEL }

private data class ClientStats(
    val client: Client,
    val sessionCount: Int,
    val unpaidAmount: Double,
    val unpaidCount: Int,
    val lastSessionMillis: Long,
    val cancelStreak: Int,
    val completedCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onClientClick: (Long) -> Unit,
    onAddClient: () -> Unit
) {
    val clients by viewModel.clients.collectAsState()
    val clientCount by viewModel.clientCount.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val showQuota = !isPro && !viewModel.isTrialActive() && !viewModel.isOrg.value
    val allAppointments by viewModel.allAppointments.collectAsState()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ClientFilter.ALL) }
    val currency = remember { NumberFormat.getNumberInstance(Locale("fa")) }
    val now = remember { System.currentTimeMillis() }
    val monthStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000
    val sixtyDaysAgo = now - 60L * 24 * 60 * 60 * 1000

    val statsMap = remember(clients, allAppointments) {
        clients.associate { client ->
            val appts = allAppointments.filter { it.clientId == client.id }.sortedByDescending { it.dateTimeMillis }
            var streak = 0
            for (a in appts) {
                if (a.status == AppointmentStatus.CANCELLED) streak++ else break
            }
            client.id to ClientStats(
                client = client,
                sessionCount = appts.size,
                unpaidAmount = appts.filter { !it.isPaid && it.status != AppointmentStatus.CANCELLED }.sumOf { it.fee },
                unpaidCount = appts.count { !it.isPaid && it.status != AppointmentStatus.CANCELLED && it.fee > 0 },
                lastSessionMillis = appts.maxOfOrNull { it.dateTimeMillis } ?: 0L,
                cancelStreak = streak,
                completedCount = appts.count { it.status == AppointmentStatus.COMPLETED }
            )
        }
    }

    val debtorIds = remember(statsMap) { statsMap.filter { it.value.unpaidAmount > 0 }.keys }
    val activeIds = remember(statsMap, thirtyDaysAgo) { statsMap.filter { it.value.lastSessionMillis >= thirtyDaysAgo }.keys }
    val newIds = remember(clients, monthStart) { clients.filter { it.createdAt >= monthStart }.map { it.id }.toSet() }
    val inactiveIds = remember(statsMap, sixtyDaysAgo) {
        statsMap.filter {
            it.value.lastSessionMillis in 1 until sixtyDaysAgo ||
                (it.value.lastSessionMillis == 0L && it.value.client.createdAt < sixtyDaysAgo)
        }.keys
    }
    val totalDebt = remember(statsMap) { statsMap.values.sumOf { it.unpaidAmount } }
    val topDebtors = remember(statsMap) {
        statsMap.values.filter { it.unpaidAmount > 0 }.sortedByDescending { it.unpaidAmount }.take(5)
    }

    val filteredStats = remember(statsMap, query, filter, debtorIds, activeIds, newIds, inactiveIds) {
        statsMap.values.filter { cs ->
            val c = cs.client
            val matchQuery = query.isBlank() || c.name.contains(query, true) || c.phone.contains(query) ||
                c.nationalId.contains(query) || c.address.contains(query, true)
            val matchFilter = when (filter) {
                ClientFilter.ALL -> !c.isArchived
                ClientFilter.DEBTORS -> c.id in debtorIds && !c.isArchived
                ClientFilter.ACTIVE -> c.id in activeIds && !c.isArchived
                ClientFilter.NEW -> c.id in newIds && !c.isArchived
                ClientFilter.INACTIVE -> c.id in inactiveIds && !c.isArchived
                ClientFilter.ARCHIVED -> c.isArchived
                ClientFilter.HIGH_CANCEL -> !c.isArchived && (
                    cs.cancelStreak >= 2 || (cs.sessionCount >= 3 && cs.completedCount * 2 < cs.sessionCount)
                )
            }
            matchQuery && matchFilter
        }.sortedWith(compareByDescending<ClientStats> { it.unpaidAmount }.thenByDescending { it.lastSessionMillis }.thenBy { it.client.name })
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClient,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.PersonAdd, null) },
                text = { Text("مراجع جدید", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 100.dp)) {
            if (showQuota) {
                item {
                    SoftCard {
                        Text("مراجع: $clientCount / ${AppointmentViewModel.FREE_CLIENT_LIMIT}", fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { (clientCount.toFloat() / AppointmentViewModel.FREE_CLIENT_LIMIT).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            }
            item {
                GradientTopBar(
                    title = "داشبورد مراجعین",
                    subtitle = "${JalaliDate.toFa(clients.size)} مراجع ثبت‌شده",
                    onBack = onBack
                )
            }

            item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiTile(Modifier.weight(1f), "کل مراجعین", "${clients.size}", Icons.Default.People, MaterialTheme.colorScheme.primary)
                        KpiTile(Modifier.weight(1f), "فعال (۳۰ روز)", "${activeIds.size}", Icons.Default.TrendingUp, Emerald500)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiTile(Modifier.weight(1f), "جدید این ماه", "${newIds.size}", Icons.Default.PersonAdd, Sky500)
                        KpiTile(Modifier.weight(1f), "بدهی", currency.format(totalDebt), Icons.Default.AccountBalanceWallet, Amber500) {
                            filter = ClientFilter.DEBTORS
                        }
                    }
                }
            }

            if (topDebtors.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader("بدهکاران اولویت‌دار", "همه") { filter = ClientFilter.DEBTORS }
                    }
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(topDebtors, key = { it.client.id }) { cs ->
                            SoftCard(Modifier.width(160.dp), onClick = { onClientClick(cs.client.id) }) {
                                Text(cs.client.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                Text("${currency.format(cs.unpaidAmount)} ت", color = Amber500, fontWeight = FontWeight.SemiBold)
                                Text("${JalaliDate.toFa(cs.unpaidCount)} جلسه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("جستجوی نام، تلفن، کد ملی، آدرس...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, null) }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chips = listOf(
                        ClientFilter.ALL to "همه (${clients.size})",
                        ClientFilter.DEBTORS to "بدهکار (${debtorIds.size})",
                        ClientFilter.ACTIVE to "فعال (${activeIds.size})",
                        ClientFilter.NEW to "جدید (${newIds.size})",
                        ClientFilter.INACTIVE to "غیرفعال (${inactiveIds.size})",
                        ClientFilter.ARCHIVED to "بایگانی",
                        ClientFilter.HIGH_CANCEL to "لغو زیاد"
                    )
                    items(chips.size) { i ->
                        val (f, label) = chips[i]
                        FilterChip(
                            selected = filter == f,
                            onClick = { filter = f },
                            label = { Text(label) },
                            leadingIcon = if (filter == f) ({ Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }) else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionHeader("نتایج", "${JalaliDate.toFa(filteredStats.size)}")
                }
            }

            if (filteredStats.isEmpty()) {
                item {
                    Box(Modifier.padding(16.dp)) {
                        EmptyState(
                            Icons.Default.Search,
                            if (clients.isEmpty()) "هنوز مراجعی ثبت نشده" else "نتیجه‌ای یافت نشد",
                            actionLabel = if (clients.isEmpty()) "افزودن اولین مراجع" else null,
                            onAction = if (clients.isEmpty()) onAddClient else null
                        )
                    }
                }
            }

            items(filteredStats, key = { it.client.id }) { cs ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    SoftCard(onClick = { onClientClick(cs.client.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(
                                cs.client.name,
                                if (cs.unpaidAmount > 0) Amber500 else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(cs.client.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                                    if (cs.cancelStreak >= 3) {
                                        Spacer(Modifier.width(6.dp))
                                        StatusPill("لغو×${cs.cancelStreak}", MaterialTheme.colorScheme.error)
                                    }
                                }
                                if (cs.client.phone.isNotBlank()) {
                                    Text(JalaliDate.toFa(cs.client.phone), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                val last = if (cs.lastSessionMillis <= 0) "بدون جلسه" else "آخرین: ${JalaliDate.formatDate(cs.lastSessionMillis)}"
                                Text("${JalaliDate.toFa(cs.sessionCount)} جلسه · ${JalaliDate.toFa(cs.completedCount)} انجام · $last",
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (cs.unpaidAmount > 0) {
                                    Text("بدهی: ${currency.format(cs.unpaidAmount)} تومان", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Amber500)
                                }
                            }
                            Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
