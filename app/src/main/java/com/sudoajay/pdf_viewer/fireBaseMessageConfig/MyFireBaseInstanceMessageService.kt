package com.sudoajay.pdf_viewer.fireBaseMessageConfig

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sudoajay.pdf_viewer.R

class MyFireBaseInstanceMessageService : FirebaseMessagingService() {
    private var notificationManager: NotificationManager? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val url = data["Url"]
        //Check if the message contains notification
        if (remoteMessage.notification != null) {
            sendNotification(remoteMessage.notification!!.body, url)
        }
    }

    /**
     * Dispay the notification
     */
    private fun sendNotification(body: String?, url: String?) {
        val channelId = "Information"
        val title = getString(R.string.app_name)
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        val pendingIntent = PendingIntent.getActivity(this, 0 /*Request code*/, i, PendingIntent.FLAG_ONE_SHOT)
        //Set sound of notification
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (notificationManager == null) {
            notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        // this check for android Oero In which Channel Id Come as New Feature
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            assert(notificationManager != null)
            var mChannel = notificationManager!!.getNotificationChannel(channelId)
            if (mChannel == null) {
                mChannel = NotificationChannel(channelId, title, importance)
                notificationManager!!.createNotificationChannel(mChannel)
            }
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT < 18)
            builder.setSmallIcon(R.drawable.internal_storage_icon).color =
                    ContextCompat.getColor(applicationContext, R.color.colorPrimary)



        notificationManager!!.notify(0 /*ID of notification*/, builder.build())
    }
}