package com.smmiqbal.fluidtracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

val widgetScienceInsights = listOf(
    "Morning hydration immediately upon waking restores night fluid loss and primes metabolism.",
    "Mild 1-2% dehydration measurably diminishes cognitive sharpness and working memory.",
    "Cellular water volume supports efficient nutrient delivery and metabolic waste clearance.",
    "Optimal hydration reduces cardiovascular strain by maintaining healthy blood volume.",
    "Fluid intake regulates joint cartilage hydration, shock absorption, and muscle elasticity.",
    "Consistent daily water intake supports sustained cognitive energy without caffeine crashes.",
    "Cellular hydration enhances skin barrier strength, natural radiance, and cellular turnover.",
    "Adequate water intake aids core thermoregulation and prevents fatigue during deep work.",
    "Hydration before cognitive sessions boosts executive task switching and mental clarity.",
    "Drinking water regularly stabilizes electrolyte equilibrium and neuromuscular signaling."
)

object WidgetThemeHelper {
    fun getCardBackground(themeId: String): Int = when (themeId) {
        "celestial" -> R.drawable.widget_glass_card_bg_celestial
        "aero" -> R.drawable.widget_glass_card_bg_aero
        "stealth_drop" -> R.drawable.widget_glass_card_bg_stealth
        "pure_light" -> R.drawable.widget_glass_card_bg_pure
        "pacific" -> R.drawable.widget_glass_card_bg_pacific
        "arctic" -> R.drawable.widget_glass_card_bg_arctic
        "titanium" -> R.drawable.widget_glass_card_bg_titanium
        "emerald" -> R.drawable.widget_glass_card_bg_emerald
        "violet" -> R.drawable.widget_glass_card_bg_violet
        "amber" -> R.drawable.widget_glass_card_bg_amber
        "nordic" -> R.drawable.widget_glass_card_bg_nordic
        else -> R.drawable.widget_glass_card_bg_oled
    }

    fun getPillBackground(themeId: String): Int = when (themeId) {
        "celestial" -> R.drawable.widget_glass_pill_bg_celestial
        "aero" -> R.drawable.widget_glass_pill_bg_aero
        "stealth_drop" -> R.drawable.widget_glass_pill_bg_stealth
        "pure_light" -> R.drawable.widget_glass_pill_bg_pure
        "pacific" -> R.drawable.widget_glass_pill_bg_pacific
        "arctic" -> R.drawable.widget_glass_pill_bg_arctic
        "titanium" -> R.drawable.widget_glass_pill_bg_titanium
        "emerald" -> R.drawable.widget_glass_pill_bg_emerald
        "violet" -> R.drawable.widget_glass_pill_bg_violet
        "amber" -> R.drawable.widget_glass_pill_bg_amber
        "nordic" -> R.drawable.widget_glass_pill_bg_nordic
        else -> R.drawable.widget_glass_pill_bg_oled
    }

    fun getOrbWidgetBackground(themeId: String): Int = when (themeId) {
        "celestial" -> R.drawable.widget_glass_orb_bg_celestial
        "aero" -> R.drawable.widget_glass_orb_bg_aero
        "stealth_drop" -> R.drawable.widget_glass_orb_bg_stealth
        "pure_light" -> R.drawable.widget_glass_orb_bg_pure
        "pacific" -> R.drawable.widget_glass_orb_bg_pacific
        "arctic" -> R.drawable.widget_glass_orb_bg_arctic
        "titanium" -> R.drawable.widget_glass_orb_bg_titanium
        "emerald" -> R.drawable.widget_glass_orb_bg_emerald
        "violet" -> R.drawable.widget_glass_orb_bg_violet
        "amber" -> R.drawable.widget_glass_orb_bg_amber
        "nordic" -> R.drawable.widget_glass_orb_bg_nordic
        else -> R.drawable.widget_glass_orb_bg_oled
    }

    fun getOrbButtonDrawable(): Int = R.drawable.widget_orb_btn_water
}

/**
 * Widget 1: Frosted Liquid Glass Hub Card (4x2 / 3x2)
 */
class WaterAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ADD_WATER = "com.smmiqbal.fluidtracker.ACTION_ADD_WATER"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // 1. Update Card Hub Widgets
            val cardComponent = ComponentName(context, WaterAppWidgetProvider::class.java)
            val cardIds = appWidgetManager.getAppWidgetIds(cardComponent)
            if (cardIds.isNotEmpty()) {
                val intent = Intent(context, WaterAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, cardIds)
                }
                context.sendBroadcast(intent)
            }

            // 2. Update Orb Widgets
            val orbComponent = ComponentName(context, WaterQuickOrbWidgetProvider::class.java)
            val orbIds = appWidgetManager.getAppWidgetIds(orbComponent)
            if (orbIds.isNotEmpty()) {
                val intent = Intent(context, WaterQuickOrbWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, orbIds)
                }
                context.sendBroadcast(intent)
            }

            // 3. Update Pill Widgets
            val pillComponent = ComponentName(context, WaterMinimalPillWidgetProvider::class.java)
            val pillIds = appWidgetManager.getAppWidgetIds(pillComponent)
            if (pillIds.isNotEmpty()) {
                val intent = Intent(context, WaterMinimalPillWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, pillIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val repo = WaterRepository(context)
            repo.checkDateTransition()

            val count = repo.glassCount.first()
            val totalMl = repo.totalVolumeMl.first()
            val goalMl = repo.goalVolumeMl.first()
            val streak = repo.streakDays.first()
            val themeId = repo.selectedTheme.first()
            val userName = repo.userName.first()
            val quoteIdx = repo.selectedWidgetQuote.first()

            val quote = if (quoteIdx in widgetScienceInsights.indices) {
                widgetScienceInsights[quoteIdx]
            } else {
                widgetScienceInsights[(count + streak) % widgetScienceInsights.size]
            }

            val pct = if (goalMl > 0) ((totalMl.toFloat() / goalMl.toFloat()) * 100).toInt() else 0

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.water_widget_layout)

                // Themed Frosted Glass Acrylic Background
                views.setInt(
                    R.id.widget_root,
                    "setBackgroundResource",
                    WidgetThemeHelper.getCardBackground(themeId)
                )

                // Header Badges
                views.setTextViewText(R.id.widget_drink_badge, "FLUID PRO")
                views.setTextViewText(R.id.widget_streak_badge, "${streak}D STREAK")
                views.setTextViewText(R.id.widget_user_badge, userName)

                // Volume Stats
                views.setTextViewText(R.id.widget_count_big, String.format(Locale.US, "%,d ml", totalMl))
                views.setTextViewText(
                    R.id.widget_count_sub,
                    "of ${String.format(Locale.US, "%,d", goalMl)} ml ($pct%)"
                )
                views.setProgressBar(R.id.widget_progress_bar, 100, pct.coerceIn(0, 100), false)

                // Milestone Badge
                if (totalMl >= goalMl) {
                    views.setViewVisibility(R.id.widget_milestone_badge, View.VISIBLE)
                    views.setTextViewText(R.id.widget_milestone_badge, "GOAL MET")
                } else {
                    views.setViewVisibility(R.id.widget_milestone_badge, View.GONE)
                }

                views.setTextViewText(R.id.widget_quote_text, quote)

                // 3D Glass Orb Button
                views.setInt(
                    R.id.widget_btn_add,
                    "setBackgroundResource",
                    WidgetThemeHelper.getOrbButtonDrawable()
                )

                // PendingIntent for Quick Add Button (+250ml)
                val addIntent = Intent(context, WaterAppWidgetProvider::class.java).apply {
                    action = ACTION_ADD_WATER
                }
                val pendingAdd = PendingIntent.getBroadcast(
                    context,
                    101,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_add, pendingAdd)

                // PendingIntent to Open App on Root Tap
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingOpen = PendingIntent.getActivity(
                    context,
                    102,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingOpen)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ADD_WATER) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repo = WaterRepository(context)
                    repo.addIntake(250)
                    val totalMl = repo.totalVolumeMl.first()
                    val goalMl = repo.goalVolumeMl.first()

                    SoundHelper.playWaterDropSound()
                    SoundHelper.triggerHapticFeedback(context)

                    Handler(Looper.getMainLooper()).post {
                        val toastMsg = if (totalMl >= goalMl) {
                            "Logged 250ml ($totalMl ml). Daily target reached!"
                        } else {
                            "Logged 250ml ($totalMl / $goalMl ml)"
                        }
                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                    }

                    // Immediately synchronize all widgets on screen
                    updateAllWidgets(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

/**
 * Widget 2: Floating Frosted Liquid Glass Orb (1x1 / 2x2)
 */
class WaterQuickOrbWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ADD_WATER_ORB = "com.smmiqbal.fluidtracker.ACTION_ADD_WATER_ORB"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val repo = WaterRepository(context)
            repo.checkDateTransition()

            val totalMl = repo.totalVolumeMl.first()
            val goalMl = repo.goalVolumeMl.first()
            val themeId = repo.selectedTheme.first()

            val pct = if (goalMl > 0) ((totalMl.toFloat() / goalMl.toFloat()) * 100).toInt() else 0

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_orb_layout)

                // Frosted Orb Glass Background
                views.setInt(
                    R.id.widget_orb_root,
                    "setBackgroundResource",
                    WidgetThemeHelper.getOrbWidgetBackground(themeId)
                )

                views.setTextViewText(R.id.widget_orb_pct, "$pct%")
                views.setTextViewText(R.id.widget_orb_count, "${String.format(Locale.US, "%,d", totalMl)} / ${String.format(Locale.US, "%,d", goalMl)} ml")

                views.setInt(
                    R.id.widget_btn_add_orb,
                    "setBackgroundResource",
                    WidgetThemeHelper.getOrbButtonDrawable()
                )

                val addIntent = Intent(context, WaterQuickOrbWidgetProvider::class.java).apply {
                    action = ACTION_ADD_WATER_ORB
                }
                val pendingAdd = PendingIntent.getBroadcast(
                    context,
                    201,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_add_orb, pendingAdd)

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingOpen = PendingIntent.getActivity(
                    context,
                    202,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_orb_root, pendingOpen)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ADD_WATER_ORB) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repo = WaterRepository(context)
                    repo.addIntake(250)
                    val totalMl = repo.totalVolumeMl.first()
                    val goalMl = repo.goalVolumeMl.first()

                    SoundHelper.playWaterDropSound()
                    SoundHelper.triggerHapticFeedback(context)

                    Handler(Looper.getMainLooper()).post {
                        val toastMsg = if (totalMl >= goalMl) {
                            "Logged 250ml ($totalMl ml). Daily target reached!"
                        } else {
                            "Logged 250ml ($totalMl / $goalMl ml)"
                        }
                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                    }

                    // Immediately synchronize all widgets on screen
                    WaterAppWidgetProvider.updateAllWidgets(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

/**
 * Widget 3: Minimalist Frosted Liquid Glass Pill (3x1 / 4x1)
 */
class WaterMinimalPillWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ADD_WATER_PILL = "com.smmiqbal.fluidtracker.ACTION_ADD_WATER_PILL"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val repo = WaterRepository(context)
            repo.checkDateTransition()

            val totalMl = repo.totalVolumeMl.first()
            val goalMl = repo.goalVolumeMl.first()
            val streak = repo.streakDays.first()
            val themeId = repo.selectedTheme.first()

            val pct = if (goalMl > 0) ((totalMl.toFloat() / goalMl.toFloat()) * 100).toInt() else 0

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_pill_layout)

                // Frosted Pill Glass Background
                views.setInt(
                    R.id.widget_pill_root,
                    "setBackgroundResource",
                    WidgetThemeHelper.getPillBackground(themeId)
                )

                views.setTextViewText(
                    R.id.widget_pill_count,
                    "${String.format(Locale.US, "%,d", totalMl)} / ${String.format(Locale.US, "%,d", goalMl)} ml"
                )
                views.setTextViewText(R.id.widget_pill_streak, "${streak}D STREAK")
                views.setProgressBar(R.id.widget_pill_progress, 100, pct.coerceIn(0, 100), false)

                views.setInt(
                    R.id.widget_btn_add_pill,
                    "setBackgroundResource",
                    WidgetThemeHelper.getOrbButtonDrawable()
                )

                val addIntent = Intent(context, WaterMinimalPillWidgetProvider::class.java).apply {
                    action = ACTION_ADD_WATER_PILL
                }
                val pendingAdd = PendingIntent.getBroadcast(
                    context,
                    301,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_add_pill, pendingAdd)

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingOpen = PendingIntent.getActivity(
                    context,
                    302,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_pill_root, pendingOpen)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ADD_WATER_PILL) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repo = WaterRepository(context)
                    repo.addIntake(250)
                    val totalMl = repo.totalVolumeMl.first()
                    val goalMl = repo.goalVolumeMl.first()

                    SoundHelper.playWaterDropSound()
                    SoundHelper.triggerHapticFeedback(context)

                    Handler(Looper.getMainLooper()).post {
                        val toastMsg = if (totalMl >= goalMl) {
                            "Logged 250ml ($totalMl ml). Daily target reached!"
                        } else {
                            "Logged 250ml ($totalMl / $goalMl ml)"
                        }
                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                    }

                    // Immediately synchronize all widgets on screen
                    WaterAppWidgetProvider.updateAllWidgets(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
