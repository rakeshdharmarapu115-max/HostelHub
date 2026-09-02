package com.hostelhub.app

import android.app.Application
import com.hostelhub.app.notifications.HostelNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HostelApp : Application() {

    @Inject
    lateinit var notificationManager: HostelNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager.createNotificationChannels()
    }
}
