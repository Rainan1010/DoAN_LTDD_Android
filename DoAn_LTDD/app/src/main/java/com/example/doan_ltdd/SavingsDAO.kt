package com.example.doan_ltdd

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDAO {
    // --- CHO SAVINGS GOAL ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal)

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>> // Dùng Flow để tự động cập nhật UI khi DB thay đổi

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: String): SavingsGoal?

    // --- CHO DEPOSIT LOG ---
    @Insert
    suspend fun insertLog(log: DepositLog)

    @Query("SELECT * FROM deposit_logs ORDER BY transactionDate DESC")
    fun getAllLogs(): Flow<List<DepositLog>>

    @Query("SELECT * FROM deposit_logs WHERE goalId = :goalId ORDER BY transactionDate DESC")
    fun getLogsByGoalId(goalId: String): Flow<List<DepositLog>>
    @Query("SELECT * FROM savings_goals")
    suspend fun getAllGoalsList(): List<SavingsGoal>

    // Insert danh sách Goals
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<SavingsGoal>)

    // Insert danh sách Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDeposits(deposits: List<DepositLog>)

    // Kiểm tra xem đã có dữ liệu chưa
    @Query("SELECT COUNT(*) FROM savings_goals")
    suspend fun getCount(): Int
}