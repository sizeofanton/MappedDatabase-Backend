package com.sizeofanton.mdbbackend
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sizeofanton.mdbbackend.pojo.UserInfo
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ApplicationTestUsers {
    @Test
    fun stage1_testGetUsers() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<UserInfo>>(){}.type
            val usersList: List<UserInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/users/get?token=TOKEN").response.content, type)
            assertEquals(1, usersList.size)
        }
    }

    @Test
    fun stage2_testAddUser() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(
                HttpMethod.Post,
                "/db/users/add/?token=TOKEN&user=new_user&password=new_password&is_admin=1"
            ).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun stage3_testEditUser() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<UserInfo>>(){}.type
            val usersList: List<UserInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/users/get?token=TOKEN").response.content, type)
            val idToEdit = usersList.get(1).userId
            handleRequest(
                HttpMethod.Post,
                "/db/users/edit/?token=TOKEN&id=$idToEdit&password=0&is_admin=1&is_active=1"
            ).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun stage4_testRemoveUser() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<UserInfo>>(){}.type
            val usersList: List<UserInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/users/get?token=TOKEN").response.content, type)
            val idToDelete = usersList.get(1).userId
            handleRequest(HttpMethod.Post, "/db/users/remove?token=TOKEN&id=$idToDelete").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

}