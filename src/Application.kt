package com.sizeofanton.mdbbackend

import com.mysql.cj.log.Slf4JLogger
import com.sizeofanton.mdbbackend.db.DataBaseHelper
import com.sizeofanton.mdbbackend.di.appModule
import com.sizeofanton.mdbbackend.di.testingModule
import com.sizeofanton.mdbbackend.firebase.FirebaseDispatcher
import com.sizeofanton.mdbbackend.modules.*
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

fun main(args: Array<String>){

    io.ktor.server.netty.EngineMain.main(args)
}

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(
    testing: Boolean = environment
        .config
        .propertyOrNull("ktor.application.testing")?.getString()?.toBoolean() ?: false
) {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost()
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(Koin) {
        Slf4JLogger("Logger")
        if (!testing) modules(appModule)
        else modules(testingModule)
        val dataBaseHelper: DataBaseHelper = get()
        dataBaseHelper.initRecolorTimer()
        val firebaseDispatcher: FirebaseDispatcher = get()
        firebaseDispatcher.start()
    }

    routing {
        loginRouting()
        userRouting(testing)
        locationRouting(testing)
        itemsRouting(testing)
        notificationRouting(testing)
        firebaseRouting(testing)
        testRouting()
    }
}


