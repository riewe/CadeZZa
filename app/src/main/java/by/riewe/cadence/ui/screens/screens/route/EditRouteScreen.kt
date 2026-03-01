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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import by.riewe.cadence.data.entity.RefrigeratorMode
import by.riewe.cadence.ui.components.CountryInputField
import by.riewe.cadence.ui.components.DatePickerDialogField
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.theme.StatusActive
import by.riewe.cadence.ui.theme.StatusError
import by.riewe.cadence.ui.theme.StatusWarning
import by.riewe.cadence.ui.viewmodel.RouteViewModel
import by.riewe.cadence.utils.formatDate
import by.riewe.cadence.utils.formatNumberWithSpaces
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRouteScreen(
    routeId: Long,
    viewModel: RouteViewModel,
    onNavigateBack: () -> Unit,
    onRouteUpdated: () -> Unit,
    onRouteDeleted: () -> Unit
) {
    val selectedRoute by viewModel.selectedRoute.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(routeId) {
        viewModel.loadRouteById(routeId)
    }

    EditRouteContent(
        route = selectedRoute,
        isLoading = isLoading,
        error = error,
        onNavigateBack = onNavigateBack,
        onUpdateRoute = { updatedRoute ->
            viewModel.updateRoute(updatedRoute)
            onRouteUpdated()
        },
        onDeleteRoute = {
            viewModel.deleteRoute(routeId)
            onRouteDeleted()
        },
        onRevertToDraft = {
            viewModel.revertToDraft(routeId)
        },
        onClearError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRouteContent(
    route: Route?,
    isLoading: Boolean,
    error: String?,
    onNavigateBack: () -> Unit,
    onUpdateRoute: (Route) -> Unit,
    onDeleteRoute: () -> Unit,
    onRevertToDraft: () -> Unit,
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

    // === ВСЕ ПОЛЯ РЕЙСА ===
    var routeNumber by remember { mutableStateOf(route.routeNumber.toString()) }
    var departureCountry by remember { mutableStateOf(route.startCountry) }
    var arrivalCountry by remember { mutableStateOf(route.finishCountry ?: "") }
    var cargoName by remember { mutableStateOf(route.cargoName) }
    var cargoWeight by remember { mutableStateOf(route.cargoWeight.toString()) }
    var cmrNumber by remember { mutableStateOf(route.cmrNumber) }
    var trailerNumber by remember { mutableStateOf(route.trailerNumber) }

    // Режим холодильника
    var selectedRefrigeratorMode by remember {
        mutableStateOf(
            when (route.cargoMode) {
                "Авто" -> RefrigeratorMode.AUTO
                "Непрерывный" -> RefrigeratorMode.CONTINUOUS
                else -> RefrigeratorMode.OFF
            }
        )
    }
    var expandedRefrigeratorMenu by remember { mutableStateOf(false) }

    // Даты
    var startDate by remember { mutableStateOf(formatDate(route.startDate)) }
    var endDate by remember { mutableStateOf(route.endDate?.let { formatDate(it) } ?: "") }

    // Показания
    var startOdometer by remember { mutableStateOf(route.startOdometer.toString()) }
    var endOdometer by remember { mutableStateOf(route.endOdometer?.toString() ?: "") }
    var startEH by remember { mutableStateOf(route.startEH.toString()) }
    var endEH by remember { mutableStateOf(route.endEH?.toString() ?: "") }

    // Примечания
    var notes by remember { mutableStateOf(route.notes ?: "") }

    // UI состояние
    var showValidationErrors by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRevertDialog by remember { mutableStateOf(false) }

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

    // Диалоги
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить рейс?") },
            text = { Text("Рейс №$routeNumber будет удалён безвозвратно.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteRoute()
                    }
                ) {
                    Text("Удалить", color = StatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showRevertDialog) {
        AlertDialog(
            onDismissRequest = { showRevertDialog = false },
            title = { Text("Вернуть в черновики?") },
            text = { Text("Данные выгрузки будут удалены. Рейс станет черновиком.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRevertDialog = false
                        onRevertToDraft()
                        onNavigateBack()
                    }
                ) {
                    Text("Вернуть", color = StatusWarning)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevertDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    val isCompleted = route.status == RouteStatus.COMPLETED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Редактирование рейса")
                        Text(
                            "Рейс №${route.routeNumber} • ${if (isCompleted) "Завершён" else "Черновик"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCompleted) StatusActive else StatusWarning
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
                actions = {
                    // Кнопка удаления
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = StatusError
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

            // === ОСНОВНЫЕ ДАННЫЕ ===
            item {
                SectionHeader("Основные данные")
            }

            item {
                FormattedTextField(
                    value = routeNumber,
                    onValueChange = { routeNumber = it.filter { c -> c.isDigit() } },
                    label = "Номер рейса",
                    isError = showValidationErrors && routeNumber.isBlank(),
                    errorMessage = "Введите номер",
                    displayFormatter = { "№ $it" },
                    onFocus = { focusedItemIndex = 0 },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    )
                )
            }

            // === МАРШРУТ ===
            item {
                SectionHeader("Маршрут")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CountryInputField(
                        value = departureCountry,
                        onValueChange = { departureCountry = it },
                        label = "Загрузка",
                        isError = showValidationErrors && departureCountry.length < 2,
                        errorMessage = "2 буквы",
                        onFocus = { focusedItemIndex = 1 },
                        modifier = Modifier.weight(1f)
                    )

                    CountryInputField(
                        value = arrivalCountry,
                        onValueChange = { arrivalCountry = it },
                        label = "Выгрузка",
                        isError = showValidationErrors && arrivalCountry.isNotBlank() && arrivalCountry.length < 2,
                        errorMessage = "2 буквы",
                        onFocus = { focusedItemIndex = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // === ГРУЗ ===
            item {
                SectionHeader("Информация о грузе")
            }

            item {
                OutlinedTextField(
                    value = cargoName,
                    onValueChange = { cargoName = it },
                    label = { Text("Наименование груза *") },
                    isError = showValidationErrors && cargoName.isBlank(),
                    supportingText = if (showValidationErrors && cargoName.isBlank()) {
                        { Text("Обязательное поле", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 3 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Words
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormattedTextField(
                        value = cargoWeight,
                        onValueChange = { cargoWeight = it.filter { c -> c.isDigit() } },
                        label = "Вес",
                        isError = showValidationErrors && cargoWeight.isBlank(),
                        errorMessage = "Введите вес",
                        displayFormatter = { "${formatNumberWithSpaces(it)} кг" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 4 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Режим холодильника
                    ExposedDropdownMenuBox(
                        expanded = expandedRefrigeratorMenu,
                        onExpandedChange = { expandedRefrigeratorMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedRefrigeratorMode.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Режим") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRefrigeratorMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRefrigeratorMenu,
                            onDismissRequest = { expandedRefrigeratorMenu = false }
                        ) {
                            RefrigeratorMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.displayName) },
                                    onClick = {
                                        selectedRefrigeratorMode = mode
                                        expandedRefrigeratorMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = cmrNumber,
                    onValueChange = { cmrNumber = it },
                    label = { Text("Номер CMR *") },
                    isError = showValidationErrors && cmrNumber.isBlank(),
                    supportingText = if (showValidationErrors && cmrNumber.isBlank()) {
                        { Text("Обязательное поле", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 5 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            // === ТРАНСПОРТ ===
            item {
                SectionHeader("Транспорт")
            }

            item {
                FormattedTextField(
                    value = trailerNumber,
                    onValueChange = { trailerNumber = it.uppercase() },
                    label = "Номер прицепа",
                    isError = showValidationErrors && trailerNumber.isBlank(),
                    errorMessage = "Введите номер",
                    displayFormatter = { it },
                    onFocus = { focusedItemIndex = 6 },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Characters
                    )
                )
            }

            // === ДАТЫ ===
            item {
                SectionHeader("Даты")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatePickerDialogField(
                        date = startDate,
                        onDateSelected = { startDate = it },
                        label = "Дата загрузки *",
                        modifier = Modifier.weight(1f),
                        isError = showValidationErrors && startDate.isBlank()
                    )

                    DatePickerDialogField(
                        date = endDate,
                        onDateSelected = { endDate = it },
                        label = "Дата выгрузки",
                        modifier = Modifier.weight(1f),
                        isError = false
                    )
                }
            }

            // === ПОКАЗАНИЯ ===
            item {
                SectionHeader("Показания одометра")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormattedTextField(
                        value = startOdometer,
                        onValueChange = { startOdometer = it.filter { c -> c.isDigit() } },
                        label = "Загрузка",
                        isError = showValidationErrors && startOdometer.isBlank(),
                        errorMessage = "Введите",
                        displayFormatter = { "${formatNumberWithSpaces(it)} км" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 7 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    FormattedTextField(
                        value = endOdometer,
                        onValueChange = { endOdometer = it.filter { c -> c.isDigit() } },
                        label = "Выгрузка",
                        isError = false,
                        errorMessage = "",
                        displayFormatter = { if (it.isNotBlank()) "${formatNumberWithSpaces(it)} км" else "" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 8 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            item {
                SectionHeader("Моточасы")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormattedTextField(
                        value = startEH,
                        onValueChange = { startEH = it.filter { c -> c.isDigit() } },
                        label = "Начало",
                        isError = showValidationErrors && startEH.isBlank(),
                        errorMessage = "Введите",
                        displayFormatter = { "$it ч" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 9 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    FormattedTextField(
                        value = endEH,
                        onValueChange = { endEH = it.filter { c -> c.isDigit() } },
                        label = "Конец",
                        isError = false,
                        errorMessage = "",
                        displayFormatter = { if (it.isNotBlank()) "$it ч" else "" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 10 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Предпросмотр расчётов
            if (startOdometer.isNotBlank() && endOdometer.isNotBlank() &&
                startEH.isNotBlank() && endEH.isNotBlank()) {
                val startOdo = startOdometer.toIntOrNull() ?: 0
                val endOdo = endOdometer.toIntOrNull() ?: 0
                val startE = startEH.toIntOrNull() ?: 0
                val endE = endEH.toIntOrNull() ?: 0

                if (endOdo > startOdo && endE > startE) {
                    item {
                        CalculationPreviewCard(
                            startOdometer = startOdo,
                            endOdometer = endOdo,
                            startEH = startE,
                            endEH = endE
                        )
                    }
                }
            }

            // === ПРИМЕЧАНИЯ ===
            item {
                SectionHeader("Дополнительно")
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Примечания") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 11 },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            // === ДЕЙСТВИЯ ДЛЯ ЗАВЕРШЁННОГО РЕЙСА ===
            if (isCompleted) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = StatusWarning.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = StatusWarning.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Дополнительные действия",
                                style = MaterialTheme.typography.titleSmall,
                                color = StatusWarning
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { showRevertDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Вернуть в черновики (удалить данные выгрузки)",
                                    color = StatusWarning
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // === КНОПКИ ===
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val errors = mutableListOf<String>()

                            if (routeNumber.isBlank()) errors.add("Номер рейса")
                            if (departureCountry.length < 2) errors.add("Страна загрузки")
                            if (cargoName.isBlank()) errors.add("Наименование груза")
                            if (cargoWeight.isBlank()) errors.add("Вес груза")
                            if (cmrNumber.isBlank()) errors.add("Номер CMR")
                            if (trailerNumber.isBlank()) errors.add("Номер прицепа")
                            if (startDate.isBlank()) errors.add("Дата загрузки")
                            if (startOdometer.isBlank()) errors.add("Одометр загрузки")
                            if (startEH.isBlank()) errors.add("Моточасы начала")

                            val startOdo = startOdometer.toIntOrNull() ?: 0
                            val endOdo = endOdometer.toIntOrNull() ?: 0
                            val startE = startEH.toIntOrNull() ?: 0
                            val endE = endEH.toIntOrNull() ?: 0

                            // Проверка если заполнены данные выгрузки
                            val hasUnloadingData = arrivalCountry.isNotBlank() ||
                                    endDate.isNotBlank() ||
                                    endOdometer.isNotBlank() ||
                                    endEH.isNotBlank()

                            if (hasUnloadingData) {
                                if (arrivalCountry.length < 2) errors.add("Страна выгрузки (2 буквы)")
                                if (endDate.isBlank()) errors.add("Дата выгрузки")
                                if (endOdometer.isBlank()) errors.add("Одометр выгрузки")
                                if (endEH.isBlank()) errors.add("Моточасы конца")
                                if (endOdo <= startOdo) errors.add("Одометр выгрузки > одометра загрузки")
                                if (endE <= startE) errors.add("Моточасы конца > моточасов начала")
                            }

                            if (errors.isNotEmpty()) {
                                showValidationErrors = true
                                errorDialogMessage = "Исправьте ошибки:\n\n" +
                                        errors.joinToString("\n") { "• $it" }
                                showErrorDialog = true
                                return@Button
                            }

                            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            val startDateMillis = dateFormat.parse(startDate)?.time ?: 0
                            val endDateMillis = if (endDate.isNotBlank()) {
                                dateFormat.parse(endDate)?.time
                            } else null

                            val isNowCompleted = hasUnloadingData && endDateMillis != null

                            val updatedRoute = route.copy(
                                routeNumber = routeNumber.toInt(),
                                startDate = startDateMillis,
                                startOdometer = startOdo,
                                startCountry = departureCountry.uppercase(),
                                cargoName = cargoName,
                                cargoWeight = cargoWeight.toIntOrNull() ?: 0,
                                cmrNumber = cmrNumber,
                                cargoTemperature = selectedRefrigeratorMode.name,
                                cargoMode = selectedRefrigeratorMode.displayName,
                                trailerNumber = trailerNumber.uppercase(),
                                startEH = startE,
                                endDate = endDateMillis,
                                endOdometer = endOdometer.toIntOrNull(),
                                finishCountry = arrivalCountry.uppercase().takeIf { it.isNotBlank() },
                                endEH = endEH.toIntOrNull(),
                                totalEH = if (endE > startE) endE - startE else null,
                                routeMileage = if (endOdo > startOdo) endOdo - startOdo else 0,
                                status = if (isNowCompleted) RouteStatus.COMPLETED else RouteStatus.DRAFT,
                                notes = notes.takeIf { it.isNotBlank() }
                            )

                            onUpdateRoute(updatedRoute)
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Сохранить изменения")
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
private fun CalculationPreviewCard(
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
                text = "Расчётные значения",
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
fun EditRouteScreenPreview() {
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
        cargoTemperature = "AUTO",
        cargoMode = "Авто",
        trailerNumber = "AB 1234",
        startEH = 2500,
        endDate = System.currentTimeMillis(),
        endOdometer = 150450,
        finishCountry = "PL",
        endEH = 2510,
        totalEH = 10,
        routeMileage = 450,
        status = RouteStatus.COMPLETED,
        notes = "Тестовый рейс"
    )

    CadenceTheme {
        EditRouteContent(
            route = sampleRoute,
            isLoading = false,
            error = null,
            onNavigateBack = {},
            onUpdateRoute = {},
            onDeleteRoute = {},
            onRevertToDraft = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true, name = "Draft Route")
@Composable
fun EditRouteScreenDraftPreview() {
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
        notes = "Черновик"
    )

    CadenceTheme {
        EditRouteContent(
            route = sampleRoute,
            isLoading = false,
            error = null,
            onNavigateBack = {},
            onUpdateRoute = {},
            onDeleteRoute = {},
            onRevertToDraft = {},
            onClearError = {}
        )
    }
}