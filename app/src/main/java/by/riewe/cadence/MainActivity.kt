// by/riewe/cadence/MainActivity.kt (альтернативный вариант)
package by.riewe.cadence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import by.riewe.cadence.navigation.CadenzzaNavHost
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.viewmodel.CadenzzaViewModel
import by.riewe.cadence.ui.viewmodel.PeriodViewModel
import by.riewe.cadence.ui.viewmodel.RouteViewModel
import by.riewe.cadence.ui.viewmodel.RefuelingViewModel
import by.riewe.cadence.ui.viewmodel.ExpenseViewModel
import by.riewe.cadence.ui.viewmodel.TrailerCouplingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включаем edge-to-edge
        enableEdgeToEdge()

        // Настраиваем WindowInsets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val factory = (application as CadenzzaApplication).viewModelFactory

        setContent {
            CadenceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val cadenzzaViewModel: CadenzzaViewModel = viewModel(factory = factory)
                    val periodViewModel: PeriodViewModel = viewModel(factory = factory)
                    val routeViewModel: RouteViewModel = viewModel(factory = factory)
                    val refuelingViewModel: RefuelingViewModel = viewModel(factory = factory)
                    val expenseViewModel: ExpenseViewModel = viewModel(factory = factory)
                    val trailerCouplingsViewModel: TrailerCouplingsViewModel = viewModel(factory = factory)

                    CadenzzaNavHost(
                        cadenzzaViewModel = cadenzzaViewModel,
                        periodViewModel = periodViewModel,
                        routeViewModel = routeViewModel,
                        refuelingViewModel = refuelingViewModel,
                        expenseViewModel = expenseViewModel,
                        trailerCouplingsViewModel = trailerCouplingsViewModel
                    )
                }
            }
        }
    }
}