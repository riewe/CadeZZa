package by.riewe.cadence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.riewe.cadence.data.dao.*

class ViewModelFactory(
    private val cadenzzaDao: CadenzzaDao,
    private val periodDao: PeriodDao,
    private val routeDao: RouteDao,
    private val refuelingDao: RefuelingDao,
    private val expenseDao: ExpenseDao,
    private val trailerCouplingsDao: TrailerCouplingsDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CadenzzaViewModel::class.java) -> {
                CadenzzaViewModel(cadenzzaDao, periodDao) as T
            }
            modelClass.isAssignableFrom(PeriodViewModel::class.java) -> {
                PeriodViewModel(periodDao) as T
            }
            modelClass.isAssignableFrom(RouteViewModel::class.java) -> {
                RouteViewModel(routeDao, periodDao) as T
            }
            modelClass.isAssignableFrom(RefuelingViewModel::class.java) -> {
                RefuelingViewModel(refuelingDao, periodDao) as T
            }
            modelClass.isAssignableFrom(ExpenseViewModel::class.java) -> {
                ExpenseViewModel(expenseDao, periodDao) as T
            }
            modelClass.isAssignableFrom(TrailerCouplingsViewModel::class.java) -> {
                TrailerCouplingsViewModel(trailerCouplingsDao, periodDao) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}