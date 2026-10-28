package com.therapist.assistant.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.therapist.assistant.viewmodel.AppointmentViewModel

@Composable
fun PinLockScreen(
    viewModel: AppointmentViewModel,
    onUnlocked: () -> Unit
) {
    val storedHash by viewModel.pinHash.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    fun tryBiometric() {
        val activity = context as? FragmentActivity ?: return
        val bm = BiometricManager.from(context)
        if (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) {
            error = "بیومتریک در دسترس نیست"
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onUnlocked()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    error = errString.toString()
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("ورود به درمانگرنو")
                .setSubtitle("تأیید هویت برای دسترسی به پرونده‌ها")
                .setNegativeButtonText("استفاده از پین")
                .build()
        )
    }

    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled) tryBiometric()
    }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxWidth().height(240.dp).background(
                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)))
            )
        )
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(88.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, null, Modifier.size(42.dp), tint = Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Text("درمانگرنو", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text("ورود امن به پرونده‌ها", color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(28.dp))
            Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                pin = it
                                error = null
                            }
                        },
                        label = { Text("پین ۴ تا ۶ رقمی") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = error != null,
                        supportingText = error?.let { { Text(it) } },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (viewModel.checkPin(pin, storedHash)) onUnlocked()
                            else error = "پین نادرست است"
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = pin.length >= 4,
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("ورود", fontWeight = FontWeight.Bold) }
                    if (biometricEnabled) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { tryBiometric() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, null)
                            Spacer(Modifier.width(8.dp))
                            Text("ورود با اثرانگشت / چهره")
                        }
                    }
                }
            }
        }
    }
}
