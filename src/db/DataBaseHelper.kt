package com.sizeofanton.mdbbackend.db

import com.sizeofanton.mdbbackend.pojo.*
import com.sizeofanton.mdbbackend.repository.EventRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.util.*

enum class LoginRetCode{
    LOGIN_SUCCESS,
    LOGIN_SUCCESS_ADMIN,
    USER_IS_BANNED,
    NO_SUCH_USER,
    WRONG_PASSWORD
}

enum class CreateRetCode {
    SUCCESS,
    ALREADY_EXISTS,
    ERROR
}

enum class RetCode {
    SUCCESS,
    ERROR
}

open class DataBaseHelper: KoinComponent {

    protected var connection: Connection? = null
    protected val USER = "YOUR_ACCOUNT"
    protected val PASSWORD = "YOUR_PASSWORD"
    private val eventRepository: EventRepository by inject()

    init {
        connection = DriverManager.getConnection("jdbc:mysql://localhost/mappeddatabase?useUnicode=true&serverTimezone=UTC",
            USER,
            PASSWORD)
    }

    fun initRecolorTimer() {
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                recolorLocations()
            }
        }
        timer.schedule(task, 0, 5000)
    }

    fun userLogin(user: String, password: String): LoginRetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val resultSet: ResultSet = statement.executeQuery("SELECT user_name,user_password,user_is_admin,user_is_active " +
                                                                "FROM users WHERE user_name='$user';")
        var passwordFromDb = ""
        var userActive: Boolean = false
        var userAdmin: Boolean = false
        while (resultSet.next()){
            passwordFromDb = resultSet.getString("user_password")
            userActive = resultSet.getBoolean("user_is_active")
            userAdmin = resultSet.getBoolean("user_is_admin")
        }

        return when {
            passwordFromDb.equals("") -> LoginRetCode.NO_SUCH_USER
            !userActive -> LoginRetCode.USER_IS_BANNED
            passwordFromDb.equals(password) && userAdmin -> LoginRetCode.LOGIN_SUCCESS_ADMIN
            passwordFromDb.equals(password)  -> LoginRetCode.LOGIN_SUCCESS
            else -> LoginRetCode.WRONG_PASSWORD
        }
    }

    fun createNewUser(user: String, password: String, admin: String): CreateRetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val resultSet: ResultSet = statement.executeQuery("SELECT SUM(user_id) FROM users WHERE user_name='$user'")
        while (resultSet.next()){
            val count = resultSet.getInt(1)
            if (count != 0) return CreateRetCode.ALREADY_EXISTS
        }

        val ret = statement.executeUpdate("INSERT INTO users (user_name, user_password, user_is_admin, user_is_active) " +
                                               "VALUES ('$user','$password','$admin','1');")
        statement.close()
        return when (ret) {
            in 0..Int.MAX_VALUE -> CreateRetCode.SUCCESS
            else -> CreateRetCode.ERROR
        }
    }

    fun removeUser(id: String): RetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val ret = statement.executeUpdate("DELETE FROM users WHERE user_id=$id")
        statement.close()
        return if (ret == 1) RetCode.SUCCESS
        else RetCode.ERROR
    }

    fun editUser(id: String,
                 newUserPassword: String,
                 admin: String,
                 active: String) : RetCode
    {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()

        if (newUserPassword.equals("0")){
            val ret = statement.executeUpdate("UPDATE users SET user_is_admin=$admin, user_is_active=$active WHERE user_id=$id;")
            statement.close()
            return if (ret == 1) RetCode.SUCCESS
            else RetCode.ERROR
        } else {
            val ret = statement.executeUpdate("UPDATE users SET user_password='$newUserPassword', user_is_admin=$admin, " +
                    "user_is_active=$active WHERE user_id=$id;")
            statement.close()
            return if (ret == 1) RetCode.SUCCESS
            else RetCode.ERROR
        }
    }

    fun getUsers(): Pair<RetCode, List<UserInfo>> {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT user_id,user_name,user_is_admin,user_is_active FROM users")

        val usersList = mutableListOf<UserInfo>()
        while (resultSet.next()){
            usersList.add(
                UserInfo(
                    userId = resultSet.getInt(1),
                    userName = resultSet.getString(2),
                    isActive = resultSet.getBoolean(4),
                    isAdmin = resultSet.getBoolean(3)
                )
            )
        }

        return Pair(RetCode.SUCCESS, usersList)
    }

    fun getUseByName(name: String): UserInfo {
        if (connection == null) throw java.lang.Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT user_id,user_is_admin,user_is_active" +
                " FROM users WHERE user_name='$name';")
        var user: UserInfo? = null
        while(resultSet.next()) {
            user = UserInfo(
                resultSet.getInt(1),
                name,
                resultSet.getBoolean(2),
                resultSet.getBoolean(3)
            )
        }
        return user!!
    }

    fun getUserPassMd5ById(id: Int): String {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT user_password FROM users WHERE user_id='$id';")
        var passMd5 = ""
        while(resultSet.next()) {
            passMd5 = resultSet.getString(1)
        }
        return passMd5
    }

    fun handleCreatingNewLocation(title: String, latitude: String, longitude: String): CreateRetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT SUM(location_title) FROM locations " +
                                                "WHERE location_title='$title'")
        while (resultSet.next()){
            val count = resultSet.getInt(1)
            if (count != 0) return CreateRetCode.ALREADY_EXISTS
        }

        val ret = statement.executeUpdate("INSERT INTO locations (location_title, location_latitude, location_longitude) " +
                "VALUES ('$title',$latitude,$longitude);")
        return if (ret == 1) CreateRetCode.SUCCESS
        else CreateRetCode.ERROR
    }

    fun handleRemovingLocation(id: String): RetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val ret = statement.executeUpdate("DELETE FROM locations WHERE location_id=$id;")
        return if (ret == 1) RetCode.SUCCESS
        else RetCode.ERROR
    }

    fun handleEditLocation(id: String, title: String, latitude: String, longitude: String) : RetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val ret = statement.executeUpdate("UPDATE locations SET location_title='$title', location_latitude=$latitude," +
                " location_longitude=$longitude WHERE location_id=$id;")
        return if (ret == 1) RetCode.SUCCESS
        else RetCode.ERROR
    }

    fun handleGetLocations(): Pair<RetCode, List<LocationInfo>> {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT location_id, location_title, location_color, " +
                                                "location_latitude, location_longitude FROM locations")
        val locationsList = mutableListOf<LocationInfo>()

        while (resultSet.next()) {
            locationsList.add(
                LocationInfo(
                    id = resultSet.getInt(1),
                    title = resultSet.getString(2),
                    color = resultSet.getInt(3),
                    latitude = resultSet.getDouble(4),
                    longitude = resultSet.getDouble(5)
                )
            )
        }

        return Pair(RetCode.SUCCESS, locationsList)

    }

    fun handleCreateNewItem(locationId: String, title: String, itemCount: String, isRequired: String): CreateRetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT COUNT(item_title) FROM items " +
                                                    "WHERE location_id=$locationId AND item_title='$title';")
        while (resultSet.next()){
            val count = resultSet.getInt(1)
            if (count != 0) return CreateRetCode.ALREADY_EXISTS
        }

        val ret = statement.executeUpdate("INSERT INTO items (location_id, item_title, item_count, item_required) " +
                                                "VALUES ($locationId, '$title', $itemCount, $isRequired);")
        statement.close()
        return if (ret == 1) CreateRetCode.SUCCESS else CreateRetCode.ERROR
    }

    fun handleRemoveItem(id: String): RetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val ret = statement.executeUpdate("DELETE FROM items WHERE item_id=$id;")
        return if (ret == 1) RetCode.SUCCESS
        else RetCode.ERROR
    }

    fun handleEditItem(id: String, title: String, count: String, isRequired: String): RetCode {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()
        val ret = statement.executeUpdate("UPDATE items SET item_title='$title', item_count=$count," +
                " item_required=$isRequired WHERE item_id=$id;")
        return if (ret == 1) RetCode.SUCCESS
        else RetCode.ERROR
    }

    fun handleGetItems(locationId: String) : Pair<RetCode, List<ItemInfo>> {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()

        val resultSet = statement.executeQuery("SELECT item_id, item_title, item_count, item_required " +
                "FROM items WHERE location_id = $locationId")
        val itemsList = mutableListOf<ItemInfo>()

        while (resultSet.next()) {
            itemsList.add(
                ItemInfo(
                    id = resultSet.getInt(1),
                    title = resultSet.getString(2),
                    count = resultSet.getInt(3),
                    required = resultSet.getBoolean(4)
                )
            )
        }

        return Pair(RetCode.SUCCESS, itemsList)

    }

    fun handleGetColors(): Pair<RetCode, List<LocationColorInfo>> {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()

        val resultSet = statement.executeQuery("SELECT location_id, location_color FROM locations;")
        val colorsList = mutableListOf<LocationColorInfo>()

        while (resultSet.next()) {
            colorsList.add(
                LocationColorInfo(resultSet.getInt(1), resultSet.getInt(2))
            )
        }

        return Pair(RetCode.SUCCESS, colorsList)

    }

    fun handleGetLocationTitleById(id: String): String? {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()

        val resultSet = statement.executeQuery("SELECT location_title FROM locations WHERE location_id=$id;")
        var title = ""
        while (resultSet.next()) {
            title = resultSet.getString(1)
        }
        return if (title != "") title else null
    }

    fun handleGetItemTitleById(id: String): String? {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()

        val resultSet = statement.executeQuery("SELECT item_title FROM items WHERE item_id=$id")
        var title = ""
        while (resultSet.next()) {
            title = resultSet.getString(1)
        }
        return if (title != "") title else null
    }

    fun handleGetLocationTitleByItemId(id: String): String? {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()

        var resultSet = statement.executeQuery("SELECT location_id FROM items WHERE item_id=$id")
        var locId = ""
        while (resultSet.next()) {
            locId = resultSet.getString(1)
        }



        resultSet = statement.executeQuery("SELECT location_title FROM locations WHERE location_id=$locId")
        var locTitle = ""
        while (resultSet.next()) {
            locTitle = resultSet.getString(1)
        }

        return if (locTitle != "") locTitle else null
    }

    fun recolorLocations() {
        if (connection == null) throw Exception("Database connection is null")
        val statement: Statement = connection!!.createStatement()

        var resultSet = statement.executeQuery("SELECT location_id FROM locations;")
        val locationsIdList = mutableListOf<Int>()

        while (resultSet.next()) {
            locationsIdList.add(resultSet.getInt(1))
        }

        resultSet = statement.executeQuery("SELECT location_color FROM locations;")
        val oldLocationsColors = mutableListOf<Int>()

        while (resultSet.next()) {
            oldLocationsColors.add(resultSet.getInt(1))
        }

        for ((i,location) in locationsIdList.withIndex()) {
            val resultSetRequired = statement.executeQuery("SELECT COUNT(item_id) FROM items WHERE location_id=$location " +
                    "AND item_required='1';")
            var required: Int = 0
            while (resultSetRequired.next()) {
                required = resultSetRequired.getInt(1)
            }

            val resultSetTotal = statement.executeQuery("SELECT COUNT(item_id) FROM items WHERE location_id=$location")
            var total: Int = 1
            while (resultSetTotal.next()) {
                total = resultSetTotal.getInt(1)
            }

            val percent: Double = required.toDouble() / total.toDouble()
            val newColor = when {
                percent >= 0.5 -> 2
                percent >= 0.25 && percent < 0.5 -> 1
                else -> 0
            }

            if (oldLocationsColors[i] != newColor)
                eventRepository.putEventNoExceptions(
                    Event(
                        EventType.LOCATION_STATUS_CHANGED,
                        listOf(
                            handleGetLocationTitleById(location.toString()),
                            newColor.toString()
                        )
                    )
                )

            statement.executeUpdate("UPDATE locations SET location_color='$newColor' WHERE location_id=$location")

        }

        statement.close()
    }
}