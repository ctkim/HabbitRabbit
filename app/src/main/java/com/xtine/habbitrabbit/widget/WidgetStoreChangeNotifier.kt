package com.xtine.habbitrabbit.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.xtine.habbitrabbit.data.repo.StoreChangeNotifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class WidgetStoreChangeNotifier(
    private val context: Context
) : StoreChangeNotifier {
    override suspend fun notifyChanged() {
        HabbitRabbitWidget().updateAll(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object WidgetNotifierModule {

    @Provides
    @Singleton
    fun provideStoreChangeNotifier(@ApplicationContext context: Context): StoreChangeNotifier =
        WidgetStoreChangeNotifier(context)
}
