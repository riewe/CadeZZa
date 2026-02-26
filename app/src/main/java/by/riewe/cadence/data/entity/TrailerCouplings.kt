package by.riewe.cadence.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "couplings",
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
data class TrailerCouplings(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val periodId: Long,

    val number: Int,
    val fromTruck: String,
    val trailerNumber: String,
    val startDate: Long,
    val endDate: Long?,
    val startEH: Int,
    val endEH: Int?,
    val totalEH: Int?,
    val startFuel: Int,
    val endFuel: Int?,
    val startCountry: String,
    val endCountry: String?
    )
