package com.kuba.calendarium.di

import android.content.Context
import com.kuba.calendarium.data.AppDatabase
import com.kuba.calendarium.data.dataStore.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import javax.inject.Singleton

const val TEST_PREFS_NAME = "TEST_USER_PREFS"

@Module
@TestInstallIn(
    components = [SingletonComponent::class], // Or appropriate component
    replaces = [AppModule::class] // Module that provides Dispatchers.Main or others
)
object TestModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) =
        AppDatabase.builder(context).build()

    @Provides
    @Singleton
    fun provideEventDao(appDatabase: AppDatabase) = appDatabase.eventDao()

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context) =
        UserPreferencesRepository(context, TEST_PREFS_NAME)

    @Provides
    fun provideTestDispatcher(): CoroutineDispatcher = StandardTestDispatcher()
}