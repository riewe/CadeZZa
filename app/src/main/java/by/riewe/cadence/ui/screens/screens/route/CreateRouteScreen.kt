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
import androidx.compose.material.icons.filled.ConfirmationNumber
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import by.riewe.cadence.data.entity.RefrigeratorMode
import by.riewe.cadence.data.entity.RouteStatus
import by.riewe.cadence.ui.components.CountryInputField
import by.riewe.cadence.ui.components.DatePickerDialogField
import by.riewe.cadence.ui.components.NumberVisualTransformation
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
fun CreateRouteScreen(
    periodId: Long,
    viewModel: RouteViewModel,
    suggestedRouteNumber: Int? = null,
    onNavigateBack: () -> Unit,
    onRouteCreated: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearError()
        }
    }

    CreateRouteContent(
        periodId = periodId,
        suggestedRouteNumber = suggestedRouteNumber,
        isLoading = isLoading,
        error = error,
        onNavigateBack = onNavigateBack,
        onCreateRoute = { routeData ->
            // Определяем статус по заполненности полей выгрузки
            val isCompleted = routeData.arrivalCountry.isNotBlank() &&
                    routeData.endDate > 0 &&
                    routeData.endOdometer > routeData.startOdometer &&
                    routeData.endEH > routeData.startEH

            if (isCompleted) {
                viewModel.createCompleteRoute(
                    periodId = periodId,
                    routeNumber = routeData.routeNumber,
                    startDate = routeData.startDate,
                    startOdometer = routeData.startOdometer,
                    endDate = routeData.endDate,
                    endOdometer = routeData.endOdometer,
                    departureCountry = routeData.departureCountry,
                    arrivalCountry = routeData.arrivalCountry,
                    cargoName = routeData.cargoName,
                    cargoWeight = routeData.cargoWeight,
                    cmrNumber = routeData.cmrNumber,
                    cargoTemperature = routeData.cargoTemperature,
                    cargoMode = routeData.cargoMode,
                    trailerNumber = routeData.trailerNumber,
                    startEH = routeData.startEH,
                    endEH = routeData.endEH,
                    notes = routeData.notes
                )
            } else {
                viewModel.createDraftRoute(
                    periodId = periodId,
                    routeNumber = routeData.routeNumber,
                    startDate = routeData.startDate,
                    startOdometer = routeData.startOdometer,
                    departureCountry = routeData.departureCountry,
                    cargoName = routeData.cargoName,
                    cargoWeight = routeData.cargoWeight,
                    cmrNumber = routeData.cmrNumber,
                    cargoTemperature = routeData.cargoTemperature,
                    cargoMode = routeData.cargoMode,
                    trailerNumber = routeData.trailerNumber,
                    startEH = routeData.startEH,
                    notes = routeData.notes
                )
            }
            onRouteCreated()
        },
        onClearError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRouteContent(
    periodId: Long,
    suggestedRouteNumber: Int?,
    isLoading: Boolean,
    error: String?,
    onNavigateBack: () -> Unit,
    onCreateRoute: (RouteData) -> Unit,
    onClearError: () -> Unit
) {
    // === ДАННЫЕ ЗАГРУЗКИ (обязательные) ===
    var routeNumber by remember(suggestedRouteNumber) {
        mutableStateOf(suggestedRouteNumber?.toString() ?: "")
    }
    var departureCountry by remember { mutableStateOf("") }
    var cargoName by remember { mutableStateOf("") }
    var cargoWeight by remember { mutableStateOf("") }
    var cmrNumber by remember { mutableStateOf("") }
    var trailerNumber by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var startOdometer by remember { mutableStateOf("") }
    var startEH by remember { mutableStateOf("") }

    // === РЕЖИМ ХОЛОДИЛЬНИКА ===
    var selectedRefrigeratorMode by remember { mutableStateOf(RefrigeratorMode.OFF) }
    var expandedRefrigeratorMenu by remember { mutableStateOf(false) }

    // === ДАННЫЕ ВЫГРУЗКИ (опциональные) ===
    var arrivalCountry by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var endOdometer by remember { mutableStateOf("") }
    var endEH by remember { mutableStateOf("") }

    // === ПРИМЕЧАНИЯ ===
    var notes by remember { mutableStateOf("") }

    // Состояние валидации и UI
    var showValidationErrors by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var createdRouteStatus by remember { mutableStateOf<RouteStatus?>(null) }

    // Для автоскролла к полю с фокусом
    val listState = rememberLazyListState()
    var focusedItemIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(focusedItemIndex) {
        if (focusedItemIndex >= 0) {
            kotlinx.coroutines.delay(300)
            listState.animateScrollToItem(focusedItemIndex)
        }
    }

    // Обработка ошибок
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

    if (showSuccessDialog) {
        val isDraft = createdRouteStatus == RouteStatus.DRAFT
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateBack()
            },
            title = { Text(if (isDraft) "Черновик создан" else "Рейс завершён") },
            text = {
                Text(
                    if (isDraft) {
                        "Рейс сохранён как черновик. Вы можете дополнить данные выгрузки позже."
                    } else {
                        "Рейс полностью завершён и сохранён."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Понятно")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Новый рейс")
                        Text(
                            "Период #$periodId",
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

            // === НОМЕР РЕЙСА ===
            item {
                FormattedTextField(
                    value = routeNumber,
                    onValueChange = { routeNumber = it.filter { c -> c.isDigit() } },
                    label = "Номер рейса",
                    isError = showValidationErrors && routeNumber.isBlank(),
                    errorMessage = "Введите номер рейса",
                    displayFormatter = { "№ $it" },
                    supportingText = {
                        if (suggestedRouteNumber != null && routeNumber == suggestedRouteNumber.toString()) {
                            Text("Предложен автоматически", color = MaterialTheme.colorScheme.primary)
                        }
                    },
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
                CountryInputField(
                    value = departureCountry,
                    onValueChange = { departureCountry = it },
                    label = "Страна загрузки",
                    isError = showValidationErrors && departureCountry.length < 2,
                    errorMessage = "Формат: DE, PL, FR",
                    onFocus = { focusedItemIndex = 1 },
                    modifier = Modifier.fillMaxWidth()
                )
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
                        { Text("Укажите название груза", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 2 },
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
                        onFocus = { focusedItemIndex = 3 },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        )
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
                        { Text("Укажите номер CMR", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 4 },
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
                    errorMessage = "Введите номер прицепа",
                    displayFormatter = { it },
                    onFocus = { focusedItemIndex = 5 },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Characters
                    )
                )
            }

            // === ДАТА ЗАГРУЗКИ ===
            item {
                SectionHeader("Дата загрузки")
            }

            item {
                DatePickerDialogField(
                    date = startDate,
                    onDateSelected = { startDate = it },
                    label = "Дата загрузки *",
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationErrors && startDate.isBlank()
                )
            }

            // === ПОКАЗАНИЯ ЗАГРУЗКИ ===
            item {
                SectionHeader("Показания прибора")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormattedTextField(
                        value = startOdometer,
                        onValueChange = { startOdometer = it.filter { c -> c.isDigit() } },
                        label = "Одометр",
                        isError = showValidationErrors && startOdometer.isBlank(),
                        errorMessage = "Введите одометр",
                        displayFormatter = { "${formatNumberWithSpaces(it)} км" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 6 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    FormattedTextField(
                        value = startEH,
                        onValueChange = { startEH = it.filter { c -> c.isDigit() } },
                        label = "Моточасы",
                        isError = showValidationErrors && startEH.isBlank(),
                        errorMessage = "Введите моточасы",
                        displayFormatter = { "$it ч" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 7 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // === ДАННЫЕ ВЫГРУЗКИ (ОПЦИОНАЛЬНО) ===
            item {
                SectionHeader("Данные выгрузки (опционально)")
            }

            item {
                InfoCard(
                    title = "Завершение рейса",
                    message = "Заполните эти поля сейчас, если уже знаете данные выгрузки. " +
                            "Или оставьте пустыми — рейс будет сохранён как черновик."
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                CountryInputField(
                    value = arrivalCountry,
                    onValueChange = { arrivalCountry = it },
                    label = "Страна выгрузки",
                    isError = showValidationErrors && arrivalCountry.isNotBlank() && arrivalCountry.length < 2,
                    errorMessage = "Формат: DE, PL, FR",
                    onFocus = { focusedItemIndex = 8 },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DatePickerDialogField(
                    date = endDate,
                    onDateSelected = { endDate = it },
                    label = "Дата выгрузки",
                    modifier = Modifier.fillMaxWidth(),
                    isError = false
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
                        isError = false,
                        errorMessage = "",
                        displayFormatter = { "${formatNumberWithSpaces(it)} км" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 9 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    FormattedTextField(
                        value = endEH,
                        onValueChange = { endEH = it.filter { c -> c.isDigit() } },
                        label = "Моточасы",
                        isError = false,
                        errorMessage = "",
                        displayFormatter = { "$it ч" },
                        modifier = Modifier.weight(1f),
                        onFocus = { focusedItemIndex = 10 },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Предпросмотр пробега если введены данные
            if (endOdometer.isNotBlank() && startOdometer.isNotBlank()) {
                val endVal = endOdometer.toIntOrNull() ?: 0
                val startVal = startOdometer.toIntOrNull() ?: 0
                if (endVal > startVal) {
                    item {
                        MileagePreviewCard(
                            startOdometer = startVal,
                            endOdometer = endVal,
                            startEH = startEH.toIntOrNull() ?: 0,
                            endEH = endEH.toIntOrNull() ?: 0
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
                    placeholder = { Text("Любая дополнительная информация...") },
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

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // === КНОПКИ ===
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val emptyFields = mutableListOf<String>()

                            if (routeNumber.isBlank()) emptyFields.add("Номер рейса")
                            if (departureCountry.length < 2) emptyFields.add("Страна загрузки")
                            if (cargoName.isBlank()) emptyFields.add("Наименование груза")
                            if (cargoWeight.isBlank()) emptyFields.add("Вес груза")
                            if (cmrNumber.isBlank()) emptyFields.add("Номер CMR")
                            if (trailerNumber.isBlank()) emptyFields.add("Номер прицепа")
                            if (startDate.isBlank()) emptyFields.add("Дата загрузки")
                            if (startOdometer.isBlank()) emptyFields.add("Одометр загрузки")
                            if (startEH.isBlank()) emptyFields.add("Моточасы загрузки")

                            if (emptyFields.isNotEmpty()) {
                                showValidationErrors = true
                                errorDialogMessage = "Заполните обязательные поля:\n\n" +
                                        emptyFields.joinToString("\n") { "• $it" }
                                showErrorDialog = true
                                return@Button
                            }

                            // Проверка данных выгрузки
                            val hasUnloadingData = arrivalCountry.isNotBlank() ||
                                    endDate.isNotBlank() ||
                                    endOdometer.isNotBlank() ||
                                    endEH.isNotBlank()

                            val isComplete = if (hasUnloadingData) {
                                val errors = mutableListOf<String>()

                                if (arrivalCountry.length < 2) errors.add("Страна выгрузки (2 буквы)")
                                if (endDate.isBlank()) errors.add("Дата выгрузки")
                                if (endOdometer.isBlank()) errors.add("Одометр выгрузки")
                                if (endEH.isBlank()) errors.add("Моточасы выгрузки")

                                val endVal = endOdometer.toIntOrNull() ?: 0
                                val startVal = startOdometer.toIntOrNull() ?: 0
                                val endEHVal = endEH.toIntOrNull() ?: 0
                                val startEHVal = startEH.toIntOrNull() ?: 0

                                if (endVal <= startVal) {
                                    errors.add("Одометр выгрузки должен быть больше одометра загрузки")
                                }
                                if (endEHVal <= startEHVal) {
                                    errors.add("Моточасы выгрузки должны быть больше моточасов загрузки")
                                }

                                if (errors.isNotEmpty()) {
                                    showValidationErrors = true
                                    errorDialogMessage = "Исправьте ошибки в данных выгрузки:\n\n" +
                                            errors.joinToString("\n") { "• $it" }
                                    showErrorDialog = true
                                    return@Button
                                }
                                true
                            } else false

                            // Конвертация дат
                            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            val startDateMillis = dateFormat.parse(startDate)?.time
                                ?: System.currentTimeMillis()

                            val endDateMillis = if (endDate.isNotBlank()) {
                                dateFormat.parse(endDate)?.time ?: 0
                            } else 0

                            onCreateRoute(
                                RouteData(
                                    routeNumber = routeNumber.toInt(),
                                    startDate = startDateMillis,
                                    startOdometer = startOdometer.toInt(),
                                    departureCountry = departureCountry.uppercase(),
                                    cargoName = cargoName,
                                    cargoWeight = cargoWeight.toInt(),
                                    cmrNumber = cmrNumber,
                                    cargoTemperature = selectedRefrigeratorMode.name,
                                    cargoMode = selectedRefrigeratorMode.displayName,
                                    trailerNumber = trailerNumber.uppercase(),
                                    startEH = startEH.toInt(),
                                    arrivalCountry = arrivalCountry.uppercase(),
                                    endDate = endDateMillis,
                                    endOdometer = endOdometer.toIntOrNull() ?: 0,
                                    endEH = endEH.toIntOrNull() ?: 0,
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                            )
                            createdRouteStatus = if (isComplete) RouteStatus.COMPLETED else RouteStatus.DRAFT
                            showSuccessDialog = true
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
                            Text("Сохранить рейс")
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



data class RouteData(
    val routeNumber: Int,
    val startDate: Long,
    val startOdometer: Int,
    val departureCountry: String,
    val cargoName: String,
    val cargoWeight: Int,
    val cmrNumber: String,
    val cargoTemperature: String,
    val cargoMode: String,
    val trailerNumber: String,
    val startEH: Int,
    val arrivalCountry: String,
    val endDate: Long,
    val endOdometer: Int,
    val endEH: Int,
    val notes: String?
)

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
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    // Вычисляем текст заранее
    val supportText = when {
        isError -> errorMessage
        value.isNotEmpty() -> displayFormatter(value)
        else -> null
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label *") },
        isError = isError,
        supportingText = {
            // Теперь здесь безопасно использовать @Composable
            if (supportText != null) {
                Text(
                    text = supportText,
                    color = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
            } else {
                supportingText?.invoke()
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
private fun InfoCard(title: String, message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = StatusWarning.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = StatusWarning.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = StatusWarning,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                text = "Предварительный расчёт",
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
fun CreateRouteScreenPreview() {
    CadenceTheme {
        CreateRouteContent(
            periodId = 1,
            suggestedRouteNumber = 5,
            isLoading = false,
            error = null,
            onNavigateBack = {},
            onCreateRoute = {},
            onClearError = {}
        )
    }
}