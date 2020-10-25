package com.sizeofanton.mdbbackend.modules

import com.sizeofanton.mdbbackend.localization.Language
import com.sizeofanton.mdbbackend.repository.UserRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.inject

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.firebaseRouting(testing: Boolean) {
    val userRepository: UserRepository by inject()
    routing {
        post("/register_firebase_token"){
            val token = call.request.queryParameters["token"]
            val firebaseToken = call.request.queryParameters["firebase_token"]
            if (token == null || firebaseToken == null) {
                log.error("Registering firebase token error - some parameters are invalid")
                return@post
            }

            userRepository.assignFirebaseToken(token, firebaseToken)
        }

        post("/set_user_language") {
            val token = call.request.queryParameters["token"]
            val language = call.request.queryParameters["language"]

            if (token == null || language == null) {
                log.error("Specifying user's language error - some parameters are invalid")
                return@post
            }

            val langCode = when(language) {
                "ru" -> Language.RU
                "en" -> Language.EN
                else -> Language.EN
            }
            userRepository.assignUserLanguage(token, langCode)
        }
    }
}
