package com.therapist.assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ─── پالت پایه (سازگار با UiKit قدیمی) ───
val Rose50 = Color(0xFFFFF1F3)
val Rose100 = Color(0xFFFFE0E6)
val Rose200 = Color(0xFFFFB3C1)
val Rose400 = Color(0xFFF43F5E)
val Rose500 = Color(0xFFE11D48)
val Rose700 = Color(0xFFBE123C)
val Rose900 = Color(0xFF881337)

val Emerald500 = Color(0xFF10B981)
val Emerald100 = Color(0xFFD1FAE5)
val Amber500 = Color(0xFFF59E0B)
val Amber100 = Color(0xFFFEF3C7)
val Sky500 = Color(0xFF0EA5E9)
val Sky100 = Color(0xFFE0F2FE)
val Sky50 = Color(0xFFF0F9FF)
val Sheet700 = Color(0xFF334155)
val Slate700 = Color(0xFF334155)
val Slate500 = Color(0xFF64748B)
val Slate100 = Color(0xFFF1F5F9)

enum class AppColorTheme(
    val id: String,
    val titleFa: String,
    /** رنگ نمایش در انتخابگر تم */
    val swatch: Color
) {
    SKY("sky", "آبی اقیانوس", Color(0xFF0EA5E9)),
    ROSE("rose", "رز یاقوتی", Color(0xFFE11D48)),
    EMERALD("emerald", "زمرد آرام", Color(0xFF10B981)),
    VIOLET("violet", "بنفش شفق", Color(0xFF8B5CF6)),
    SUNSET("sunset", "غروب نارنجی", Color(0xFFF97316)),
    OCEAN("ocean", "فیروزه دریایی", Color(0xFF14B8A6)),
    LAVENDER("lavender", "اسطوخودوس", Color(0xFFA78BFA)),
    MIDNIGHT("midnight", "نیمه‌شب لوکس", Color(0xFF6366F1));

    companion object {
        fun fromId(id: String?): AppColorTheme =
            entries.find { it.id == id } ?: SKY
    }
}

private fun lightScheme(
    primary: Color,
    onPrimary: Color = Color.White,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color,
    tertiary: Color,
    tertiaryContainer: Color,
    onTertiaryContainer: Color,
    background: Color,
    surface: Color = Color.White,
    surfaceVariant: Color,
    outline: Color
) = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    background = background,
    onBackground = Color(0xFF0F172A),
    surface = surface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = outline,
    outlineVariant = outline.copy(alpha = 0.55f),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    inversePrimary = primary.copy(alpha = 0.85f),
    surfaceTint = primary.copy(alpha = 0.08f)
)

private fun darkScheme(
    primary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    secondaryContainer: Color,
    tertiary: Color,
    tertiaryContainer: Color,
    background: Color,
    surface: Color,
    surfaceVariant: Color
) = darkColorScheme(
    primary = primary,
    onPrimary = Color(0xFF0B1220),
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = Color(0xFF0B1220),
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = tertiary,
    onTertiary = Color(0xFF0B1220),
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = Color(0xFFF1F5F9),
    background = background,
    onBackground = Color(0xFFF1F5F9),
    surface = surface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),
    surfaceTint = primary.copy(alpha = 0.12f)
)

