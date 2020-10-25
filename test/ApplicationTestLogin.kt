package com.sizeofanton.mdbbackend

import com.sizeofanton.mdbbackend.extensions.md5
import com.sizeofanton.mdbbackend.repository.UserRepository
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import org.junit.After
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.koin.core.KoinComponent
import org.koin.core.inject

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ApplicationTestLogin: KoinComponent {
    @Test
    fun stage1_testLogin() {
        withTestApplication({module(testing=true)}) {
            handleRequest(HttpMethod.Get, "/login?user=test&password=testpassword").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun stage2_testLogout() {
        withTestApplication({module(testing=true)}) {
            val userRepository: UserRepository by inject()
            handleRequest(HttpMethod.Get, "/login?user=test&password=testpassword")
            handleRequest(HttpMethod.Post, "/logout?user=test")
            assertEquals(false, userRepository.checkIfUserActive("test"))
        }
    }

    @Test
    fun stage3_passwordChange() {
        val oldPassword = "testpassword".md5()
        val newPassword = "testpassword1".md5()
        withTestApplication({module(testing=true)}) {
            handleRequest(HttpMethod.Post, "/change_pass?user=test&old_password=$oldPassword" +
                    "&new_password=$newPassword").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Post, "/change_pass?user=test&old_password=$newPassword" +
                    "&new_password=$oldPassword")
        }
    }
}