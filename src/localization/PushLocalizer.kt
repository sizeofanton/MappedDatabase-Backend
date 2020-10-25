package com.sizeofanton.mdbbackend.localization

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

enum class Language {
    RU,
    EN
}

class PushLocalizer {
    fun getString(lang: Language, name: String, args: Array<String>): String {
        val xmlFile = when(lang) {
            Language.RU -> File("resources/strings-ru.xml")
            Language.EN -> File("resources/strings-en.xml")
        }
        val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile).apply {
            documentElement.normalize()
        }

        var localizedString = ""
        val elements = xmlDoc.getElementsByTagName("string")
        for (i in 0 until elements.length) {
            val itemName = elements.item(i).attributes.item(0).textContent
            if (itemName == name) {
                localizedString = elements.item(i).textContent
                break
            }
        }


        for (i in args.indices) {
            localizedString = localizedString.replace("%${i+1}", args[i])
        }

        return localizedString
    }
}