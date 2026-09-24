package com.horusgrey.aurion.actions

import android.app.AlarmClockInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract

sealed class PlannedAction(val summary: String) {
    data class OpenUri(val uri: String) : PlannedAction("Open $uri")
    data class ComposeSms(val number: String, val body: String) : PlannedAction("Draft SMS to $number")
    data class CreateCalendarEvent(val title: String, val beginMillis: Long, val endMillis: Long) : PlannedAction("Create calendar event: $title")
    data class SetAlarm(val hour: Int, val minute: Int, val message: String) : PlannedAction("Set alarm for %02d:%02d".format(hour, minute))
}

class LocalActionBroker(private val context: Context) {
    fun executeConfirmed(action: PlannedAction) {
        val intent = when (action) {
            is PlannedAction.OpenUri -> Intent(Intent.ACTION_VIEW, Uri.parse(action.uri))
            is PlannedAction.ComposeSms -> Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${action.number}")).apply {
                putExtra("sms_body", action.body)
            }
            is PlannedAction.CreateCalendarEvent -> Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, action.title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, action.beginMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, action.endMillis)
            }
            is PlannedAction.SetAlarm -> Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, action.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, action.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, action.message)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