fun colorSchemeFor(theme: AppColorTheme, dark: Boolean): ColorScheme {
    if (dark) {
        return when (theme) {
            AppColorTheme.SKY -> darkScheme(
                primary = Color(0xFF7DD3FC),
                primaryContainer = Color(0xFF0369A1),
                onPrimaryContainer = Color(0xFFE0F2FE),
                secondary = Color(0xFF67E8F9),
                secondaryContainer = Color(0xFF0E7490),
                tertiary = Color(0xFFA5B4FC),
                tertiaryContainer = Color(0xFF4338CA),
                background = Color(0xFF070D18),
                surface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFF1E293B)
            )
            AppColorTheme.ROSE -> darkScheme(
                primary = Color(0xFFFDA4AF),
                primaryContainer = Color(0xFF9F1239),
                onPrimaryContainer = Color(0xFFFFE4E6),
                secondary = Color(0xFFF9A8D4),
                secondaryContainer = Color(0xFF9D174D),
                tertiary = Color(0xFFFCD34D),
                tertiaryContainer = Color(0xFF92400E),
                background = Color(0xFF140A0E),
                surface = Color(0xFF1F1218),
                surfaceVariant = Color(0xFF3B1F2B)
            )
            AppColorTheme.EMERALD -> darkScheme(
                primary = Color(0xFF6EE7B7),
                primaryContainer = Color(0xFF047857),
                onPrimaryContainer = Color(0xFFD1FAE5),
                secondary = Color(0xFF5EEAD4),
                secondaryContainer = Color(0xFF0F766E),
                tertiary = Color(0xFFA3E635),
                tertiaryContainer = Color(0xFF3F6212),
                background = Color(0xFF07140F),
                surface = Color(0xFF0F1F18),
                surfaceVariant = Color(0xFF1A3328)
            )
            AppColorTheme.VIOLET -> darkScheme(
                primary = Color(0xFFC4B5FD),
                primaryContainer = Color(0xFF6D28D9),
                onPrimaryContainer = Color(0xFFEDE9FE),
                secondary = Color(0xFFF0ABFC),
                secondaryContainer = Color(0xFFA21CAF),
                tertiary = Color(0xFF93C5FD),
                tertiaryContainer = Color(0xFF1D4ED8),
                background = Color(0xFF0C0A14),
                surface = Color(0xFF15121F),
                surfaceVariant = Color(0xFF2A2140)
            )
            AppColorTheme.SUNSET -> darkScheme(
                primary = Color(0xFFFDBA74),
                primaryContainer = Color(0xFFC2410C),
                onPrimaryContainer = Color(0xFFFFEDD5),
                secondary = Color(0xFFFDE047),
                secondaryContainer = Color(0xFFA16207),
                tertiary = Color(0xFFF9A8D4),
                tertiaryContainer = Color(0xFF9D174D),
                background = Color(0xFF140C08),
                surface = Color(0xFF1C120C),
                surfaceVariant = Color(0xFF3A2418)
            )
            AppColorTheme.OCEAN -> darkScheme(
                primary = Color(0xFF5EEAD4),
                primaryContainer = Color(0xFF0F766E),
                onPrimaryContainer = Color(0xFFCCFBF1),
                secondary = Color(0xFF7DD3FC),
                secondaryContainer = Color(0xFF0369A1),
                tertiary = Color(0xFFA5B4FC),
                tertiaryContainer = Color(0xFF4338CA),
                background = Color(0xFF06141A),
                surface = Color(0xFF0C1C22),
                surfaceVariant = Color(0xFF16333C)
            )
            AppColorTheme.LAVENDER -> darkScheme(
                primary = Color(0xFFDDD6FE),
                primaryContainer = Color(0xFF7C3AED),
                onPrimaryContainer = Color(0xFFF5F3FF),
                secondary = Color(0xFFFBCFE8),
                secondaryContainer = Color(0xFFBE185D),
                tertiary = Color(0xFFBFDBFE),
                tertiaryContainer = Color(0xFF1E40AF),
                background = Color(0xFF100E18),
                surface = Color(0xFF181524),
                surfaceVariant = Color(0xFF2C2540)
            )
            AppColorTheme.MIDNIGHT -> darkScheme(
                primary = Color(0xFFA5B4FC),
                primaryContainer = Color(0xFF4338CA),
                onPrimaryContainer = Color(0xFFEEF2FF),
                secondary = Color(0xFFC4B5FD),
                secondaryContainer = Color(0xFF6D28D9),
                tertiary = Color(0xFF67E8F9),
                tertiaryContainer = Color(0xFF0E7490),
                background = Color(0xFF06070F),
                surface = Color(0xFF0C0E1A),
                surfaceVariant = Color(0xFF1A1D33)
            )
        }
    }
    return when (theme) {
        AppColorTheme.SKY -> lightScheme(
            primary = Color(0xFF0284C7),
            primaryContainer = Color(0xFFE0F2FE),
            onPrimaryContainer = Color(0xFF0C4A6E),
            secondary = Color(0xFF06B6D4),
            secondaryContainer = Color(0xFFCFFAFE),
            onSecondaryContainer = Color(0xFF164E63),
            tertiary = Color(0xFF6366F1),
            tertiaryContainer = Color(0xFFE0E7FF),
            onTertiaryContainer = Color(0xFF312E81),
            background = Color(0xFFF0F9FF),
            surfaceVariant = Color(0xFFE0F2FE),
            outline = Color(0xFF7DD3FC)
        )
        AppColorTheme.ROSE -> lightScheme(
            primary = Color(0xFFE11D48),
            primaryContainer = Color(0xFFFFE4E6),
            onPrimaryContainer = Color(0xFF881337),
            secondary = Color(0xFFDB2777),
            secondaryContainer = Color(0xFFFCE7F3),
            onSecondaryContainer = Color(0xFF831843),
            tertiary = Color(0xFFF59E0B),
            tertiaryContainer = Color(0xFFFEF3C7),
            onTertiaryContainer = Color(0xFF78350F),
            background = Color(0xFFFFF1F3),
            surfaceVariant = Color(0xFFFFE4E6),
            outline = Color(0xFFFDA4AF)
        )
        AppColorTheme.EMERALD -> lightScheme(
            primary = Color(0xFF059669),
            primaryContainer = Color(0xFFD1FAE5),
            onPrimaryContainer = Color(0xFF064E3B),
            secondary = Color(0xFF0D9488),
            secondaryContainer = Color(0xFFCCFBF1),
            onSecondaryContainer = Color(0xFF134E4A),
            tertiary = Color(0xFF65A30D),
            tertiaryContainer = Color(0xFFECFCCB),
            onTertiaryContainer = Color(0xFF365314),
            background = Color(0xFFF0FDF4),
            surfaceVariant = Color(0xFFDCFCE7),
            outline = Color(0xFF6EE7B7)
        )
        AppColorTheme.VIOLET -> lightScheme(
            primary = Color(0xFF7C3AED),
            primaryContainer = Color(0xFFEDE9FE),
            onPrimaryContainer = Color(0xFF4C1D95),
            secondary = Color(0xFFD946EF),
            secondaryContainer = Color(0xFFFAE8FF),
            onSecondaryContainer = Color(0xFF701A75),
            tertiary = Color(0xFF2563EB),
            tertiaryContainer = Color(0xFFDBEAFE),
            onTertiaryContainer = Color(0xFF1E3A8A),
            background = Color(0xFFF5F3FF),
            surfaceVariant = Color(0xFFEDE9FE),
            outline = Color(0xFFC4B5FD)
        )
        AppColorTheme.SUNSET -> lightScheme(
            primary = Color(0xFFEA580C),
            primaryContainer = Color(0xFFFFEDD5),
            onPrimaryContainer = Color(0xFF7C2D12),
            secondary = Color(0xFFF59E0B),
            secondaryContainer = Color(0xFFFEF3C7),
            onSecondaryContainer = Color(0xFF78350F),
            tertiary = Color(0xFFE11D48),
            tertiaryContainer = Color(0xFFFFE4E6),
            onTertiaryContainer = Color(0xFF881337),
            background = Color(0xFFFFF7ED),
            surfaceVariant = Color(0xFFFFEDD5),
            outline = Color(0xFFFDBA74)
        )
        AppColorTheme.OCEAN -> lightScheme(
            primary = Color(0xFF0D9488),
            primaryContainer = Color(0xFFCCFBF1),
            onPrimaryContainer = Color(0xFF134E4A),
            secondary = Color(0xFF0284C7),
            secondaryContainer = Color(0xFFE0F2FE),
            onSecondaryContainer = Color(0xFF0C4A6E),
            tertiary = Color(0xFF6366F1),
            tertiaryContainer = Color(0xFFE0E7FF),
            onTertiaryContainer = Color(0xFF312E81),
            background = Color(0xFFF0FDFA),
            surfaceVariant = Color(0xFFCCFBF1),
            outline = Color(0xFF5EEAD4)
        )
        AppColorTheme.LAVENDER -> lightScheme(
            primary = Color(0xFF8B5CF6),
            primaryContainer = Color(0xFFF3E8FF),
            onPrimaryContainer = Color(0xFF5B21B6),
            secondary = Color(0xFFEC4899),
            secondaryContainer = Color(0xFFFCE7F3),
            onSecondaryContainer = Color(0xFF831843),
            tertiary = Color(0xFF6366F1),
            tertiaryContainer = Color(0xFFE0E7FF),
            onTertiaryContainer = Color(0xFF312E81),
            background = Color(0xFFFAF5FF),
            surfaceVariant = Color(0xFFF3E8FF),
            outline = Color(0xFFD8B4FE)
        )
        AppColorTheme.MIDNIGHT -> lightScheme(
            primary = Color(0xFF4F46E5),
            primaryContainer = Color(0xFFE0E7FF),
            onPrimaryContainer = Color(0xFF312E81),
            secondary = Color(0xFF7C3AED),
            secondaryContainer = Color(0xFFEDE9FE),
            onSecondaryContainer = Color(0xFF4C1D95),
            tertiary = Color(0xFF0891B2),
            tertiaryContainer = Color(0xFFCFFAFE),
            onTertiaryContainer = Color(0xFF164E63),
            background = Color(0xFFEEF2FF),
            surfaceVariant = Color(0xFFE0E7FF),
            outline = Color(0xFFA5B4FC)
        )
    }
}

