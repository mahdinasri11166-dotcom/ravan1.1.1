package com.therapist.assistant.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therapist.assistant.ui.components.GradientTopBar
import com.therapist.assistant.ui.components.SoftCard
import com.therapist.assistant.util.BusinessCardImage
import com.therapist.assistant.util.QrUtil
import com.therapist.assistant.viewmodel.AppointmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class CardTheme(
    val id: String,
    val title: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val onPrimary: Color,
    val qrModule: Int
)

private val cardThemes = listOf(
    CardTheme("sky", "آبی حرفه‌ای", Color(0xFF0284C7), Color(0xFF0C4A6E), Color(0xFFE0F2FE), Color.White, 0xFF0C4A6E.toInt()),
    CardTheme("rose", "رز کلینیک", Color(0xFFBE123C), Color(0xFF881337), Color(0xFFFFE4E6), Color.White, 0xFF881337.toInt()),
    CardTheme("emerald", "زمردی", Color(0xFF059669), Color(0xFF064E3B), Color(0xFFD1FAE5), Color.White, 0xFF064E3B.toInt()),
    CardTheme("violet", "بنفش لوکس", Color(0xFF7C3AED), Color(0xFF4C1D95), Color(0xFFEDE9FE), Color.White, 0xFF4C1D95.toInt()),
    CardTheme("sunset", "غروب گرم", Color(0xFFEA580C), Color(0xFF9A3412), Color(0xFFFFEDD5), Color.White, 0xFF9A3412.toInt())
)

