package by.riewe.cadence.data.db

import androidx.room.Embedded
import androidx.room.Relation
import by.riewe.cadence.data.entity.CadenzzaEntity
import by.riewe.cadence.data.entity.Expense
import by.riewe.cadence.data.entity.Period
import by.riewe.cadence.data.entity.Refueling
import by.riewe.cadence.data.entity.Route
import by.riewe.cadence.data.entity.TrailerCouplings

data class CadenzzaWithPeriods(
    @Embedded
    val cadenzza: CadenzzaEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "cadenzzaId",
        entity = Period::class  // ← ДОБАВЬ: укажи Entity класс
    )
    val periods: List<PeriodWithData>
)

data class PeriodWithData(
    @Embedded
    val period: Period,

    @Relation(
        parentColumn = "id",
        entityColumn = "periodId",
        entity = Route::class  // ← ДОБАВЬ
    )
    val routes: List<Route>,

    @Relation(
        parentColumn = "id",
        entityColumn = "periodId",
        entity = Refueling::class  // ← ДОБАВЬ
    )
    val refuelings: List<Refueling>,

    @Relation(
        parentColumn = "id",
        entityColumn = "periodId",
        entity = Expense::class  // ← ДОБАВЬ
    )
    val expenses: List<Expense>,

    @Relation(
        parentColumn = "id",
        entityColumn = "periodId",
        entity = TrailerCouplings::class  // ← ДОБАВЬ
    )
    val trailerCouplings: List<TrailerCouplings>
)