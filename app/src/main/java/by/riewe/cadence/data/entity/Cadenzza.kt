package by.riewe.cadence.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cadenzza")
data class CadenzzaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val cadenzzaNumber: String,

    val startDate: Long,
    val startTime: Long,

    val driver1: String,
    val driver2: String?,

    val endDate: Long?,
    val endTime: Long?,

    val truckNumber: String,
    val startTrailerNumber: String,
    val endTrailerNumber: String?,

    val startOdometer: Int,
    val endOdometer: Int?,

    val startTruckFuel: Int,
    val endTruckFuel: Int?,

    val startTrailerFuel: Int,
    val endTrailerFuel: Int?,

    val startMH: Int,
    val endMH: Int?,

    val totalMileage: Int,
    val totalDays: Int
)