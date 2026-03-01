// by/riewe/cadence/ui/screens/CadenzzaListScreen.kt
package by.riewe.cadence.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.riewe.cadence.data.entity.CadenzzaEntity
import by.riewe.cadence.ui.components.CardText
import by.riewe.cadence.ui.components.ExpandableCard
import by.riewe.cadence.ui.components.menu.CadenzzaAction
import by.riewe.cadence.ui.components.menu.CadenzzaContextMenu
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.viewmodel.CadenzzaViewModel
import by.riewe.cadence.utils.formatDate
import by.riewe.cadence.utils.formatNumberWithSpaces
import by.riewe.cadence.utils.formatTime
import by.riewe.cadence.ui.theme.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadenzzaListScreen(
    viewModel: CadenzzaViewModel,
    onNavigateToCreate: (String?) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToClose: (Long) -> Unit,
) {
    val cadenzzaList by viewModel.allCadenzza.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val suggestedNumber by viewModel.suggestedCadenzzaNumber.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadNextCadenzzaNumber()
    }

    CadenzzaListContent(
        cadenzzaList = cadenzzaList,
        isLoading = isLoading,
        suggestedNumber = suggestedNumber,
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToClose = onNavigateToClose,
        onDeleteCadenzza = viewModel::deleteCadenzza,
        onAddPeriod = { viewModel.addPeriod(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CadenzzaListContent(
    cadenzzaList: List<CadenzzaEntity>,
    isLoading: Boolean,
    suggestedNumber: String?,
    onNavigateToCreate: (String?) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToClose: (Long) -> Unit,
    onDeleteCadenzza: (Long) -> Unit,
    onAddPeriod: (Long) -> Unit,
) {
    var contextMenuVisible by remember { mutableStateOf(false) }
    var selectedCadenzza by remember { mutableStateOf<CadenzzaEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Каденции") },
                colors = TopAppBarDefaults.topAppBarColors(),
                // Убираем windowInsets — используем дефолтные
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreate(suggestedNumber) }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        // ИСПРАВЛЕНИЕ: используем innerPadding только здесь, без дублирования
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // ← только этот padding!
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (cadenzzaList.isEmpty()) {
                EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    onCreateClick = { onNavigateToCreate(suggestedNumber) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                    // УБРАНО: contentPadding = innerPadding — это было дублирование!
                ) {
                    items(cadenzzaList, key = { it.id }) { cadenzza ->
                        CadenzzaCard(
                            cadenzza = cadenzza,
                            onClick = { onNavigateToDetail(cadenzza.id) },
                            onLongPress = {
                                selectedCadenzza = cadenzza
                                contextMenuVisible = true
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            // Контекстное меню
            selectedCadenzza?.let { cadenzza ->
                CadenzzaContextMenu(
                    expanded = contextMenuVisible,
                    onDismiss = {
                        contextMenuVisible = false
                        selectedCadenzza = null
                    },
                    onAction = { action ->
                        when (action) {
                            is CadenzzaAction.StartNewPeriod -> onAddPeriod(cadenzza.id)
                            is CadenzzaAction.CloseCadenzza -> onNavigateToClose(cadenzza.id)
                            is CadenzzaAction.EditCadenzza -> { }
                            is CadenzzaAction.DeleteCadenzza -> onDeleteCadenzza(cadenzza.id)
                        }
                        contextMenuVisible = false
                        selectedCadenzza = null
                    },
                    isCadenzzaClosed = cadenzza.endDate != null
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Нет каденций", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Нажмите + чтобы создать", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CadenzzaCard(
    cadenzza: CadenzzaEntity,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val isClosed = cadenzza.endDate != null

    ExpandableCard(
        collapsedContent = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val (backgroundColor, textColor) = if (isClosed) {
                        StatusClosed to Color.White          // Серый для закрытой
                    } else {
                        StatusActive to Color.White          // Зелёный для активной
                    }
                    Surface(
                        color = backgroundColor,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Каденция № ${cadenzza.cadenzzaNumber} (${cadenzza.truckNumber})",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                CardText(
                    label = "Начало:",
                    value = "${formatDate(cadenzza.startDate)} ${formatTime(cadenzza.startTime)}"
                )

                val endDateStr = if (isClosed) {
                    "${formatDate(cadenzza.endDate)} ${formatTime(cadenzza.endTime)}"
                } else "-"
                CardText(label = "Окончание:", value = endDateStr)

                val daysText = if (isClosed) {
                    "${cadenzza.totalDays}"
                } else {
                    "${TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - cadenzza.startDate)}"
                }
                CardText(label = "Дней в каденции:", value = daysText)
            }
        },
        expandedContent = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                CardText("Водитель 1:", cadenzza.driver1)
                cadenzza.driver2?.let { CardText("Водитель 2:", it) }
                CardText("Прицеп:", cadenzza.startTrailerNumber)
                CardText(
                    "Одометр начало:",
                    "${formatNumberWithSpaces(cadenzza.startOdometer.toString())} км"
                )
                if (isClosed) {
                    CardText(
                        "Одометр конец:",
                        "${formatNumberWithSpaces(cadenzza.endOdometer.toString())} км"
                    )
                    CardText(
                        "Пробег:",
                        "${formatNumberWithSpaces(cadenzza.totalMileage.toString())} км"
                    )
                }
            }
        },
        onAction = onClick,
        onLongPress = onLongPress,
        actionLabel = if (isClosed) "Просмотр" else "Продолжить"
    )
}

@Preview(showBackground = true)
@Composable
fun CadenzzaListScreenPreview() {
    val sampleCadenzzas = listOf(
        CadenzzaEntity(
            id = 1,
            cadenzzaNumber = "123",
            truckNumber = "AB 123",
            startDate = System.currentTimeMillis(),
            startTime = System.currentTimeMillis(),
            driver1 = "John Doe",
            driver2 = null,
            endDate = null,
            endTime = null,
            startOdometer = 100000,
            endOdometer = null,
            startTrailerNumber = "TR1",
            endTrailerNumber = null,
            startTruckFuel = 500,
            endTruckFuel = null,
            startTrailerFuel = 0,
            endTrailerFuel = null,
            startMH = 100,
            endMH = null,
            totalMileage = 0,
            totalDays = 0
        ),
        CadenzzaEntity(
            id = 2,
            cadenzzaNumber = "124",
            truckNumber = "CD 567",
            startDate = System.currentTimeMillis() - 86400000 * 10,
            startTime = System.currentTimeMillis(),
            endDate = System.currentTimeMillis(),
            endTime = System.currentTimeMillis(),
            driver1 = "Jane Smith",
            driver2 = "Peter Jones",
            startOdometer = 200000,
            endOdometer = 210000,
            totalMileage = 10000,
            totalDays = 10,
            startTrailerNumber = "TR1",
            endTrailerNumber = "TR2",
            startTruckFuel = 600,
            endTruckFuel = 100,
            startTrailerFuel = 0,
            endTrailerFuel = 0,
            startMH = 200,
            endMH = 220
        ),
    )
    CadenceTheme {
        CadenzzaListContent(
            cadenzzaList = sampleCadenzzas,
            isLoading = false,
            suggestedNumber = "125",
            onNavigateToCreate = {},
            onNavigateToDetail = {},
            onNavigateToClose = {},
            onDeleteCadenzza = {},
            onAddPeriod = {}
        )
    }
}