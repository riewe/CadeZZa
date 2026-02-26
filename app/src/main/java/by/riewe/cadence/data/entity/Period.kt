package by.riewe.cadence.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "periods",
    foreignKeys = [
        ForeignKey(
            entity = CadenzzaEntity::class,
            parentColumns = ["id"],
            childColumns = ["cadenzzaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cadenzzaId"), Index(value = ["cadenzzaId", "periodNumber"], unique = true)]
)
data class Period(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cadenzzaId: Long,
    val periodNumber: Int, // Индекс периода: 1, 2, 3...
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null, // null = текущий активный период
    val notes: String? = null
)