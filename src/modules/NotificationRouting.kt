package com.sizeofanton.mdbbackend.modules

import com.google.gson.Gson
import com.sizeofanton.mdbbackend.repository.EventRepository
import com.sizeofanton.mdbbackend.repository.UserRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.inject

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.notificationRouting(testing: Boolean) {
    routing {
        val userRepository: UserRepository by inject()
        val eventRepository: EventRepository by inject()
        get("/notification") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@get
                }
            }
            val event = eventRepository.getEvent(token!!)
            if (event == null) {
                call.respond(HttpStatusCode.NotFound)
            } else call.respond(HttpStatusCode.OK, Gson().toJson(event))
        }
    }
}