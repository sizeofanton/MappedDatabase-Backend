package com.sizeofanton.mdbbackend.db

import java.sql.DriverManager

class DataBaseHelperTest : DataBaseHelper() {
    init {
        connection = DriverManager.getConnection("jdbc:mysql://localhost/mappeddatabasetest?useUnicode=true&serverTimezone=UTC",
            USER,
            PASSWORD)
    }


    companion object {
        private var instance: DataBaseHelperTest? = null
        fun getInstance(): DataBaseHelper {
            if (instance == null) instance =
                DataBaseHelperTest()
            return instance!!
        }


    }
}