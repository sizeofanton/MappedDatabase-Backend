package com.sizeofanton.mdbbackend

import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ApplicationTest {

    @Test
    fun stage1_testGson() {
        withTestApplication({module(testing=true)}) {
            handleRequest(HttpMethod.Get, "/json/gson").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("{\"gson\":\"ok\"}", response.content)
            }
        }
    }

}
