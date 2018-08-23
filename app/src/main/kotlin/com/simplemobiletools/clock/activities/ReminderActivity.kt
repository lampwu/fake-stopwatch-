package com.simplemobiletools.clock.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.ALARM_ID
import com.simplemobiletools.clock.helpers.getPassedSeconds
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import kotlinx.android.synthetic.main.activity_reminder.*

class ReminderActivity : SimpleActivity() {
    private val hideNotificationHandler = Handler()
    private var isAlarmReminder = false
    private var alarm: Alarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        showOverLockscreen()
        updateTextColors(reminder_holder)

        val id = intent.getIntExtra(ALARM_ID, -1)
        isAlarmReminder = id != -1
        if (id != -1) {
            alarm = dbHelper.getAlarmWithId(id) ?: return
        }

        val label = if (isAlarmReminder) {
            if (alarm!!.label.isEmpty()) {
                getString(R.string.alarm)
            } else {
                alarm!!.label
            }
        } else {
            getString(R.string.timer)
        }

        reminder_title.text = label
        reminder_text.text = if (isAlarmReminder) getFormattedTime(getPassedSeconds(), false, false) else getString(R.string.time_expired)
        reminder_stop.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, getAdjustedPrimaryColor())
        reminder_stop.setOnClickListener {
            finish()
        }

        reminder_snooze.beVisibleIf(isAlarmReminder)
        reminder_snooze.applyColorFilter(config.textColor)
        reminder_snooze.setOnClickListener {
            snoozeClicked()
        }

        Handler().postDelayed({
            if (isAlarmReminder) {
                showAlarmNotification(alarm!!, true)
            } else {
                showTimerNotification(true)
            }

            val maxDuration = if (isAlarmReminder) config.alarmMaxReminderSecs else config.timerMaxReminderSecs
            hideNotificationHandler.postDelayed({
                finish()
            }, maxDuration * 1000L)
        }, 1000L)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        if (isAlarmReminder) {
            hideNotification(alarm?.id ?: 0)
        } else {
            hideTimerNotification()
        }
        hideNotificationHandler.removeCallbacksAndMessages(null)
    }

    private fun snoozeClicked() {
        if (config.useSameSnooze) {
            setupAlarmClock(alarm!!, config.snoozeTime * MINUTE_SECONDS)
            finishActivity()
        } else {
            showPickSecondsDialog(config.snoozeTime * MINUTE_SECONDS, true, cancelCallback = { finishActivity() }) {
                config.snoozeTime = it / MINUTE_SECONDS
                setupAlarmClock(alarm!!, it)
                finishActivity()
            }
        }
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(0, 0)
    }
}
