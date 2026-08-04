package com.safetywristband.tracker.di

import android.content.Context
import androidx.room.Room
import com.safetywristband.tracker.data.local.AlertDao
import com.safetywristband.tracker.data.local.AppDatabase
import com.safetywristband.tracker.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()
}
