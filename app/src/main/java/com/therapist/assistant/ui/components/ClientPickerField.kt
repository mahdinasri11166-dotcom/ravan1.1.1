package com.therapist.assistant.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.Client

/**
 * انتخاب مراجع از پرونده‌های موجود + امکان ساخت پرونده سریع با تایپ نام.
 */
@Composable
fun ClientPickerField(
    clients: List<Client>,
    selected: Client?,
    query: String,
    onQueryChange: (String) -> Unit,
    onSelect: (Client) -> Unit,
    onCreateQuick: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "انتخاب مراجع (از پرونده‌ها)",
    onlyActive: Boolean = true
) {
    val pool = remember(clients, onlyActive) {
        if (onlyActive) clients.filter { !it.isArchived } else clients
    }
    val filtered = remember(pool, query) {
        if (query.isBlank()) pool.take(30)
        else pool.filter {
            it.name.contains(query, true) ||
                it.phone.contains(query) ||
                it.nationalId.contains(query)
        }.take(30)
    }
    val exactMatch = pool.any { it.name.equals(query.trim(), ignoreCase = true) }
    val canCreate = query.trim().length >= 2 && !exactMatch

    Column(modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                Text(
                    when {
                        selected != null -> "انتخاب‌شده: ${selected.name}"
                        canCreate -> "پرونده‌ای نیست — می‌توانید پرونده جدید بسازید"
                        else -> "از لیست پرونده‌ها انتخاب کنید یا نام را تایپ کنید"
                    }
                )
            }
        )

        if (canCreate) {
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = { onCreateQuick(query.trim()) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("ساخت پرونده «${query.trim()}» و ادامه")
            }
            Text(
                "بعداً می‌توانید کد ملی، تلفن و بقیه را در پرونده تکمیل کنید.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (filtered.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            SoftCard {
                Text("پرونده‌ها", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                LazyColumn(Modifier.heightIn(max = 220.dp)) {
                    items(filtered, key = { it.id }) { c ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(c)
                                    onQueryChange(c.name)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarCircle(c.name, MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.name, fontWeight = FontWeight.Medium)
                                val sub = listOf(c.phone, c.nationalId).filter { it.isNotBlank() }.joinToString(" · ")
                                if (sub.isNotBlank()) {
                                    Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (selected?.id == c.id) {
                                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else if (query.isBlank()) {
            Text(
                "هنوز پرونده فعالی نیست. نام مراجع را تایپ کنید تا پرونده ساخته شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
