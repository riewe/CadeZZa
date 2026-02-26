// by/riewe/cadence/navigation/CadenzzaNavHost.kt
package by.riewe.cadence.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import by.riewe.cadence.ui.screens.CadenzzaListScreen
import by.riewe.cadence.ui.screens.CreateCadenzzaScreen
import by.riewe.cadence.ui.screens.CloseCadenzzaScreen
import by.riewe.cadence.ui.screens.CadenzzaDetailScreen
import by.riewe.cadence.ui.viewmodel.CadenzzaViewModel
import by.riewe.cadence.ui.viewmodel.PeriodViewModel
import by.riewe.cadence.ui.viewmodel.RouteViewModel
import by.riewe.cadence.ui.viewmodel.RefuelingViewModel
import by.riewe.cadence.ui.viewmodel.ExpenseViewModel
import by.riewe.cadence.ui.viewmodel.TrailerCouplingsViewModel

@Composable
fun CadenzzaNavHost(
    navController: NavHostController = rememberNavController(),
    cadenzzaViewModel: CadenzzaViewModel,
    periodViewModel: PeriodViewModel,
    routeViewModel: RouteViewModel,
    refuelingViewModel: RefuelingViewModel,
    expenseViewModel: ExpenseViewModel,
    trailerCouplingsViewModel: TrailerCouplingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Route.CadenzzaList
    ) {
        composable<Route.CadenzzaList> {
            CadenzzaListScreen(
                viewModel = cadenzzaViewModel,
                onNavigateToCreate = { suggestedNumber ->
                    navController.navigate(Route.CreateCadenzza(suggestedNumber))
                },
                onNavigateToDetail = { cadenzzaId ->
                    navController.navigate(Route.CadenzzaDetail(cadenzzaId))
                },
                onNavigateToClose = { cadenzzaId ->
                    navController.navigate(Route.CloseCadenzza(cadenzzaId))
                }
            )
        }

        composable<Route.CreateCadenzza> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.CreateCadenzza>()
            CreateCadenzzaScreen(
                viewModel = cadenzzaViewModel,
                suggestedNumber = args.suggestedNumber,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.CloseCadenzza> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.CloseCadenzza>()
            CloseCadenzzaScreen(
                viewModel = cadenzzaViewModel,
                cadenzzaId = args.cadenzzaId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.CadenzzaDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.CadenzzaDetail>()
            CadenzzaDetailScreen(
                cadenzzaId = args.cadenzzaId,
                cadenzzaViewModel = cadenzzaViewModel,
                periodViewModel = periodViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRoutes = { },
                onNavigateToRefuelings = { },
                onNavigateToExpenses = { },
                onNavigateToCouplings = { }
            )
        }
    }
}