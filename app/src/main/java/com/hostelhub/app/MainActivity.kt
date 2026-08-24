package com.hostelhub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.hostelhub.app.data.local.AppSettingsManager
import com.hostelhub.app.presentation.navigation.AppNavHost
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.HostelManagementTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appSettingsManager: AppSettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by appSettingsManager.isDarkMode.collectAsState()
            HostelManagementTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundCool
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
