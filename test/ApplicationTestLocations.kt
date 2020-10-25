package com.sizeofanton.mdbbackend
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sizeofanton.mdbbackend.pojo.LocationColorInfo
import com.sizeofanton.mdbbackend.pojo.LocationInfo
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ApplicationTestLocations {

    @Test
    fun stage1_testGetLocations() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<LocationInfo>>(){}.type
            val locationsList: List<LocationInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/locations/get?token=TOKEN").response.content, type)
            assertEquals(1, locationsList.size)
        }
    }

    @Test
    fun stage2_testAddLocation() {
        withTestApplication({module(testing = true)}) {
            handleRequest(
                HttpMethod.Post,
                "/db/locations/add?token=TOKEN&title=testloc2&latitude=22.3&longitude=33.9"
            ).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun stage3_testEditLocation() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<LocationInfo>>(){}.type
            val locationsList: List<LocationInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/locations/get?token=TOKEN").response.content, type)
            val idToEdit = locationsList[1].id
            handleRequest(
                HttpMethod.Post,
                "/db/locations/edit?token=TOKEN&id=$idToEdit&title=editedlocation&latitude=1.0&longitude=1.0"
            ).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun stage4_testDeleteLocation() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<LocationInfo>>(){}.type
            val locationsList: List<LocationInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/locations/get?token=TOKEN").response.content, type)
            val idToDelete = locationsList[1].id
            handleRequest(
                HttpMethod.Post,
                "/db/locations/remove?token=TOKEN&id=$idToDelete"
            ).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun stage5_testGetColors() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<LocationColorInfo>>(){}.type
            val locationsColorList: List<LocationColorInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/token/locations/colors").response.content, type)
            assertEquals(1, locationsColorList.size)
        }
    }

}