package com.therapist.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
@Composable
fun RoseGradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), MaterialTheme.colorScheme.tertiary)
                )
            )
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), content = content)
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val mod = modifier
        .shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        )
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            )
        )
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Column(mod.padding(16.dp), content = content)
}

@Composable
fun KpiTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.1f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(1.dp, accent.copy(alpha = 0.15f), shape)
            .padding(14.dp)
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = accent.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun NavChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun AvatarCircle(
    letter: String,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Int = 48
) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(color, color.copy(alpha = 0.75f)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            letter.take(1),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    SoftCard {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAction, shape = RoundedCornerShape(12.dp)) {
                    Text(actionLabel)
                }
            }
        }
    }
}


@Composable
fun GradientTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                    )
                )
            )
    ) {
        Column(Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                actions()
            }
        }
    }
}


@Composable
fun PlanLimitBanner(
    message: String,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message.isBlank()) return
    SoftCard(modifier = modifier) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
            Text("ارتقا به نسخه ویژه")
        }
    }
}
