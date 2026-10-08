package com.smmiqbal.fluidtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.smmiqbal.fluidtracker.ui.theme.FluidTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WaterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize reminder notifications channel and schedule
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val selectedThemeId by viewModel.selectedThemeId.collectAsState()

            FluidTrackerTheme(selectedThemeId = selectedThemeId) {
                FluidTrackerMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WaterAppWidgetProvider.updateAllWidgets(this)
    }
}
