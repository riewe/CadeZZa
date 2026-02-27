// by/riewe/cadence/ui/screens/CadenzzaDetailScreen.kt
package by.riewe.cadence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.RvHookup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.riewe.cadence.data.entity.CadenzzaEntity
import by.riewe.cadence.data.entity.Period
import by.riewe.cadence.ui.components.CardText
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.viewmodel.CadenzzaViewModel
import by.riewe.cadence.ui.viewmodel.PeriodViewModel
import by.riewe.cadence.utils.formatDate
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadenzzaDetailScreen(
    cadenzzaId: Long,
    cadenzzaViewModel: CadenzzaViewModel,
    periodViewModel: PeriodViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRoutes: (Long) -> Unit,
    onNavigateToRefuelings: (Long) -> Unit,
    onNavigateToExpenses: (Long) -> Unit,
    onNavigateToCouplings: (Long) -> Unit
) {
    val cadenzzaWithPeriods by cadenzzaViewModel.selectedCadenzza.collectAsStateWithLifecycle()
    val periods by periodViewModel.periods.collectAsStateWithLifecycle()
    val currentPeriod by periodViewModel.currentPeriod.collectAsStateWithLifecycle()
    val isLoading by periodViewModel.isLoading.collectAsStateWithLifecycle()
    val error by periodViewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(cadenzzaId) {
        cadenzzaViewModel.loadCadenzzaDetails(cadenzzaId)
        periodViewModel.loadPeriods(cadenzzaId)
    }

    DisposableEffect(Unit) {
        onDispose {
            periodViewModel.clearSelectedPeriod()
            cadenzzaViewModel.clearSelectedCadenzza()
        }
    }

    val cadenzza = cadenzzaWithPeriods?.cadenzza
    val isCadenzzaClosed = cadenzza?.endDate != null

    CadenzzaDetailContent(
        cadenzza = cadenzza,
        periods = periods,
        currentPeriod = currentPeriod,
        isLoading = isLoading,
        error = error,
        isCadenzzaClosed = isCadenzzaClosed,
        onNavigateBack = onNavigateBack,
        onNavigateToRoutes = onNavigateToRoutes,
        onNavigateToRefuelings = onNavigateToRefuelings,
        onNavigateToExpenses = onNavigateToExpenses,
        onNavigateToCouplings = onNavigateToCouplings,
        onClosePeriod = {
            periodViewModel.closeCurrentPeriodAndCreateNew(
                cadenzzaId = cadenzzaId,
                endDate = System.currentTimeMillis()
            )
        },
        onClearError = { periodViewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CadenzzaDetailContent(
    cadenzza: CadenzzaEntity?,
    periods: List<Period>,
    currentPeriod: Period?,
    isLoading: Boolean,
    error: String?,
    isCadenzzaClosed: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToRoutes: (Long) -> Unit,
    onNavigateToRefuelings: (Long) -> Unit,
    onNavigateToExpenses: (Long) -> Unit,
    onNavigateToCouplings: (Long) -> Unit,
    onClosePeriod: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Каденция № ${cadenzza?.cadenzzaNumber ?: ""}")
                        if (currentPeriod != null) {
                            Text(
                                "Текущий период: ${currentPeriod.periodNumber}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
                modifier = Modifier.shadow(10.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Информация о каденции
                    item {
                        CadenzzaInfoCard(cadenzza = cadenzza)
                    }

                    // Текущий активный период (если каденция не закрыта)
                    if (!isCadenzzaClosed && currentPeriod != null) {
                        item {
                            CurrentPeriodCard(
                                period = currentPeriod,
                                onAddRoute = { onNavigateToRoutes(currentPeriod.id) },
                                onAddRefueling = { onNavigateToRefuelings(currentPeriod.id) },
                                onAddExpense = { onNavigateToExpenses(currentPeriod.id) },
                                onAddCoupling = { onNavigateToCouplings(currentPeriod.id) },
                                onClosePeriod = onClosePeriod
                            )
                        }
                    }

                    // История периодов
                    if (periods.isNotEmpty()) {
                        item {
                            Text(
                                "История периодов:",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(periods.sortedByDescending { it.periodNumber }, key = { it.id }) { period ->
                            PeriodHistoryCard(
                                period = period,
                                isCurrent = period.id == currentPeriod?.id,
                                onClick = {
                                    onNavigateToRoutes(period.id)
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            // Показываем ошибки
            if (error != null) {
                AlertDialog(
                    onDismissRequest = onClearError,
                    title = { Text("Ошибка") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = onClearError) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CadenzzaInfoCard(cadenzza: CadenzzaEntity?) {
    if (cadenzza == null) return

    val isClosed = cadenzza.endDate != null
    val daysInCadenzza = if (isClosed) {
        cadenzza.totalDays
    } else {
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - cadenzza.startDate).toInt()
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClosed)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Каденция № ${cadenzza.cadenzzaNumber}",
                    style = MaterialTheme.typography.titleLarge
                )
                Surface(
                    color = if (isClosed) Color.Gray else Color(0xFF4CAF50),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (isClosed) "Закрыта" else "Активна",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CardText("Тягач:", cadenzza.truckNumber)
            CardText("Водитель 1:", cadenzza.driver1)
            cadenzza.driver2?.let { CardText("Водитель 2:", it) }
            CardText("Начало:", "${formatDate(cadenzza.startDate)} ${formatTime(cadenzza.startTime)}")

            if (isClosed) {
                CardText("Окончание:", "${formatDate(cadenzza.endDate)} ${formatTime(cadenzza.endTime)}")
                CardText("Пробег:", "${formatNumberWithSpaces(cadenzza.totalMileage.toString())} км")
            }

            CardText("Дней в работе:", daysInCadenzza.toString())
        }
    }
}

@Composable
private fun CurrentPeriodCard(
    period: Period,
    onAddRoute: () -> Unit,
    onAddRefueling: () -> Unit,
    onAddExpense: () -> Unit,
    onAddCoupling: () -> Unit,
    onClosePeriod: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Текущий период ${period.periodNumber}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Начало: ${formatDate(period.startDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                AssistChip(
                    onClick = { },
                    label = { Text("Активен") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Быстрые действия
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    label = "Рейс",
                    icon = Icons.Default.Route,
                    onClick = onAddRoute
                )
                QuickActionButton(
                    label = "Заправка",
                    icon = Icons.Default.LocalGasStation,
                    onClick = onAddRefueling
                )
                QuickActionButton(
                    label = "Расход",
                    icon = Icons.Default.AttachMoney,
                    onClick = onAddExpense
                )
                QuickActionButton(
                    label = "Перецеп",
                    icon = Icons.Default.RvHookup,
                    onClick = onAddCoupling
                )
            }

            // Кнопка закрытия периода
            Button(
                onClick = onClosePeriod,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Закрыть период ${period.periodNumber} и создать новый")
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PeriodHistoryCard(
    period: Period,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val isClosed = period.endDate != null

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Период ${period.periodNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "С ${formatDate(period.startDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                period.endDate?.let {
                    Text(
                        "По ${formatDate(it)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Surface(
                color = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    isClosed -> Color.Gray
                    else -> Color(0xFF4CAF50)
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    when {
                        isCurrent -> "Текущий"
                        isClosed -> "Закрыт"
                        else -> "Активен"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

// Вспомогательные функции
private fun formatTime(minutes: Long?): String {
    if (minutes == null) return "-"
    val hours = (minutes / (60 * 60 * 1000)) % 24
    val mins = (minutes / (60 * 1000)) % 60
    return "${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}"
}

private fun formatNumberWithSpaces(number: String): String {
    val digitsOnly = number.filter { it.isDigit() }
    if (digitsOnly.length <= 3) return digitsOnly
    val reversed = digitsOnly.reversed()
    val spaced = reversed.chunked(3).joinToString(" ")
    return spaced.reversed()
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, name = "Active Cadenzza with Current Period")
@Composable
fun CadenzzaDetailScreenActivePreview() {
    CadenceTheme {
        val mockCadenzza = CadenzzaEntity(
            id = 1,
            cadenzzaNumber = "42",
            startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5),
            startTime = 8 * 60 * 60 * 1000L, // 08:00
            driver1 = "ИВАНОВ И.И.",
            driver2 = "ПЕТРОВ П.П.",
            endDate = null,
            endTime = null,
            truckNumber = "ABC 123",
            startTrailerNumber = "XY 1234",
            endTrailerNumber = null,
            startOdometer = 100000,
            endOdometer = null,
            startTruckFuel = 500,
            endTruckFuel = null,
            startTrailerFuel = 200,
            endTrailerFuel = null,
            startMH = 1000,
            endMH = null,
            totalMileage = 0,
            totalDays = 0
        )

        val mockPeriods = listOf(
            Period(
                id = 1,
                cadenzzaId = 1,
                periodNumber = 1,
                startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5),
                endDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                notes = "Первый рейс"
            ),
            Period(
                id = 2,
                cadenzzaId = 1,
                periodNumber = 2,
                startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                endDate = null,
                notes = null
            )
        )

        CadenzzaDetailContent(
            cadenzza = mockCadenzza,
            periods = mockPeriods,
            currentPeriod = mockPeriods[1],
            isLoading = false,
            error = null,
            isCadenzzaClosed = false,
            onNavigateBack = {},
            onNavigateToRoutes = {},
            onNavigateToRefuelings = {},
            onNavigateToExpenses = {},
            onNavigateToCouplings = {},
            onClosePeriod = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true, name = "Closed Cadenzza")
@Composable
fun CadenzzaDetailScreenClosedPreview() {
    CadenceTheme {
        val mockCadenzza = CadenzzaEntity(
            id = 1,
            cadenzzaNumber = "15",
            startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30),
            startTime = 6 * 60 * 60 * 1000L,
            driver1 = "СИДОРОВ С.С.",
            driver2 = null,
            endDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
            endTime = 18 * 60 * 60 * 1000L,
            truckNumber = "XYZ 789",
            startTrailerNumber = "AB 5678",
            endTrailerNumber = "CD 9999",
            startOdometer = 50000,
            endOdometer = 65000,
            startTruckFuel = 400,
            endTruckFuel = 350,
            startTrailerFuel = 150,
            endTrailerFuel = 100,
            startMH = 5000,
            endMH = 5300,
            totalMileage = 15000,
            totalDays = 28
        )

        val mockPeriods = listOf(
            Period(
                id = 1,
                cadenzzaId = 1,
                periodNumber = 1,
                startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30),
                endDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(20),
                notes = "Европа"
            ),
            Period(
                id = 2,
                cadenzzaId = 1,
                periodNumber = 2,
                startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(20),
                endDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10),
                notes = "СНГ"
            ),
            Period(
                id = 3,
                cadenzzaId = 1,
                periodNumber = 3,
                startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10),
                endDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                notes = "Внутренние рейсы"
            )
        )

        CadenzzaDetailContent(
            cadenzza = mockCadenzza,
            periods = mockPeriods,
            currentPeriod = null,
            isLoading = false,
            error = null,
            isCadenzzaClosed = true,
            onNavigateBack = {},
            onNavigateToRoutes = {},
            onNavigateToRefuelings = {},
            onNavigateToExpenses = {},
            onNavigateToCouplings = {},
            onClosePeriod = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun CadenzzaDetailScreenLoadingPreview() {
    CadenceTheme {
        CadenzzaDetailContent(
            cadenzza = null,
            periods = emptyList(),
            currentPeriod = null,
            isLoading = true,
            error = null,
            isCadenzzaClosed = false,
            onNavigateBack = {},
            onNavigateToRoutes = {},
            onNavigateToRefuelings = {},
            onNavigateToExpenses = {},
            onNavigateToCouplings = {},
            onClosePeriod = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun CadenzzaDetailScreenErrorPreview() {
    CadenceTheme {
        val mockCadenzza = CadenzzaEntity(
            id = 1,
            cadenzzaNumber = "7",
            startDate = System.currentTimeMillis(),
            startTime = 0L,
            driver1 = "ТЕСТ",
            driver2 = null,
            endDate = null,
            endTime = null,
            truckNumber = "TEST 01",
            startTrailerNumber = "TT 0001",
            endTrailerNumber = null,
            startOdometer = 0,
            endOdometer = null,
            startTruckFuel = 0,
            endTruckFuel = null,
            startTrailerFuel = 0,
            endTrailerFuel = null,
            startMH = 0,
            endMH = null,
            totalMileage = 0,
            totalDays = 0
        )

        CadenzzaDetailContent(
            cadenzza = mockCadenzza,
            periods = emptyList(),
            currentPeriod = null,
            isLoading = false,
            error = "Ошибка загрузки периодов: Нет подключения к базе данных",
            isCadenzzaClosed = false,
            onNavigateBack = {},
            onNavigateToRoutes = {},
            onNavigateToRefuelings = {},
            onNavigateToExpenses = {},
            onNavigateToCouplings = {},
            onClosePeriod = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true, name = "Single Period (First)")
@Composable
fun CadenzzaDetailScreenSinglePeriodPreview() {
    CadenceTheme {
        val mockCadenzza = CadenzzaEntity(
            id = 1,
            cadenzzaNumber = "1",
            startDate = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(8),
            startTime = 0L,
            driver1 = "НОВИКОВ Н.Н.",
            driver2 = null,
            endDate = null,
            endTime = null,
            truckNumber = "NEW 001",
            startTrailerNumber = "NW 0001",
            endTrailerNumber = null,
            startOdometer = 1000,
            endOdometer = null,
            startTruckFuel = 600,
            endTruckFuel = null,
            startTrailerFuel = 300,
            endTrailerFuel = null,
            startMH = 100,
            endMH = null,
            totalMileage = 0,
            totalDays = 0
        )

        val mockPeriods = listOf(
            Period(
                id = 1,
                cadenzzaId = 1,
                periodNumber = 1,
                startDate = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(8),
                endDate = null,
                notes = null
            )
        )

        CadenzzaDetailContent(
            cadenzza = mockCadenzza,
            periods = mockPeriods,
            currentPeriod = mockPeriods[0],
            isLoading = false,
            error = null,
            isCadenzzaClosed = false,
            onNavigateBack = {},
            onNavigateToRoutes = {},
            onNavigateToRefuelings = {},
            onNavigateToExpenses = {},
            onNavigateToCouplings = {},
            onClosePeriod = {},
            onClearError = {}
        )
    }
}
