package by.riewe.cadence.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object CadenzzaList : Route()

    @Serializable
    data class CreateCadenzza(val suggestedNumber: String? = null) : Route()

    @Serializable
    data class CloseCadenzza(val cadenzzaId: Long) : Route()

    @Serializable
    data class CadenzzaDetail(val cadenzzaId: Long) : Route()

    // Экраны списков для периода
    @Serializable
    data class RouteList(val periodId: Long) : Route()

    @Serializable
    data class RefuelingList(val periodId: Long) : Route()

    @Serializable
    data class ExpenseList(val periodId: Long) : Route()

    @Serializable
    data class CouplingList(val periodId: Long) : Route()
}