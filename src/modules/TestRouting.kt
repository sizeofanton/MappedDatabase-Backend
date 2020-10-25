package com.sizeofanton.mdbbackend.modules

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.testRouting() {
    routing {

        get("/json/gson") {
            call.respond(mapOf("gson" to "ok"))
        }

    }
}