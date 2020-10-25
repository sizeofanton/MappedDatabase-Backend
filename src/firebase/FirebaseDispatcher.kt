package com.sizeofanton.mdbbackend.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.sizeofanton.mdbbackend.localization.parseEvent
import com.sizeofanton.mdbbackend.repository.EventRepository
import com.sizeofanton.mdbbackend.repository.UserRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.FileInputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class FirebaseDispatcher: KoinComponent {
    private val eventRepository: EventRepository by inject()
    private val userRepository: UserRepository by inject()

    private val executor = Executors.newSingleThreadScheduledExecutor()

    fun start() {
        val serviceAccount = FileInputStream("serviceAccount.json")
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://mappeddatabase.firebaseio.com")
            .build()

        FirebaseApp.initializeApp(options)

        executor.scheduleAtFixedRate({
            for (token in eventRepository.getTokens()) {
                val event = eventRepository.getEvent(token)
                if (event != null) {
                    val user = userRepository.getUserByToken(token)
                    val parsedEvent = parseEvent(event, user.language)
                    send(parsedEvent.first, parsedEvent.second, user.firebaseToken)
                }
            }
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun send(title: String, body: String, token: String): String {
        val message = Message.builder()
            .setToken(token)
            .setNotification(Notification.builder().setTitle(title).setBody(body).build())
            .build()
        return FirebaseMessaging.getInstance().sendAsync(message).get()
    }

}