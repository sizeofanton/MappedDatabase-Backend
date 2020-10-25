package com.sizeofanton.mdbbackend.modules

import com.google.gson.Gson
import com.sizeofanton.mdbbackend.db.CreateRetCode
import com.sizeofanton.mdbbackend.db.DataBaseHelper
import com.sizeofanton.mdbbackend.db.RetCode
import com.sizeofanton.mdbbackend.pojo.Event
import com.sizeofanton.mdbbackend.pojo.EventType
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
fun Application.locationRouting(testing: Boolean) {
    routing {
        val dataBaseHelper: DataBaseHelper by inject()
        val userRepository: UserRepository by inject()
        val eventRepository: EventRepository by inject()

        post("/db/locations/add") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }
            val title = call.request.queryParameters["title"]
            val latitude = call.request.queryParameters["latitude"]
            val longitude = call.request.queryParameters["longitude"]
            if (title == null || latitude == null || longitude == null) {
                log.error("Adding location error - some parameters are invalid")
                return@post
            }

            when (dataBaseHelper.handleCreatingNewLocation(title, latitude, longitude)) {
                CreateRetCode.ALREADY_EXISTS -> call.respond(
                    HttpStatusCode.Conflict,
                    "Such location is already exists"
                )

                CreateRetCode.ERROR -> call.respond(
                    HttpStatusCode.InternalServerError,
                    "Error while creating new location"
                )

                CreateRetCode.SUCCESS -> {
                    if (!testing) {
                        val event = Event(
                            EventType.LOCATION_ADDED,
                            listOf(
                                userRepository.getUserByToken(token!!).name,
                                title
                            )
                        )
                        eventRepository.putEvent(token, event)
                    }
                    call.respond(
                        HttpStatusCode.OK,
                        "New location created successfully"
                    )
                }
            }

        }

        post("/db/locations/remove") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }
            val idToRemove = call.request.queryParameters["id"]
            if (idToRemove == null) {
                log.error("Removing location error - some parameters are invalid")
                return@post
            }

            val locationTitle = dataBaseHelper.handleGetLocationTitleById(idToRemove)
            when (dataBaseHelper.handleRemovingLocation(idToRemove)) {
                RetCode.ERROR -> call.respond(
                    HttpStatusCode.BadRequest,
                    "Error while removing location"
                )

                RetCode.SUCCESS -> {
                    if (!testing) {
                        val event = Event(
                            EventType.LOCATION_REMOVED,
                            listOf(
                                userRepository.getUserByToken(token!!).name,
                                locationTitle
                            )
                        )
                        eventRepository.putEvent(token, event)
                    }
                    call.respond(
                        HttpStatusCode.OK,
                        "Location removed successfully"
                    )
                }
            }
        }

        post("/db/locations/edit") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }
            val idToEdit = call.request.queryParameters["id"]
            val title = call.request.queryParameters["title"]
            val latitude = call.request.queryParameters["latitude"]
            val longitude = call.request.queryParameters["longitude"]

            if (idToEdit == null || title == null || latitude == null || longitude == null) {
                log.error("Editing location error - some parameters are invalid")
                return@post
            }

            when (dataBaseHelper.handleEditLocation(idToEdit, title, latitude, longitude)) {
                RetCode.SUCCESS -> call.respond(
                    HttpStatusCode.OK,
                    "Location successfully edited"
                )
                RetCode.ERROR -> call.respond(
                    HttpStatusCode.InternalServerError,
                    "Error while editing location"
                )
            }
        }

        get("/db/locations/get") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@get
                }
            }

            val ret = dataBaseHelper.handleGetLocations()
            if (ret.first == RetCode.SUCCESS) {
                call.respond(HttpStatusCode.OK, Gson().toJson(ret.second))
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Error while getting locations list")
            }

        }

        get("/db/{token}/locations/colors") {
            val token = call.parameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@get
                }
            }

            val ret = dataBaseHelper.handleGetColors()
            if (ret.first == RetCode.SUCCESS) {
                call.respond(HttpStatusCode.OK, Gson().toJson(ret.second))
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Error while getting locations colors")
            }
        }

    }
}