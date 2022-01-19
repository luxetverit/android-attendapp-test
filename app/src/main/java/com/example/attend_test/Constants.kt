package com.example.attend_test

import com.google.zxing.client.android.BuildConfig

object Constants {
    // values have to be globally unique
    const val INTENT_ACTION_GRANT_USB: String = BuildConfig.APPLICATION_ID.toString() + ".GRANT_USB"
    const val INTENT_ACTION_DISCONNECT: String = BuildConfig.APPLICATION_ID.toString() + ".Disconnect"
    const val NOTIFICATION_CHANNEL: String = BuildConfig.APPLICATION_ID.toString() + ".Channel"
    const val INTENT_CLASS_MAIN_ACTIVITY: String = BuildConfig.APPLICATION_ID.toString() + ".MainActivity"

    // values have to be unique within each app
    const val NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001
}