package ltd.evilcorp.atox.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.activity.ChatActivity
import ltd.evilcorp.atox.vo.Contact
import javax.inject.Inject
import javax.inject.Singleton

private const val MESSAGE_CHANNEL = "aTox messages"

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val name = context.getString(R.string.messages)
        val descriptionText = context.getString(R.string.messages_incoming)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(MESSAGE_CHANNEL, name, importance).apply {
            description = descriptionText
        }

        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifier.createNotificationChannel(channel)
    }

    fun showMessageNotification(contact: Contact, message: String) {
        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra("publicKey", contact.publicKey)
        }

        val notificationBuilder = NotificationCompat.Builder(context, MESSAGE_CHANNEL)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(contact.name)
            .setContentText(message)
            .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setAutoCancel(true)

        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifier.notify(contact.publicKey.hashCode(), notificationBuilder.build())
    }
}
