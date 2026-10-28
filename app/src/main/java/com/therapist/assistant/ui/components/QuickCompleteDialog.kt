package com.therapist.assistant.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.therapist.assistant.data.Appointment

@Composable
fun QuickCompleteDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onConfirm: (fee: Double, isPaid: Boolean, note: String) -> Unit
) {
    var feeText by remember { mutableStateOf(if (appointment.fee > 0) appointment.fee.toLong().toString() else "") }
    var isPaid by remember { mutableStateOf(appointment.isPaid) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("بستن جلسه — ${appointment.clientName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("وضعیت به «انجام‌شده» تغییر می‌کند.")
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it.filter { c -> c.isDigit() } },
                    label = { Text("مبلغ (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row {
                    Checkbox(checked = isPaid, onCheckedChange = { isPaid = it })
                    Text("پرداخت شد", modifier = Modifier.padding(top = 12.dp))
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("یادداشت کوتاه (اختیاری)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(feeText.toDoubleOrNull() ?: appointment.fee, isPaid, note)
            }) { Text("ثبت انجام جلسه") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