@Composable
fun BusinessCardScreen(
    viewModel: AppointmentViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val clinicName by viewModel.clinicName.collectAsState()
    val clinicPhone by viewModel.clinicPhone.collectAsState()
    val clinicAddress by viewModel.clinicAddress.collectAsState()
    val therapistName by viewModel.defaultTherapistName.collectAsState()
    val currentName = viewModel.currentTherapistName()

    var name by remember(currentName, therapistName) {
        mutableStateOf(currentName.ifBlank { therapistName }.ifBlank { "درمانگر" })
    }
    var role by remember { mutableStateOf("روان‌شناس و مشاور") }
    var tagline by remember { mutableStateOf("همراه شما در مسیر سلامت روان") }
    var phone by remember(clinicPhone) { mutableStateOf(clinicPhone) }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var address by remember(clinicAddress) { mutableStateOf(clinicAddress) }
    var clinic by remember(clinicName) { mutableStateOf(clinicName.ifBlank { "مرکز گل سرخ" }) }
    var themeId by remember { mutableStateOf("rose") }
    var sharing by remember { mutableStateOf(false) }

    val theme = cardThemes.find { it.id == themeId } ?: cardThemes[1]
    val displayName = name.ifBlank { "درمانگر" }

    val vcard = remember(displayName, clinic, role, phone, email, address, website, tagline) {
        QrUtil.vCard(
            fullName = displayName,
            organization = clinic,
            title = role,
            phone = phone,
            email = email,
            address = address,
            url = website,
            note = tagline
        )
    }
    val qrBmp = remember(vcard, theme.qrModule) {
        QrUtil.encode(vcard, size = 512, moduleColor = theme.qrModule)
    }

    fun shareAsImage() {
        if (sharing) return
        sharing = true
        scope.launch {
            try {
                val palette = BusinessCardImage.CardPalette(
                    primary = theme.primary.toIntArgb(),
                    secondary = theme.secondary.toIntArgb(),
                    accent = theme.accent.toIntArgb(),
                    onPrimary = theme.onPrimary.toIntArgb()
                )
                val bmp = withContext(Dispatchers.Default) {
                    BusinessCardImage.render(
                        name = displayName,
                        role = role,
                        clinic = clinic,
                        phone = phone,
                        email = email,
                        website = website,
                        address = address,
                        tagline = tagline,
                        palette = palette,
                        qrBitmap = qrBmp
                    )
                }
                val file = withContext(Dispatchers.IO) {
                    BusinessCardImage.saveToCache(context, bmp, "card_${theme.id}.png")
                }
                BusinessCardImage.shareImage(context, file, "کارت ویزیت — $displayName")
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در ساخت تصویر: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                sharing = false
            }
        }
    }

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientTopBar(
                title = "کارت ویزیت دیجیتال",
                subtitle = "ارسال به‌صورت تصویر + QR مخاطب",
                onBack = onBack
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Preview card
                Box(
                    Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(theme.primary, theme.secondary))
                                )
                                .padding(20.dp)
                        ) {
                            // decorative soft blobs
                            Box(
                                Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = (-12).dp, y = (-8).dp)
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                            )
                            Box(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 16.dp, y = 20.dp)
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.10f))
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(58.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color.White.copy(alpha = 0.35f),
                                                        Color.White.copy(alpha = 0.12f)
                                                    )
                                                )
                                            )
                                            .border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.Badge,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            displayName,
                                            color = theme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            role.ifBlank { "روان‌شناس" },
                                            color = theme.onPrimary.copy(alpha = 0.92f),
                                            fontSize = 13.sp
                                        )
                                        if (clinic.isNotBlank()) {
                                            Text(
                                                clinic,
                                                color = theme.onPrimary.copy(alpha = 0.78f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Filled.CreditCard,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                if (tagline.isNotBlank()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        tagline,
                                        color = theme.onPrimary.copy(alpha = 0.88f),
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (phone.isNotBlank()) InfoLine(Icons.Outlined.Phone, phone, theme.primary)
                                if (email.isNotBlank()) InfoLine(Icons.Outlined.Email, email, theme.primary)
                                if (website.isNotBlank()) InfoLine(Icons.Outlined.Language, website, theme.primary)
                                if (address.isNotBlank()) InfoLine(Icons.Default.LocationOn, address, theme.primary)
                                if (listOf(phone, email, website, address).all { it.isBlank() }) {
                                    Text(
                                        "اطلاعات تماس را پایین وارد کنید",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = qrBmp.asImageBitmap(),
                                    contentDescription = "QR",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, theme.accent, RoundedCornerShape(12.dp))
                                        .padding(6.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("اسکن مخاطب", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Badge, null, tint = theme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("تم کارت", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cardThemes.forEach { t ->
                            FilterChip(
                                selected = themeId == t.id,
                                onClick = { themeId = t.id },
                                label = { Text(t.title, fontSize = 11.sp) },
                                leadingIcon = if (themeId == t.id) {
                                    { Icon(Icons.Outlined.Badge, null, Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                SoftCard {
                    Text("اطلاعات روی کارت", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("نام") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = role, onValueChange = { role = it },
                        label = { Text("عنوان / تخصص") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = clinic, onValueChange = { clinic = it },
                        label = { Text("نام مرکز / کلینیک") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagline, onValueChange = { tagline = it },
                        label = { Text("شعار کوتاه") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("تلفن") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("ایمیل") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = website, onValueChange = { website = it },
                        label = { Text("وب‌سایت") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address, onValueChange = { address = it },
                        label = { Text("آدرس") }, modifier = Modifier.fillMaxWidth()
                    )
                }

                SoftCard {
                    Text("ارسال و اشتراک", fontWeight = FontWeight.Bold)
                    Text(
                        "کارت به‌صورت تصویر PNG ساخته می‌شود و از طریق واتساپ، تلگرام، اینستاگرام و … قابل ارسال است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { shareAsImage() },
                        enabled = !sharing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                    ) {
                        if (sharing) {
                            CircularProgressIndicator(
                                Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("در حال ساخت تصویر…")
                        } else {
                            Icon(Icons.Default.Share, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ارسال کارت به‌صورت عکس")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val text = buildString {
                                    appendLine(displayName)
                                    appendLine(role)
                                    if (tagline.isNotBlank()) appendLine(tagline)
                                    appendLine("━━━━━━━━━━━━━━━━")
                                    if (phone.isNotBlank()) appendLine("📞 $phone")
                                    if (email.isNotBlank()) appendLine("✉ $email")
                                    if (website.isNotBlank()) appendLine("🌐 $website")
                                    if (address.isNotBlank()) appendLine("📍 $address")
                                    appendLine()
                                    appendLine("— درمانگرنو —")
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                            putExtra(Intent.EXTRA_SUBJECT, "کارت ویزیت — $displayName")
                                        },
                                        "اشتراک متنی"
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("متن")
                        }
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(
                                    AnnotatedString(
                                        buildString {
                                            appendLine(displayName)
                                            appendLine(role)
                                            if (phone.isNotBlank()) appendLine(phone)
                                            if (email.isNotBlank()) appendLine(email)
                                        }
                                    )
                                )
                                Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("کپی")
                        }
                    }
                    if (phone.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val digits = phone.filter { it.isDigit() || it == '+' }
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Call, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("تماس با همین شماره")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "QR از نوع vCard است؛ با اسکن در اکثر گوشی‌ها مستقیم به مخاطبین اضافه می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

private fun Color.toIntArgb(): Int {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF334155), maxLines = 2)
    }
}
