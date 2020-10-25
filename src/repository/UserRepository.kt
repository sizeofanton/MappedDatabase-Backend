package com.sizeofanton.mdbbackend.repository

import com.sizeofanton.mdbbackend.localization.Language
import com.sizeofanton.mdbbackend.pojo.User
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UserRepository: KoinComponent {

    private val activeUsers = mutableListOf<User>()
    private val eventRepository: EventRepository by inject()

    init {
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.scheduleAtFixedRate({
            removeUnusedTokens()
        }, 0, 1, TimeUnit.MINUTES)
    }

    fun addActiveUser(activeUser: User) {
        activeUsers.add(activeUser.apply { tokenLastUsage = System.currentTimeMillis() })
        eventRepository.initQueue(activeUser.token)
    }


    fun assignFirebaseToken(token: String, firebaseToken: String) {
        activeUsers.find { it.token == token }?.firebaseToken = firebaseToken
    }

    fun assignUserLanguage(token: String, language: Language) {
        activeUsers.find { it.token == token }?.language = language
    }

    fun checkIfUserActive(user: String): Boolean {
        return activeUsers.filter { it.token == user }.count() == 1
    }

    fun checkAccessPermission(token: String): Boolean {
        var tokenIsValid = false
        for (user in activeUsers) {
            if (user.token == token) {
                tokenIsValid = true
                user.tokenLastUsage = System.currentTimeMillis()
                break
            }
        }
        return tokenIsValid
    }

    fun removeActiveUser(user: String) {
        activeUsers.removeIf { it.token == user }
    }

    private fun removeUnusedTokens() {
        for (user in activeUsers) {
            if ((System.currentTimeMillis() - user.tokenLastUsage) >= 3600000) {
                activeUsers.removeIf { it.token == user.token }
            }
        }
    }

    fun getUserByToken(token: String): User {
        return activeUsers.first { it.token == token }
    }

}