package com.xtine.habbitrabbit.widget

import android.content.Context
import com.xtine.habbitrabbit.data.Clock
import com.xtine.habbitrabbit.data.repo.HabbitRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun habbitRepository(): HabbitRepository
    fun clock(): Clock
}

internal fun Context.widgetEntryPoint(): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
