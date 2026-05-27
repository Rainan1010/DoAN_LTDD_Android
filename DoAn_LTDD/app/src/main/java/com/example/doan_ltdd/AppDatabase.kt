package com.example.doan_ltdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SavingsGoal::class, DepositLog::class, NotificationLog::class, Category::class, User::class], version = 5, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savingsDao(): SavingsDAO
    abstract fun notificationDao(): NotificationDAO
    abstract fun categoryDao(): CategoryDAO
    abstract fun userDao(): UserDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "savings_app_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}