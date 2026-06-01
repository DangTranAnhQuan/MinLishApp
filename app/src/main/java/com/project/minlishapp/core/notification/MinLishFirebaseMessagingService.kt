package com.project.minlishapp.core.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MinLishFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            NotificationHelper.showNotification(
                context = applicationContext,
                title = it.title,
                message = it.body
            )
        }
    }

    override fun onNewToken(token: String) {
        // Handle new token registration if needed
    }
}
