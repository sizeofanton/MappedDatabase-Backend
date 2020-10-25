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
fun Application.itemsRouting(testing: Boolean) {
    routing {
        val dataBaseHelper: DataBaseHelper by inject()
        val userRepository: UserRepository by inject()
        val eventRepository: EventRepository by inject()

        post("/db/items/add/") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }

            val locationId = call.request.queryParameters["location_id"]
            val itemTitle = call.request.queryParameters["title"]
            val itemCount = call.request.queryParameters["count"]
            val isRequired = call.request.queryParameters["is_required"]

            if (locationId == null || itemTitle == null || itemCount == null || isRequired == null) {
                log.error("Creating item error - some parameters are invalid")
                return@post
            }

            when (dataBaseHelper.handleCreateNewItem(locationId, itemTitle, itemCount, isRequired)) {
                CreateRetCode.ALREADY_EXISTS -> call.respond(
                    HttpStatusCode.BadRequest,
                    "Such item already exists"
                )
                CreateRetCode.SUCCESS -> {
                    if (!testing) {
                        val event = Event(
                            EventType.ITEM_ADDED,
                            listOf(
                                userRepository.getUserByToken(token!!).name,
                                itemTitle,
                                itemCount,
                                dataBaseHelper.handleGetLocationTitleById(locationId)
                            )
                        )

                        eventRepository.putEvent(token, event)
                    }
                    call.respond(
                        HttpStatusCode.OK,
                        "Item created"
                    )

                }

                CreateRetCode.ERROR -> call.respond(
                    HttpStatusCode.InternalServerError,
                    "Something went wrong during creating item"
                )
            }
        }

        post("/db/items/remove") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }

            val id = call.request.queryParameters["id"]
            if (id == null) {
                log.error("Removing items error - id is invalid")
                return@post
            }

            val itemTitle = dataBaseHelper.handleGetItemTitleById(id)
            val locationTitle = dataBaseHelper.handleGetLocationTitleByItemId(id)
            when (dataBaseHelper.handleRemoveItem(id)) {
                RetCode.ERROR -> call.respond(
                    HttpStatusCode.BadRequest,
                    "Error while removing item"
                )

                RetCode.SUCCESS -> {
                    if (!testing) {
                        val event = Event(
                            EventType.ITEM_REMOVED,
                            listOf(
                                userRepository.getUserByToken(token!!).name,
                                itemTitle,
                                locationTitle
                            )
                        )
                        eventRepository.putEvent(token, event)
                    }
                    call.respond(
                        HttpStatusCode.OK,
                        "Item removed successfully"
                    )
                }
            }
        }

        post("/db/items/edit") {
            val token = call.request.queryParameters["token"]

            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@post
                }
            }

            val id = call.request.queryParameters["id"]
            val title = call.request.queryParameters["title"]
            val count = call.request.queryParameters["count"]
            val isRequired = call.request.queryParameters["is_required"]

            if (id == null || title == null || count == null || isRequired == null) {
                log.error("Editing item error - some parameters are invalid")
                return@post
            }

            when (dataBaseHelper.handleEditItem(id, title, count, isRequired)) {
                RetCode.SUCCESS -> {
                    if (!testing) {
                        val event = Event(
                            EventType.ITEM_EDITED,
                            listOf(
                                userRepository.getUserByToken(token!!).name,
                                dataBaseHelper.handleGetItemTitleById(id),
                                dataBaseHelper.handleGetLocationTitleByItemId(id)
                            )
                        )
                        eventRepository.putEvent(token, event)
                    }
                    call.respond(
                        HttpStatusCode.OK,
                        "Item successfully edited"
                    )
                }
                RetCode.ERROR -> call.respond(
                    HttpStatusCode.InternalServerError,
                    "Error while editing item"
                )
            }

        }

        get("/db/items/get") {
            val token = call.request.queryParameters["token"]
            if (!testing) {
                if (token == null || !userRepository.checkAccessPermission(token)) {
                    call.respond(HttpStatusCode.Forbidden, "Token is invalid")
                    return@get
                }
            }

            val locationId = call.request.queryParameters["location_id"]
            if (locationId == null) {
                log.error("Get items error - location id is invalid")
                return@get
            }

            val ret = dataBaseHelper.handleGetItems(locationId)
            if (ret.first == RetCode.SUCCESS) {
                call.respond(HttpStatusCode.OK, Gson().toJson(ret.second))
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Error while getting items list")
            }
        }

    }
}