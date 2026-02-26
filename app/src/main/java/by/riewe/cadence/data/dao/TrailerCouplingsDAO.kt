package by.riewe.cadence.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.riewe.cadence.data.entity.TrailerCouplings
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailerCouplingsDao {
    
    @Insert
    suspend fun insert(trailerCoupling: TrailerCouplings): Long
    
    @Update
    suspend fun update(trailerCoupling: TrailerCouplings)
    
    @Delete
    suspend fun delete(trailerCoupling: TrailerCouplings)
    
    @Query("SELECT * FROM couplings WHERE periodId = :periodId ORDER BY startDate DESC")
    fun getByPeriodId(periodId: Long ): Flow<List<TrailerCouplings>>
    
    @Query("SELECT * FROM couplings WHERE id = :id")
    suspend fun getById(id: Long ): TrailerCouplings?
}