/** گرادیان هدر — چندلایه و زنده‌تر */
fun headerGradientColors(theme: AppColorTheme, dark: Boolean): List<Color> {
    if (dark) {
        return when (theme) {
            AppColorTheme.SKY -> listOf(Color(0xFF0C4A6E), Color(0xFF0369A1), Color(0xFF0284C7))
            AppColorTheme.ROSE -> listOf(Color(0xFF881337), Color(0xFFBE123C), Color(0xFFE11D48))
            AppColorTheme.EMERALD -> listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669))
            AppColorTheme.VIOLET -> listOf(Color(0xFF4C1D95), Color(0xFF6D28D9), Color(0xFF8B5CF6))
            AppColorTheme.SUNSET -> listOf(Color(0xFF7C2D12), Color(0xFFC2410C), Color(0xFFEA580C))
            AppColorTheme.OCEAN -> listOf(Color(0xFF134E4A), Color(0xFF0F766E), Color(0xFF14B8A6))
            AppColorTheme.LAVENDER -> listOf(Color(0xFF5B21B6), Color(0xFF7C3AED), Color(0xFFA78BFA))
            AppColorTheme.MIDNIGHT -> listOf(Color(0xFF1E1B4B), Color(0xFF3730A3), Color(0xFF4F46E5))
        }
    }
    return when (theme) {
        AppColorTheme.SKY -> listOf(Color(0xFF38BDF8), Color(0xFF0EA5E9), Color(0xFF0284C7), Color(0xFF0369A1))
        AppColorTheme.ROSE -> listOf(Color(0xFFFB7185), Color(0xFFF43F5E), Color(0xFFE11D48), Color(0xFFBE123C))
        AppColorTheme.EMERALD -> listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857))
        AppColorTheme.VIOLET -> listOf(Color(0xFFC4B5FD), Color(0xFFA78BFA), Color(0xFF8B5CF6), Color(0xFF7C3AED))
        AppColorTheme.SUNSET -> listOf(Color(0xFFFDBA74), Color(0xFFFB923C), Color(0xFFF97316), Color(0xFFEA580C))
        AppColorTheme.OCEAN -> listOf(Color(0xFF5EEAD4), Color(0xFF2DD4BF), Color(0xFF14B8A6), Color(0xFF0D9488))
        AppColorTheme.LAVENDER -> listOf(Color(0xFFE9D5FF), Color(0xFFD8B4FE), Color(0xFFC084FC), Color(0xFFA855F7))
        AppColorTheme.MIDNIGHT -> listOf(Color(0xFF818CF8), Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF4338CA))
    }
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun TherapistAssistantTheme(
    themeId: String = AppColorTheme.SKY.id,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val theme = AppColorTheme.fromId(themeId)
    val colorScheme = colorSchemeFor(theme, darkTheme)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme.not()
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
