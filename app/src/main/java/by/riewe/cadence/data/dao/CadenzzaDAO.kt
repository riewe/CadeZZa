package by.riewe.cadence.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import by.riewe.cadence.data.db.CadenzzaWithPeriods
import by.riewe.cadence.data.entity.CadenzzaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CadenzzaDao {

    @Insert
    suspend fun insertCadenzza(cadenzza: CadenzzaEntity): Long  // ✅ возвращает id

    @Update
    suspend fun updateCadenzza(cadenzza: CadenzzaEntity)

    @Query("DELETE FROM cadenzza WHERE id = :id")
    suspend fun deleteCadenzza(id: Long )

    @Query("SELECT * FROM cadenzza ORDER BY startDate DESC")
    fun getAll(): Flow<List<CadenzzaEntity>>

    @Query("SELECT * FROM cadenzza WHERE id = :id")
    suspend fun getById(id: Long ): CadenzzaEntity? // ✅ не Flow, а объект или null

    @Transaction
    @Query("SELECT * FROM cadenzza WHERE id = :id")
    suspend fun getCadenzzaWithPeriods(id: Long ): CadenzzaWithPeriods?

    @Query("""
        UPDATE cadenzza
        SET endDate = :endDate,
            endTime = :endTime,
            endOdometer = :endOdometer,
            endTrailerNumber = :endTrailerNumber,
            endTruckFuel = :endTruckFuel,
            endTrailerFuel = :endTrailerFuel,
            endMH = :endMH,
            totalMileage = :totalMileage,
            totalDays = :totalDays
        WHERE id = :cadenzzaId            
    """)
    suspend fun closeCadenzza(
        cadenzzaId: Long ,
        endDate: Long,
        endTime: Long,
        endOdometer: Int,
        endTrailerNumber: String,
        endTruckFuel: Int,
        endTrailerFuel: Int,
        endMH: Int,
        totalMileage: Int,
        totalDays: Int
    )
}