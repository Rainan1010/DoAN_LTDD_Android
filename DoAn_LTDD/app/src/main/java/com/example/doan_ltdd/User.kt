package com.example.doan_ltdd

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val username: String,
    val password: String,
    val fullName: String,
    val email: String,
    val role: String = "USER" // [MỚI] Mặc định là USER. Giá trị: "ADMIN" hoặc "USER"
)