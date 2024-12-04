package com.opsc7311poe.gourmetguru_opscpoe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.widget.Toast

class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Stop the alarm sound here
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(context, notificationSound)
        if (ringtone.isPlaying) {
            ringtone.stop()
        }

        Toast.makeText(context, "Notification dismissed, alarm stopped", Toast.LENGTH_SHORT).show()
    }
}