package by.riewe.cadence.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.riewe.cadence.data.entity.Refueling
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelingDao {

    @Insert
    suspend fun insert(refueling: Refueling): Long

    @Update
    suspend fun update(refueling: Refueling)

    @Delete
    suspend fun delete(refueling: Refueling)

    @Query("SELECT * FROM refuelings WHERE periodId = :periodId ORDER BY date DESC")
    fun getByPeriodId(periodId: Long ): Flow<List<Refueling>>

    @Query("SELECT * FROM refuelings WHERE id = :id")
    suspend fun getById(id: Long ): Refueling?

    // ✅ Исправлено - COALESCE заменяет null на 0
    @Query("SELECT SUM(COALESCE(truckFuel, 0) + COALESCE(trailerFuel, 0)) FROM refuelings WHERE periodId = :periodId")
    suspend fun getTotalFuelForPeriod(periodId: Long ): Int?
}