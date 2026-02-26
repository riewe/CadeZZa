package by.riewe.cadence.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import by.riewe.cadence.data.entity.Route
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Insert
    suspend fun insertRoute(route: Route): Long

    @Update
    suspend fun updateRoute(route: Route)

    @Delete
    suspend fun deleteRoute(route: Route)

    @Query("SELECT * FROM routes WHERE periodId = :periodId ORDER BY startDate DESC")
    fun getByPeriodId(periodId: Long ): Flow<List<Route>>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getById(id: Long ): Route?

    @Query("SELECT SUM(routeMileage) FROM routes WHERE periodId = :periodId")
    suspend fun getTotalMileageForPeriod(periodId: Long ): Int?
}