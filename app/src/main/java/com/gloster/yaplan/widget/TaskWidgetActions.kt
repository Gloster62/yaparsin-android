package com.gloster.yaplan.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import com.gloster.yaplan.YapLanApp

private val IdKey = ActionParameters.Key<Long>("id")

/** The wanted state, not "toggle": a double tap then can't flip it back. */
private val DoneKey = ActionParameters.Key<Boolean>("done")

internal fun setTaskDone(taskId: Long, done: Boolean): Action =
    actionRunCallback<SetTaskDone>(actionParametersOf(IdKey to taskId, DoneKey to done))

internal fun setItemDone(itemId: Long, done: Boolean): Action =
    actionRunCallback<SetItemDone>(actionParametersOf(IdKey to itemId, DoneKey to done))

class SetTaskDone : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[IdKey] ?: return
        val done = parameters[DoneKey] ?: return
        (context.applicationContext as YapLanApp).repository.setDone(id, done)
    }
}

class SetItemDone : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[IdKey] ?: return
        val done = parameters[DoneKey] ?: return
        (context.applicationContext as YapLanApp).repository.setItemDone(id, done)
    }
}
