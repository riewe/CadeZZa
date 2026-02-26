package by.riewe.cadence.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import by.riewe.cadence.data.db.PeriodWithData
import by.riewe.cadence.data.entity.Period
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDao {

    @Insert
    suspend fun insertPeriod(period: Period): Long

    @Update
    suspend fun updatePeriod(period: Period)

    @Delete
    suspend fun deletePeriod(period: Period)

    @Query("SELECT * FROM periods WHERE cadenzzaId = :cadenzzaId ORDER BY periodNumber")
    fun getByCadenzzaId(cadenzzaId: Long): Flow<List<Period>>

    @Query("SELECT * FROM periods WHERE id = :id")
    suspend fun getById(id: Long): Period?

    @Query("SELECT * FROM periods WHERE cadenzzaId = :cadenzzaId AND endDate IS NULL LIMIT 1")
    suspend fun getCurrentPeriod(cadenzzaId: Long): Period?

    @Query("SELECT * FROM periods WHERE cadenzzaId = :cadenzzaId AND periodNumber = :periodNumber LIMIT 1")
    suspend fun getPeriodByNumber(cadenzzaId: Long, periodNumber: Int): Period?

    @Query("SELECT MAX(periodNumber) FROM periods WHERE cadenzzaId = :cadenzzaId")
    suspend fun getMaxPeriodNumber(cadenzzaId: Long): Int?

    @Query("SELECT COUNT(*) FROM periods WHERE cadenzzaId = :cadenzzaId")
    suspend fun getPeriodCount(cadenzzaId: Long): Int

    @Transaction
    @Query("SELECT * FROM periods WHERE id = :id")
    suspend fun getPeriodWithData(id: Long): PeriodWithData?

    @Query("SELECT * FROM periods WHERE cadenzzaId = :cadenzzaId AND periodNumber = :periodNumber")
    suspend fun getPeriodWithDataByNumber(cadenzzaId: Long, periodNumber: Int): PeriodWithData?
}