package com.sizeofanton.mdbbackend

import com.sizeofanton.mdbbackend.localization.Language
import com.sizeofanton.mdbbackend.localization.PushLocalizer
import org.junit.Test
import kotlin.test.assertEquals

class PushLocalizerTest {
    @Test
    fun pushLocalizerTest_RU() {
        val pushLocalizer = PushLocalizer()
        val text = pushLocalizer.getString(
            Language.RU,
            "notification_item_added",
            arrayOf("User", "Item", "25", "Location")
        )
        assertEquals(
            "Пользователь User добавил предмет Item в количестве 25 в локацию - Location",
            text
        )
    }

    @Test
    fun pushLocalizerTest_EN() {
        val pushLocalizer = PushLocalizer()
        val text = pushLocalizer.getString(
            Language.EN,
            "notification_location_status",
            arrayOf("Loc1", "GREEN")
        )
        assertEquals(
            "Location Loc1 status is now GREEN",
            text
        )
    }
}