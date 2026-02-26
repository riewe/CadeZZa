package by.riewe.cadence.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "refuelings",
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
data class Refueling(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val periodId: Long,
    val refuelingNumber: Int,
    val date: Long,
    val truckFuel: Int?,
    val trailerFuel: Int?,
    val adBlue: Int?,
    val country: String?,
    val cardName: String
)
