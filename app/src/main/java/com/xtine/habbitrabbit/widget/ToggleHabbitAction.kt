package com.xtine.habbitrabbit.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class ToggleHabbitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habbitId = parameters[HabbitIdKey] ?: return
        val entry = context.widgetEntryPoint()
        val today = entry.clock().today()
        entry.habbitRepository().toggle(habbitId, today)
        HabbitRabbitWidget().update(context, glanceId)
    }

    companion object {
        val HabbitIdKey = ActionParameters.Key<Long>("habbitId")
    }
}
