package com.example.doan_ltdd

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>> // Dùng Flow để tự động cập nhật Spinner khi thêm mới

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int
}