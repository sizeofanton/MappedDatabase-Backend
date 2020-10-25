package com.sizeofanton.mdbbackend.localization

import com.sizeofanton.mdbbackend.pojo.Event
import com.sizeofanton.mdbbackend.pojo.EventType

fun parseEvent(event: Event, language: Language): Pair<String, String> {
    val title: String
    val text: String
    val localizer = PushLocalizer()

    when (event.type) {
        EventType.ITEM_ADDED -> {
            title = localizer.getString(language, "notification_title_item_added", arrayOf<String>())
            text = localizer.getString(
                language,
                "notification_item_added",
                arrayOf(event.data[0]!!, event.data[1]!!, event.data[2]!!, event.data[3]!!)
            )
        }
        EventType.ITEM_REMOVED -> {
            title = localizer.getString(language, "notification_title_item_deleted", arrayOf<String>())
            text = localizer.getString(
                language,
                "notification_item_deleted",
                arrayOf(event.data[0]!!, event.data[1]!!, event.data[2]!!)
            )
        }
        EventType.ITEM_EDITED -> {
            title = localizer.getString(language, "notification_title_item_edited", arrayOf<String>())
            text = localizer.getString(
                language,
                "notification_item_edited",
                arrayOf(event.data[0]!!, event.data[1]!!, event.data[2]!!)
            )
        }
        EventType.LOCATION_ADDED -> {
            title = localizer.getString(language, "notification_title_location_added", arrayOf<String>())
            text = localizer.getString(
                language,
                "notification_location_added",
                arrayOf(event.data[0]!!, event.data[1]!!)
            )
        }
        EventType.LOCATION_REMOVED -> {
            title = localizer.getString(language, "notification_title_location_deleted", arrayOf<String>())
            text = localizer.getString(
                language,
                "notification_location_deleted",
                arrayOf(event.data[0]!!, event.data[1]!!)
            )
        }

        EventType.LOCATION_STATUS_CHANGED -> {
            title = localizer.getString(language, "notification_title_location_changed", arrayOf<String>())
            text = localizer.getString(
                language,
                "notification_location_status",
                arrayOf(event.data[0]!!, event.data[1]!!)
            )
        }
    }


    return (title to text)
}