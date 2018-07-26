package com.koma.bugly

import android.content.Context

object Bugly {
    fun init(context: Context) {
        CrashHandler.init(context)
    }
}