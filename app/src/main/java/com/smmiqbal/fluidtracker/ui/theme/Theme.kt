package com.smmiqbal.fluidtracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.smmiqbal.fluidtracker.AppTheme

// 1. OLED Obsidian Sunset (Matching Reference Design: Magenta to Sunset Orange on Velvet Dark)
val OledColorScheme = darkColorScheme(
    primary = Color(0xFFF97316),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEC4899),
    onPrimaryContainer = Color.White,
    background = Color(0xFF0C0816),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF161022),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF221834),
    onSurfaceVariant = Color(0xFFA78BFA),
    outline = Color(0xFF3B2758),
    outlineVariant = Color(0xFF1E1430)
)

// 2. Deep Pacific (Oceanic Abyss & Electric Azure)
val PacificColorScheme = darkColorScheme(
    primary = Color(0xFF06B6D4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0284C7),
    onPrimaryContainer = Color.White,
    background = Color(0xFF050E18),
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0xFF0D1B2C),
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = Color(0xFF152A42),
    onSurfaceVariant = Color(0xFF7DD3FC),
    outline = Color(0xFF1F4166),
    outlineVariant = Color(0xFF0E2034)
)

// 3. Arctic Frost (Glacial Ice & Polar Azure)
val ArcticColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4F46E5),
    onPrimaryContainer = Color.White,
    background = Color(0xFF07111D),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0F1E31),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1A2F4B),
    onSurfaceVariant = Color(0xFFBAE6FD),
    outline = Color(0xFF284872),
    outlineVariant = Color(0xFF13243A)
)

// 4. Titanium Slate (Industrial Brushed Platinum & Charcoal)
val TitaniumColorScheme = darkColorScheme(
    primary = Color(0xFFCBD5E1),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF64748B),
    onPrimaryContainer = Color.White,
    background = Color(0xFF0E1218),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF191F28),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF262F3C),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF3B4759),
    outlineVariant = Color(0xFF1F2631)
)

// 5. Midnight Emerald (Obsidian Forest & Jade Mint)
val EmeraldColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF059669),
    onPrimaryContainer = Color.White,
    background = Color(0xFF04100A),
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0xFF0A2016),
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = Color(0xFF123224),
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF1B4D37),
    outlineVariant = Color(0xFF0E271B)
)

// 6. Cyber Violet (Ultraviolet Dusk Amethyst)
val VioletColorScheme = darkColorScheme(
    primary = Color(0xFFA855F7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF7E22CE),
    onPrimaryContainer = Color.White,
    background = Color(0xFF0B0619),
    onBackground = Color(0xFFFAF5FF),
    surface = Color(0xFF190F30),
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = Color(0xFF28194B),
    onSurfaceVariant = Color(0xFFD8B4FE),
    outline = Color(0xFF422878),
    outlineVariant = Color(0xFF1F123C)
)

// 7. Solar Amber (Dark Roasted Honey & Sunburst Bronze)
val AmberColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD97706),
    onPrimaryContainer = Color.White,
    background = Color(0xFF120B04),
    onBackground = Color(0xFFFFFBEB),
    surface = Color(0xFF22160A),
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = Color(0xFF352312),
    onSurfaceVariant = Color(0xFFFCD34D),
    outline = Color(0xFF54371D),
    outlineVariant = Color(0xFF291B0E)
)

// 8. Nordic Smoke (Scandinavian Charcoal Mist & Frosted Cyan)
val NordicColorScheme = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF334155),
    onPrimaryContainer = Color.White,
    background = Color(0xFF0C1118),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF161E2A),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF222D3E),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF35445B),
    outlineVariant = Color(0xFF1B2433)
)

// 9. Celestial Ripple (Image 1 Right: Vibrant Electric Cyan with Radial Ripple Glow)
val CelestialColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF021B30),
    primaryContainer = Color(0xFF0284C7),
    onPrimaryContainer = Color.White,
    background = Color(0xFF05172A),
    onBackground = Color(0xFFF0F9FF),
    surface = Color(0xFF0D253F),
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = Color(0xFF143456),
    onSurfaceVariant = Color(0xFFBAE6FD),
    outline = Color(0xFF1B4B7C),
    outlineVariant = Color(0xFF0C243E)
)

// 10. Aero Aqua Glass (Image 1 Left: Soft Celestial Cyan with Luminous Glass)
val AeroColorScheme = darkColorScheme(
    primary = Color(0xFF0EA5E9),
    onPrimary = Color(0xFF031E33),
    primaryContainer = Color(0xFF38BDF8),
    onPrimaryContainer = Color.White,
    background = Color(0xFF081C30),
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0xFF102840),
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = Color(0xFF193B5D),
    onSurfaceVariant = Color(0xFF7DD3FC),
    outline = Color(0xFF225585),
    outlineVariant = Color(0xFF0F263D)
)

// 11. Teardrop Stealth (Image 2 Right: Charcoal Matte with Royal Cobalt & Sky Waves)
val StealthDropColorScheme = darkColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color.White,
    background = Color(0xFF0D0F14),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF161A22),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF222834),
    onSurfaceVariant = Color(0xFF93C5FD),
    outline = Color(0xFF333E52),
    outlineVariant = Color(0xFF1A1F2A)
)

// 12. Pure Drop Light (Image 2 Left: Crisp Studio Palette with Vivid Azure Drop)
val PureLightColorScheme = darkColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF38BDF8),
    onPrimaryContainer = Color.White,
    background = Color(0xFF0A1420),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF182232),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF25344B),
    onSurfaceVariant = Color(0xFF93C5FD),
    outline = Color(0xFF3B5072),
    outlineVariant = Color(0xFF1D2A3D)
)

@Composable
fun FluidTrackerTheme(
    selectedThemeId: String = "celestial",
    content: @Composable () -> Unit
) {
    val colorScheme = when (selectedThemeId) {
        "pacific" -> PacificColorScheme
        "arctic" -> ArcticColorScheme
        "titanium" -> TitaniumColorScheme
        "emerald" -> EmeraldColorScheme
        "violet" -> VioletColorScheme
        "amber" -> AmberColorScheme
        "nordic" -> NordicColorScheme
        "celestial" -> CelestialColorScheme
        "aero" -> AeroColorScheme
        "stealth_drop" -> StealthDropColorScheme
        "pure_light" -> PureLightColorScheme
        else -> CelestialColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
