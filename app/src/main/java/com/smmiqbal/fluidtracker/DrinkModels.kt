package com.smmiqbal.fluidtracker

import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val id: String,
    val label: String,
    val subtitle: String,
    val accentColor: Color,
    val heroGradient: List<Color>,
    val surfaceColor: Color,
    val bgBase: Color
) {
    OLED(
        id = "oled",
        label = "Obsidian Sunset",
        subtitle = "Deep violet obsidian with vivid sunset 3D glass",
        accentColor = Color(0xFFF97316),
        heroGradient = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFFF97316)),
        surfaceColor = Color(0xFF161022),
        bgBase = Color(0xFF0C0816)
    ),
    PACIFIC(
        id = "pacific",
        label = "Deep Pacific",
        subtitle = "Oceanic abyss navy with electric cyan glass",
        accentColor = Color(0xFF06B6D4),
        heroGradient = listOf(Color(0xFF1E40AF), Color(0xFF0284C7), Color(0xFF06B6D4)),
        surfaceColor = Color(0xFF0D1B2C),
        bgBase = Color(0xFF050E18)
    ),
    ARCTIC(
        id = "arctic",
        label = "Arctic Frost",
        subtitle = "Glacial ice white with polar azure glass",
        accentColor = Color(0xFF38BDF8),
        heroGradient = listOf(Color(0xFF4F46E5), Color(0xFF38BDF8), Color(0xFF67E8F9)),
        surfaceColor = Color(0xFF0F1E31),
        bgBase = Color(0xFF07111D)
    ),
    TITANIUM(
        id = "titanium",
        label = "Titanium Slate",
        subtitle = "Brushed industrial graphite with platinum glass",
        accentColor = Color(0xFFCBD5E1),
        heroGradient = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF94A3B8)),
        surfaceColor = Color(0xFF191F28),
        bgBase = Color(0xFF0E1218)
    ),
    EMERALD(
        id = "emerald",
        label = "Midnight Emerald",
        subtitle = "Boreal forest obsidian with jade glass",
        accentColor = Color(0xFF10B981),
        heroGradient = listOf(Color(0xFF065F46), Color(0xFF059669), Color(0xFF34D399)),
        surfaceColor = Color(0xFF0A2016),
        bgBase = Color(0xFF04100A)
    ),
    VIOLET(
        id = "violet",
        label = "Cyber Violet",
        subtitle = "Electric dusk amethyst with ultraviolet glass",
        accentColor = Color(0xFFA855F7),
        heroGradient = listOf(Color(0xFF581C87), Color(0xFF7E22CE), Color(0xFFA855F7)),
        surfaceColor = Color(0xFF190F30),
        bgBase = Color(0xFF0B0619)
    ),
    AMBER(
        id = "amber",
        label = "Solar Amber",
        subtitle = "Twilight dark honey with warm bronze glass",
        accentColor = Color(0xFFF59E0B),
        heroGradient = listOf(Color(0xFF92400E), Color(0xFFD97706), Color(0xFFFBBF24)),
        surfaceColor = Color(0xFF22160A),
        bgBase = Color(0xFF120B04)
    ),
    NORDIC(
        id = "nordic",
        label = "Nordic Smoke",
        subtitle = "Scandinavian charcoal mist with frosted cyan",
        accentColor = Color(0xFF7DD3FC),
        heroGradient = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF38BDF8)),
        surfaceColor = Color(0xFF161E2A),
        bgBase = Color(0xFF0C1118)
    ),
    CELESTIAL(
        id = "celestial",
        label = "Celestial Ripple",
        subtitle = "Electric ripple cyan with fine concentric orbit rings",
        accentColor = Color(0xFF38BDF8),
        heroGradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF7DD3FC)),
        surfaceColor = Color(0xFF0D253F),
        bgBase = Color(0xFF05172A)
    ),
    AERO(
        id = "aero",
        label = "Aero Aqua Glass",
        subtitle = "Pastel celestial azure with pristine frosted telemetry",
        accentColor = Color(0xFF0EA5E9),
        heroGradient = listOf(Color(0xFF38BDF8), Color(0xFF7DD3FC), Color(0xFFBAE6FD)),
        surfaceColor = Color(0xFF102840),
        bgBase = Color(0xFF081C30)
    ),
    STEALTH_DROP(
        id = "stealth_drop",
        label = "Teardrop Stealth",
        subtitle = "Charcoal matte vessel with intense royal cobalt & sky waves",
        accentColor = Color(0xFF2563EB),
        heroGradient = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF38BDF8)),
        surfaceColor = Color(0xFF161A22),
        bgBase = Color(0xFF0D0F14)
    ),
    PURE_LIGHT(
        id = "pure_light",
        label = "Pure Drop Light",
        subtitle = "Ultra-clean minimalist studio palette with vibrant azure droplet",
        accentColor = Color(0xFF0284C7),
        heroGradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF60A5FA)),
        surfaceColor = Color(0xFF182232),
        bgBase = Color(0xFF0A1420)
    )
}

data class DrinkItem(
    val id: String,
    val name: String,
    val badge: String,
    val colorPrimary: Color,
    val colorSecondary: Color,
    val unitName: String,
    val hydrationFactor: Float = 1.0f,
    val benefit: String = "Hydration"
)

data class VolumeOption(
    val ml: Int,
    val label: String,
    val fractionOfGlass: Float
)

val availableVolumes = listOf(
    VolumeOption(250, "250ml", 1.0f),
    VolumeOption(350, "350ml", 1.4f),
    VolumeOption(500, "500ml", 2.0f),
    VolumeOption(750, "750ml", 3.0f)
)

// Single exclusive beverage: Pure Water
val availableDrinks = listOf(
    DrinkItem(
        id = "water",
        name = "Pure Water",
        badge = "H2O",
        colorPrimary = Color(0xFF38BDF8),
        colorSecondary = Color(0xFF0284C7),
        unitName = "water",
        hydrationFactor = 1.0f,
        benefit = "Essential cellular replenishment & fluid balance"
    )
)

data class IntakeLog(
    val id: Long = System.currentTimeMillis(),
    val timeStr: String,
    val volumeMl: Int,
    val dateStr: String
)

data class DayHistory(
    val dayLabel: String,
    val dateStr: String,
    val volumeMl: Int,
    val goalMl: Int = 2000,
    val isToday: Boolean = false
)

