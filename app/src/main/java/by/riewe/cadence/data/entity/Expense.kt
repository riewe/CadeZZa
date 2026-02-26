package by.riewe.cadence.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
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
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val periodId: Long,
    val expenseNumber: Int,
    val date: Long,
    val description: String,
    val amount: Double,
    val currency: String,
    val country: String,
    val cardName: String
)
