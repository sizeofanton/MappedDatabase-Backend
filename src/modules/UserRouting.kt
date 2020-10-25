package com.sizeofanton.mdbbackend.modules

import com.google.gson.Gson
import com.sizeofanton.mdbbackend.db.CreateRetCode
import com.sizeofanton.mdbbackend.db.DataBaseHelper
import com.sizeofanton.mdbbackend.db.RetCode
import com.sizeofanton.mdbbackend.extensions.md5
import com.sizeofanton.mdbbackend.repository.EventRepository
import com.sizeofanton.mdbbackend.repository.UserRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.inject

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.userRouting(testing: Boolean) {
    routing {
        val dataBaseHelper: DataBaseHelper by inject()
        val userRepository: UserRepository by inject()
        val eventRepository: EventRepository by inject()

        post("/db/users/add") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }
            val user = call.request.queryParameters["user"]
            val password = call.request.queryParameters["password"]
            val isAdmin = call.request.queryParameters["is_admin"]

            if (user == null || password == null) {
                log.error("Username or password is NULL")
                return@post
            }

            if (isAdmin != "1" && isAdmin != "0") {
                log.error("Wrong is admin boolean value")
                return@post
            }

            when (dataBaseHelper.createNewUser(user, password.md5(), isAdmin)) {
                CreateRetCode.ALREADY_EXISTS -> call.respond(
                    HttpStatusCode.BadRequest,
                    "Such user already exists"
                )
                CreateRetCode.SUCCESS -> call.respond(
                    HttpStatusCode.OK,
                    "User created"
                )
                CreateRetCode.ERROR -> call.respond(
                    HttpStatusCode.InternalServerError,
                    "Error while "
                )
            }
        }

        post("/db/users/remove") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }
            val idToRemove = call.request.queryParameters["id"]
            if (idToRemove == null) {
                log.error("User removing error - id is null")
                return@post
            }

            when (dataBaseHelper.removeUser(idToRemove)) {
                RetCode.SUCCESS -> call.respond(HttpStatusCode.OK, "User deleted")
                RetCode.ERROR -> call.respond(HttpStatusCode.NotFound, "No such user to delete")
            }
        }

        post("/db/users/edit") {

            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }
            val idToEdit = call.request.queryParameters["id"]
            if (idToEdit == null) {
                log.error("User editing error - id is null")
                return@post
            }

            val password = call.request.queryParameters["password"]
            val isAdmin = call.request.queryParameters["is_admin"]
            val isActive = call.request.queryParameters["is_active"]

            if (password == null || isAdmin == null || isActive == null) {
                log.error("User editing error - some parameters is invalid")
                return@post
            }


            when (dataBaseHelper.editUser(idToEdit, password, isAdmin, isActive)) {
                RetCode.SUCCESS -> call.respond(HttpStatusCode.OK, "User edited")
                RetCode.ERROR -> call.respond(HttpStatusCode.Conflict, "Error while editing user")
            }
        }

        get("/db/users/get") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@get
                }
            }
            val ret = dataBaseHelper.getUsers()
            if (ret.first == RetCode.SUCCESS) {
                val gson = Gson()
                call.respond(HttpStatusCode.OK, gson.toJson(ret.second))
            } else {
                call.respond(HttpStatusCode.BadRequest, "Error while getting users list")
            }
        }



    }
}