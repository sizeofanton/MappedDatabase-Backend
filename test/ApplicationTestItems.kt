package com.sizeofanton.mdbbackend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sizeofanton.mdbbackend.pojo.ItemInfo
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.koin.core.KoinComponent

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ApplicationTestItems: KoinComponent {

    @Test
    fun stage1_testGetItems() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<ItemInfo>>(){}.type
            val itemsList: List<ItemInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/items/get?token=TOKEN&location_id=1").response.content, type)
            assertEquals(1, itemsList.size)
        }
    }

    @Test
    fun stage2_testCreateItem() {
        withTestApplication({module(testing=true)}) {
            handleRequest(
                HttpMethod.Post,
                "/db/items/add?token=TOKEN&location_id=1&title=test_item&count=1&is_required=1"
            ).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun stage3_testEditItem() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<ItemInfo>>(){}.type
            val itemList: List<ItemInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/items/get?token=TOKEN&location_id=1").response.content, type)
            val idToEdit = itemList[1].id
            println("ID TO EDIT - $idToEdit")
            handleRequest(
                HttpMethod.Post,
                "/db/items/edit?token=TOKEN&id=$idToEdit&title=test_item_edited&count=2&is_required=0"
            ).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun stage4_testRemoveItem() {
        withTestApplication({module(testing=true)}) {
            val type = object : TypeToken<List<ItemInfo>>(){}.type
            val itemList: List<ItemInfo> = Gson()
                .fromJson(handleRequest(HttpMethod.Get, "/db/items/get?token=TOKEN&location_id=1").response.content, type)
            val idToRemove = itemList[1].id
            println("ID TO REMOVE - $idToRemove")
            handleRequest(HttpMethod.Post, "/db/items/remove?id=$idToRemove").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

}