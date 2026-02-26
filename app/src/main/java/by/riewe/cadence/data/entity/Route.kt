package by.riewe.cadence.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routes",
    foreignKeys = [
        ForeignKey(
            entity = Period::class,
            parentColumns = ["id"],
            childColumns = ["periodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["periodId"])]
)

data class Route(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val periodId: Long,

    val routeNumber: Int,
    val startDate: Long,
    val endDate: Long?,
    val startOdometer: Int,
    val endOdometer: Int?,

    val startCountry: String,
    val finishCountry: String?,

    val cargoName: String,
    val cargoWeight: Int,

    val cmrNumber: String,
    val cargoTemperature: String,
    val cargoMode: String,

    val trailerNumber: String,
    val startEH: Int,
    val endEH: Int?,
    val totalEH: Int?,

    val routeMileage: Int
)