package com.smmiqbal.fluidtracker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

object StoryShareHelper {

    fun shareHydrationStory(
        context: Context,
        totalVolumeMl: Int,
        streakDays: Int,
        currentTheme: AppTheme,
        userName: String = "S. M. Mahmud Iqbal"
    ) {
        try {
            val width = 1080
            val height = 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Background Gradient (Matches current liquid glass theme)
            val c1 = currentTheme.heroGradient[0].hashCode()
            val c2 = currentTheme.surfaceColor.hashCode()
            val c3 = currentTheme.bgBase.hashCode()
            paint.shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(c1, c2, c3),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null

            // 2. Concentric Ripple Rings (Reference: Image 1 Right)
            paint.style = Paint.Style.STROKE
            paint.color = 0x26FFFFFF // 15% white
            val centerX = width / 2f
            val centerY = height * 0.44f
            for (radius in listOf(180f, 250f, 320f, 400f, 480f)) {
                paint.strokeWidth = 3f
                canvas.drawCircle(centerX, centerY, radius, paint)
            }

            // 3. Ambient Fluid Glow in Center
            paint.style = Paint.Style.FILL
            val accentInt = currentTheme.accentColor.hashCode()
            paint.shader = RadialGradient(
                centerX, centerY, 360f,
                intArrayOf(accentInt, 0x00000000),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(centerX, centerY, 360f, paint)
            paint.shader = null

            // 4. Subtle Rising Bubbles (Reference: Image 1 Right)
            paint.style = Paint.Style.FILL
            val bubbles = listOf(
                Pair(centerX - 160f, height * 0.62f) to 22f,
                Pair(centerX + 180f, height * 0.58f) to 34f,
                Pair(centerX - 80f, height * 0.32f) to 18f,
                Pair(centerX + 110f, height * 0.28f) to 26f,
                Pair(centerX + 40f, height * 0.22f) to 14f
            )
            for ((pos, r) in bubbles) {
                paint.color = 0x33FFFFFF
                canvas.drawCircle(pos.first, pos.second, r, paint)
                paint.color = 0x80FFFFFF.toInt()
                canvas.drawCircle(pos.first - r * 0.3f, pos.second - r * 0.3f, r * 0.25f, paint)
            }

            // 5. Header: Logo & Title
            try {
                val logoBmp = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo_round)
                if (logoBmp != null) {
                    val logoSize = 130
                    val scaledLogo = Bitmap.createScaledBitmap(logoBmp, logoSize, logoSize, true)
                    canvas.drawBitmap(scaledLogo, centerX - (logoSize / 2f), 180f, null)
                }
            } catch (_: Exception) {}

            paint.textAlign = Paint.Align.CENTER

            // Subtitle Header
            paint.color = 0xCCFFFFFF.toInt()
            paint.textSize = 34f
            paint.letterSpacing = 0.25f
            paint.isFakeBoldText = true
            canvas.drawText("FLUID TRACKER • HYDRATION WRAPPED", centerX, 370f, paint)
            paint.letterSpacing = 0f

            // Milestone Headline
            paint.color = 0xFFFFFFFF.toInt()
            paint.textSize = 62f
            paint.isFakeBoldText = true
            canvas.drawText("A Milestone Reached", centerX, 520f, paint)

            // Huge Total Volume
            paint.color = accentInt
            paint.textSize = 150f
            paint.isFakeBoldText = true
            canvas.drawText("${totalVolumeMl}ml", centerX, 740f, paint)

            paint.color = 0xEEFFFFFF.toInt()
            paint.textSize = 36f
            paint.isFakeBoldText = false
            canvas.drawText("Total Hydration Logged Today", centerX, 810f, paint)

            // Streak Card Container
            val pillRect = RectF(centerX - 240f, 910f, centerX + 240f, 1030f)
            paint.color = 0x2AFFFFFF
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(pillRect, 60f, 60f, paint)

            paint.color = 0x55FFFFFF
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawRoundRect(pillRect, 60f, 60f, paint)
            paint.style = Paint.Style.FILL

            // Streak Label
            paint.color = 0xFFFFFFFF.toInt()
            paint.textSize = 42f
            paint.isFakeBoldText = true
            canvas.drawText("$streakDays DAY ACTIVE STREAK", centerX, 985f, paint)

            // 6. Layered Sine Wave Body near bottom
            paint.style = Paint.Style.FILL
            paint.color = 0x33FFFFFF
            val path = android.graphics.Path()
            path.moveTo(0f, 1400f)
            for (x in 0..width step 40) {
                val y = 1400f + (sin((x.toFloat() / width) * 2 * Math.PI) * 40f).toFloat()
                path.lineTo(x.toFloat(), y)
            }
            path.lineTo(width.toFloat(), height.toFloat())
            path.lineTo(0f, height.toFloat())
            path.close()
            canvas.drawPath(path, paint)

            // 7. Developer Attribution Footer Capsule
            val footerRect = RectF(centerX - 350f, 1680f, centerX + 350f, 1780f)
            paint.color = 0x1AFFFFFF
            canvas.drawRoundRect(footerRect, 50f, 50f, paint)
            paint.color = 0x44FFFFFF
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRoundRect(footerRect, 50f, 50f, paint)
            paint.style = Paint.Style.FILL

            paint.color = 0xEEFFFFFF.toInt()
            paint.textSize = 30f
            paint.isFakeBoldText = true
            paint.letterSpacing = 0.08f
            canvas.drawText("DEVELOPED BY S. M. MAHMUD IQBAL", centerX, 1740f, paint)
            paint.letterSpacing = 0f

            // Save Bitmap to Cache
            val cachePath = File(context.cacheDir, "shared_story_images")
            if (!cachePath.exists()) cachePath.mkdirs()

            val storyFile = File(cachePath, "hydration_wrapped_story.png")
            val outputStream = FileOutputStream(storyFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Construct Content URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                storyFile
            )

            // Launch Native Android Share Chooser
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "$userName reached ${totalVolumeMl}ml with an active $streakDays-day streak on Fluid Tracker! Developed by S. M. Mahmud Iqbal."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Hydration Wrapped to Story").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            // Graceful fallback: Share text directly if file saving/intent fails
            try {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "HYDRATION WRAPPED MILESTONE\nTotal Hydration Today: ${totalVolumeMl}ml\nActive Streak: $streakDays Days\nFluid Tracker • Developed by S. M. Mahmud Iqbal"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "Share Hydration Wrapped"))
            } catch (_: Exception) {
                Toast.makeText(context, "Unable to launch share dialog.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
