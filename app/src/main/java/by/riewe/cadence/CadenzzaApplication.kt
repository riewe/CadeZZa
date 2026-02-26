package by.riewe.cadence

import android.app.Application
import by.riewe.cadence.data.db.CadenzzaDatabase
import by.riewe.cadence.ui.viewmodel.ViewModelFactory

class CadenzzaApplication : Application() {

    val database by lazy { CadenzzaDatabase.getDatabase(this) }

    val viewModelFactory by lazy {
        ViewModelFactory(
            cadenzzaDao = database.cadenzzaDao(),
            periodDao = database.periodDao(),
            routeDao = database.routeDao(),
            refuelingDao = database.refuelingDao(),
            expenseDao = database.expenseDao(),              // ← добавлено
            trailerCouplingsDao = database.trailerCouplingsDao() // ← добавлено
        )
    }
}