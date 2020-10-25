package com.sizeofanton.mdbbackend.repository

import com.sizeofanton.mdbbackend.pojo.Event
import java.util.*
import kotlin.collections.HashMap

class EventRepository {

    private val userEvents = HashMap<String, Stack<Event>>()

    fun getEvent(token: String): Event? {
        return if (userEvents.containsKey(token) && userEvents[token]!!.isNotEmpty()) {
            userEvents[token]!!.pop()
        } else null
    }

    fun putEvent(exceptToken: String, e: Event) {
        val keys = userEvents.keys.filter { it != exceptToken }
        //val keys = userEvents.keys
        for (k in keys) {
            userEvents[k]?.push(e)
        }
    }

    fun putEventNoExceptions(e: Event) {
        val keys = userEvents.keys
        for (k in keys) {
            userEvents[k]?.push(e)
        }
    }

    fun initQueue(token: String) {
        if (userEvents[token] == null) userEvents[token] = Stack<Event>()
    }

    fun getTokens() = userEvents.keys

}