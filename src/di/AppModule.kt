package com.sizeofanton.mdbbackend.di

import com.sizeofanton.mdbbackend.db.DataBaseHelper
import com.sizeofanton.mdbbackend.db.DataBaseHelperTest
import com.sizeofanton.mdbbackend.firebase.FirebaseDispatcher
import com.sizeofanton.mdbbackend.repository.EventRepository
import com.sizeofanton.mdbbackend.repository.UserRepository
import org.koin.dsl.module

val appModule = module {
    single { DataBaseHelper() }
    single { EventRepository() }
    single { UserRepository() }
    single { FirebaseDispatcher() }
}

val testingModule = module {
    single<DataBaseHelper> { DataBaseHelperTest() }
    single { EventRepository() }
    single { UserRepository() }
}