package com.hostelhub.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.hostelhub.app.data.local.AppSettingsManager
import com.hostelhub.app.data.local.ThemeMode
import com.hostelhub.app.presentation.navigation.AppNavHost
import com.hostelhub.app.presentation.security.AppLockScreen
import com.hostelhub.app.presentation.theme.HostelManagementTheme
import com.hostelhub.app.security.AppLockManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appSettingsManager: AppSettingsManager

    @Inject
    lateinit var appLockManager: AppLockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by appSettingsManager.themeMode.collectAsState()
            val isLocked by appLockManager.isLocked.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM_DEFAULT -> systemDark
            }

            HostelManagementTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLocked) {
                        AppLockScreen(
                            appLockManager = appLockManager,
                            onUnlocked = { appLockManager.unlock() }
                        )
                    } else {
                        AppNavHost()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appLockManager.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        appLockManager.onAppBackgrounded()
    }
}
