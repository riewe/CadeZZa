package by.riewe.cadence.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.riewe.cadence.data.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE periodId = :periodId ORDER BY date DESC")
    fun getByPeriodId(periodId: Long ): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long ): Expense?

    // ✅ Исправлен запрос - добавлено условие cardName
    @Query("SELECT SUM(amount) FROM expenses WHERE periodId = :periodId AND cardName = :cardName")
    suspend fun getTotalByCard(periodId: Long , cardName: String): Double?

    // Или если нужна сумма по всем картам:
    @Query("SELECT SUM(amount) FROM expenses WHERE periodId = :periodId")
    suspend fun getTotalForPeriod(periodId: Long ): Double?
}