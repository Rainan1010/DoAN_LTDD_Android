package com.example.doan_ltdd

import androidx.room.*

@Dao
interface UserDAO {
    // Đăng ký: Thêm user mới
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User)

    // Đăng nhập: Kiểm tra username và password
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    // Kiểm tra user đã tồn tại chưa
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun checkUserExists(username: String): Int

    // 1. Lấy thông tin user theo username
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    // 2. Cập nhật thông tin user
    @Update
    suspend fun updateUser(user: User)
}