package com.xtine.habbitrabbit

import android.app.Application
import com.xtine.habbitrabbit.widget.WidgetMidnightWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HabbitRabbitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WidgetMidnightWorker.enqueue(this)
    }
}
