package com.sizeofanton.mdbbackend.modules

import com.google.gson.Gson
import com.sizeofanton.mdbbackend.db.CreateRetCode
import com.sizeofanton.mdbbackend.db.DataBaseHelper
import com.sizeofanton.mdbbackend.db.LoginRetCode
import com.sizeofanton.mdbbackend.db.RetCode
import com.sizeofanton.mdbbackend.extensions.md5
import com.sizeofanton.mdbbackend.extensions.sha256
import com.sizeofanton.mdbbackend.pojo.AuthInfo
import com.sizeofanton.mdbbackend.pojo.User
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
import org.intellij.lang.annotations.Language
import org.koin.ktor.ext.inject

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.loginRouting() {
    routing {
        val dataBaseHelper: DataBaseHelper by inject()
        val userRepository: UserRepository by inject()


        get("/login") {
            val user = call.request.queryParameters["user"]
            val password = call.request.queryParameters["password"]

            if (user == null || password == null) {
                log.error("Username or password is NULL")
                return@get
            }
            log.info("User '$user' is trying to login")
            val hashedPassword = password.md5()
            when (dataBaseHelper.userLogin(user, hashedPassword)) {
                LoginRetCode.LOGIN_SUCCESS -> {
                    val token = (user + password + java.util.Date().toString()).sha256()
                    userRepository.addActiveUser(
                        User(
                            name = user,
                            token = token,
                            firebaseToken = "",
                            language = com.sizeofanton.mdbbackend.localization.Language.EN,
                            tokenLastUsage = System.currentTimeMillis()
                        )
                    )
                    call.respond(
                        HttpStatusCode.OK,
                        Gson().toJson(AuthInfo(token, false), AuthInfo::class.java)
                    )

                }
                LoginRetCode.LOGIN_SUCCESS_ADMIN -> {
                    val token = (user + password + java.util.Date().toString()).sha256()
                    userRepository.addActiveUser(
                        User(
                            name = user,
                            token = token,
                            firebaseToken = "",
                            language = com.sizeofanton.mdbbackend.localization.Language.EN,
                            tokenLastUsage = System.currentTimeMillis()
                        )
                    )
                    call.respond(
                        HttpStatusCode.OK,
                        Gson().toJson(AuthInfo(token, true), AuthInfo::class.java)
                    )

                }
                LoginRetCode.WRONG_PASSWORD -> call.respond(HttpStatusCode.Unauthorized, "Wrong password")
                LoginRetCode.NO_SUCH_USER -> call.respond(HttpStatusCode.NotFound, "No such user")
                LoginRetCode.USER_IS_BANNED -> call.respond(HttpStatusCode.Forbidden, "User is banned")
            }

        }

        post("/logout") {
            val user = call.request.queryParameters["user"]
            if (user != null) userRepository.removeActiveUser(user)
        }

        post("/change_pass") {
            val user = call.request.queryParameters["user"]
            val oldPassword = call.request.queryParameters["old_password"]
            val newPassword = call.request.queryParameters["new_password"]

            if (user == null || newPassword == null || oldPassword == null) {
                log.error("Some parameters for change user password is invalid")
                call.respond(HttpStatusCode.BadRequest, "Some input are invalid")
                return@post
            }

            log.info("User - $user changing password")

            val userToUpdate = dataBaseHelper.getUseByName(user)
            val passFromDb = dataBaseHelper.getUserPassMd5ById(userToUpdate.userId)

            if (passFromDb != oldPassword.md5()) {
                call.respond(HttpStatusCode.BadRequest, "Current password not match")
                return@post
            }

            when(dataBaseHelper.editUser(
                userToUpdate.userId.toString(),
                newPassword.md5(),
                userToUpdate.isActive.toString(),
                userToUpdate.isAdmin.toString())) {
                    RetCode.SUCCESS -> call.respond(HttpStatusCode.OK, "Password update successfully")
                    RetCode.ERROR -> call.respond(HttpStatusCode.InternalServerError, "Error while updating password")
            }

        }

        post("/register") {
            val user = call.request.queryParameters["user"]
            val password = call.request.queryParameters["password"]

            if (user == null || password == null) {
                log.error("Some parameters for registering new user are invalid")
                call.respond(HttpStatusCode.BadRequest, "Some input are invalid")
                return@post
            }

            log.info("User $user is registering")

            when(dataBaseHelper.createNewUser(user = user, password = password.md5(), admin = "0")) {
                CreateRetCode.SUCCESS -> call.respond(HttpStatusCode.OK, "You registered successfully")
                CreateRetCode.ALREADY_EXISTS -> call.respond(HttpStatusCode.BadRequest, "Such user already exists")
                CreateRetCode.ERROR -> call.respond(HttpStatusCode.InternalServerError, "Error while registering")
            }

        }

    }
}