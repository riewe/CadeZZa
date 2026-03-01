package by.riewe.cadence.ui.screens.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.riewe.cadence.data.entity.Route
import by.riewe.cadence.data.entity.RouteStatus
import by.riewe.cadence.ui.components.CountryInputField
import by.riewe.cadence.ui.components.DatePickerDialogField
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.theme.StatusActive
import by.riewe.cadence.ui.theme.StatusWarning
import by.riewe.cadence.ui.viewmodel.RouteViewModel
import by.riewe.cadence.utils.formatDate
import by.riewe.cadence.utils.formatNumberWithSpaces
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteRouteScreen(
    routeId: Long,
    viewModel: RouteViewModel,
    onNavigateBack: () -> Unit,
    onRouteCompleted: () -> Unit
) {
    val selectedRoute by viewModel.selectedRoute.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(routeId) {
        viewModel.loadRouteById(routeId)
    }

    // Если рейс не найден — уходим
    LaunchedEffect(selectedRoute) {
        if (selectedRoute == null) {
            onNavigateBack()
        }
    }

    CompleteRouteContent(
        route = selectedRoute,
        isLoading = isLoading,
        error = error,
        onNavigateBack = onNavigateBack,
        onCompleteRoute = { endDate, endOdometer, arrivalCountry, endEH, notes ->
            viewModel.completeRoute(
                routeId = routeId,
                endDate = endDate,
                endOdometer = endOdometer,
                arrivalCountry = arrivalCountry,
                endEH = endEH,
                notes = notes
            )
            onRouteCompleted()
        },
        onClearError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompleteRouteContent(
    route: Route?,
    isLoading: Boolean,
    error: String?,
    onNavigateBack: () -> Unit,
    onCompleteRoute: (Long, Int, String, Int, String?) -> Unit,
    onClearError: () -> Unit
) {
    if (route == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Проверяем что это черновик
    val isDraft = route.status == RouteStatus.DRAFT

    // Поля для выгрузки
    var arrivalCountry by remember { mutableStateOf(route.finishCountry ?: "") }
    var endDate by remember { mutableStateOf("") }
    var endOdometer by remember { mutableStateOf("") }
    var endEH by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(route.notes ?: "") }

    // UI состояние
    var showValidationErrors by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    var focusedItemIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(focusedItemIndex) {
        if (focusedItemIndex >= 0) {
            kotlinx.coroutines.delay(300)
            listState.animateScrollToItem(focusedItemIndex)
        }
    }

    error?.let {
        LaunchedEffect(it) {
            showErrorDialog = true
            errorDialogMessage = it
            onClearError()
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Ошибка") },
            text = { Text(errorDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isDraft) "Завершение рейса" else "Редактирование рейса"
                        )
                        Text(
                            "Рейс №${route.routeNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Карточка с данными загрузки
            item {
                LoadingInfoCard(route = route)
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Если рейс уже завершён — показываем текущие данные выгрузки
            if (!isDraft) {
                item {
                    CurrentUnloadingCard(route = route)
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // === ДАННЫЕ ВЫГРУЗКИ ===
            item {
                SectionHeader(
                    if (isDraft) "Данные выгрузки" else "Новые данные выгрузки"
                )
            }

            item {
                CountryInputField(
                    value = arrivalCountry,
                    onValueChange = { arrivalCountry = it },
                    label = "Страна выгрузки",
                    isError = showValidationErrors && arrivalCountry.length < 2,
                    errorMessage = "Формат: DE, PL, FR",
                    onFocus = { focusedItemIndex = 0 },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DatePickerDialogField(
                    date = endDate,
                    onDateSelected = { endDate = it },
                    label = "Дата выгрузки *",
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationErrors && endDate.isBlank()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormattedTextField(
                        value = endOdometer,
                        onValueChange = { endOdometer = it.filter { c -> c.isDigit() } },
                        label = "Одометр",
                        isError = showValidationErrors && endOdometer.isBlank(),
                        errorMessage = "Введите одометр",
                        displayFormatter = { "${formatNumberWithSpaces(it)} км" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 1 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    FormattedTextField(
                        value = endEH,
                        onValueChange = { endEH = it.filter { c -> c.isDigit() } },
                        label = "Моточасы",
                        isError = showValidationErrors && endEH.isBlank(),
                        errorMessage = "Введите моточасы",
                        displayFormatter = { "$it ч" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 2 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Предпросмотр
            if (endOdometer.isNotBlank()) {
                val endVal = endOdometer.toIntOrNull() ?: 0
                if (endVal > route.startOdometer) {
                    item {
                        MileagePreviewCard(
                            startOdometer = route.startOdometer,
                            endOdometer = endVal,
                            startEH = route.startEH,
                            endEH = endEH.toIntOrNull() ?: route.startEH
                        )
                    }
                }
            }

            // Примечания
            item {
                SectionHeader("Примечания")
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Примечания к рейсу") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 3 },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Кнопки
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val errors = mutableListOf<String>()

                            if (arrivalCountry.length < 2) errors.add("Страна выгрузки")
                            if (endDate.isBlank()) errors.add("Дата выгрузки")
                            if (endOdometer.isBlank()) errors.add("Одометр выгрузки")
                            if (endEH.isBlank()) errors.add("Моточасы выгрузки")

                            val endVal = endOdometer.toIntOrNull() ?: 0
                            val endEHVal = endEH.toIntOrNull() ?: 0

                            if (endVal <= route.startOdometer) {
                                errors.add("Одометр выгрузки должен быть больше ${formatNumberWithSpaces(route.startOdometer.toString())} км")
                            }
                            if (endEHVal <= route.startEH) {
                                errors.add("Моточасы выгрузки должны быть больше ${route.startEH} ч")
                            }

                            if (errors.isNotEmpty()) {
                                showValidationErrors = true
                                errorDialogMessage = "Исправьте ошибки:\n\n" +
                                        errors.joinToString("\n") { "• $it" }
                                showErrorDialog = true
                                return@Button
                            }

                            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            val dateMillis = dateFormat.parse(endDate)?.time
                                ?: System.currentTimeMillis()

                            onCompleteRoute(
                                dateMillis,
                                endVal,
                                arrivalCountry.uppercase(),
                                endEHVal,
                                notes.takeIf { it.isNotBlank() }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = if (isDraft) Icons.Default.CheckCircle else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isDraft) "Завершить рейс" else "Сохранить изменения")
                        }
                    }

                    TextButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Отмена")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ ====================

@Composable
private fun FormattedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorMessage: String,
    displayFormatter: (String) -> String,
    modifier: Modifier = Modifier,
    onFocus: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label *") },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            } else if (value.isNotEmpty()) {
                Text(displayFormatter(value))
            }
        },
        modifier = modifier
            .onFocusChanged { if (it.isFocused) onFocus() },
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun LoadingInfoCard(route: Route) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Данные загрузки",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Дата:", formatDate(route.startDate))
            InfoRow("Страна:", route.startCountry)
            InfoRow("Одометр:", "${formatNumberWithSpaces(route.startOdometer.toString())} км")
            InfoRow("Моточасы:", "${route.startEH} ч")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Груз:", route.cargoName)
            InfoRow("Вес:", "${formatNumberWithSpaces(route.cargoWeight.toString())} кг")
            InfoRow("CMR:", route.cmrNumber)
            InfoRow("Прицеп:", route.trailerNumber)
        }
    }
}

@Composable
private fun CurrentUnloadingCard(route: Route) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = StatusActive.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = StatusActive.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Текущие данные выгрузки",
                style = MaterialTheme.typography.titleSmall,
                color = StatusActive,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow("Дата:", route.endDate?.let { formatDate(it) } ?: "-")
            InfoRow("Страна:", route.finishCountry ?: "-")
            InfoRow("Одометр:", route.endOdometer?.let { "${formatNumberWithSpaces(it.toString())} км" } ?: "-")
            InfoRow("Моточасы:", route.endEH?.let { "$it ч" } ?: "-")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Пробег:", "${formatNumberWithSpaces(route.routeMileage.toString())} км")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MileagePreviewCard(
    startOdometer: Int,
    endOdometer: Int,
    startEH: Int,
    endEH: Int
) {
    val mileage = endOdometer - startOdometer
    val engineHours = endEH - startEH

    Card(
        colors = CardDefaults.cardColors(
            containerColor = StatusActive.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = StatusActive.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Итоги рейса",
                style = MaterialTheme.typography.titleSmall,
                color = StatusActive,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Пробег", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${formatNumberWithSpaces(mileage.toString())} км",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusActive,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Моточасы", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "$engineHours ч",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusActive,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
fun CompleteRouteScreenPreview() {
    val sampleRoute = Route(
        id = 1,
        periodId = 1,
        routeNumber = 5,
        startDate = System.currentTimeMillis() - 86400000,
        startOdometer = 150000,
        startCountry = "DE",
        cargoName = "Электроника",
        cargoWeight = 12000,
        cmrNumber = "CMR123456",
        cargoTemperature = "OFF",
        cargoMode = "Выключен",
        trailerNumber = "AB 1234",
        startEH = 2500,
        endDate = null,
        endOdometer = null,
        finishCountry = null,
        endEH = null,
        totalEH = null,
        routeMileage = 0,
        status = RouteStatus.DRAFT,
        notes = "Тестовый рейс"
    )

    CadenceTheme {
        CompleteRouteContent(
            route = sampleRoute,
            isLoading = false,
            error = null,
            onNavigateBack = {},
            onCompleteRoute = { _, _, _, _, _ -> },
            onClearError = {}
        )
    }
}