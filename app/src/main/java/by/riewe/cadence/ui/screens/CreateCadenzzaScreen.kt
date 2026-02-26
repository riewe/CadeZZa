// by/riewe/cadence/ui/screens/CreateCadenzzaScreen.kt
package by.riewe.cadence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RvHookup
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.riewe.cadence.ui.components.DatePickerDialogField
import by.riewe.cadence.ui.components.NumberVisualTransformation
import by.riewe.cadence.ui.components.TimePickerDialogField
import by.riewe.cadence.ui.components.ValidatedTextField
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.viewmodel.CadenzzaViewModel
import by.riewe.cadence.utils.formatDriverInput
import by.riewe.cadence.utils.formatTrailerNumber
import by.riewe.cadence.utils.formatTruckNumber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCadenzzaScreen(
    viewModel: CadenzzaViewModel,
    suggestedNumber: String?,
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSuggestedNumber()
        }
    }

    CreateCadenzzaContent(
        isLoading = isLoading,
        error = error,
        suggestedNumber = suggestedNumber,
        onNavigateBack = onNavigateBack,
        onCreateCadenzza = { cadenzzaNumber, driver1, driver2, truckNumber, startTrailerNumber, startDate, startTime, startOdometer, startTruckFuel, startTrailerFuel, startMH ->
            createCadenzza(
                viewModel = viewModel,
                cadenzzaNumber = cadenzzaNumber,
                driver1 = driver1,
                driver2 = driver2,
                truckNumber = truckNumber,
                startTrailerNumber = startTrailerNumber,
                startDate = startDate,
                startTime = startTime,
                startOdometer = startOdometer,
                startTruckFuel = startTruckFuel,
                startTrailerFuel = startTrailerFuel,
                startMH = startMH,
                onSuccess = onNavigateBack
            )
        },
        onClearError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCadenzzaContent(
    isLoading: Boolean,
    error: String?,
    suggestedNumber: String?,
    onNavigateBack: () -> Unit,
    onCreateCadenzza: (String, String, String?, String, String, String, String, String, String, String, String) -> Unit,
    onClearError: () -> Unit
) {
    // Поля формы
    var cadenzzaNumber by remember(suggestedNumber) {
        mutableStateOf(suggestedNumber ?: "")
    }
    var driver1 by remember { mutableStateOf("") }
    var driver2 by remember { mutableStateOf("") }

    var truckNumberField by remember { mutableStateOf(TextFieldValue("")) }
    var trailerNumberField by remember { mutableStateOf(TextFieldValue("")) }

    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }

    // Числовые поля — храним только цифры, отображаем с пробелами через VisualTransformation
    var startOdometerField by remember { mutableStateOf(TextFieldValue("")) }
    var startTruckFuelField by remember { mutableStateOf(TextFieldValue("")) }
    var startTrailerFuelField by remember { mutableStateOf(TextFieldValue("")) }
    var startMHField by remember { mutableStateOf(TextFieldValue("")) }

    // Состояния валидации и ошибок
    var showValidationErrors by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }

    // Для автоскролла к полю с фокусом
    var focusedItemIndex by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()

    LaunchedEffect(focusedItemIndex) {
        if (focusedItemIndex >= 0) {
            delay(300)
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
                title = { Text("Новая каденция") },
                colors = TopAppBarDefaults.topAppBarColors(),
                modifier = Modifier.shadow(10.dp),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Номер каденции
            item {
                OutlinedTextField(
                    value = cadenzzaNumber,
                    onValueChange = { cadenzzaNumber = it.filter { char -> char.isDigit() } },
                    label = { Text("Номер каденции *") },
                    isError = showValidationErrors && cadenzzaNumber.isBlank(),
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    supportingText = {
                        when {
                            showValidationErrors && cadenzzaNumber.isBlank() -> {
                                Text("Введите номер каденции", color = MaterialTheme.colorScheme.error)
                            }
                            suggestedNumber != null && cadenzzaNumber == suggestedNumber -> {
                                Text("Автоматически (можно изменить)", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 1 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    )
                )
            }

            // Водитель 1
            item {
                ValidatedTextField(
                    value = driver1,
                    onValueChange = { driver1 = formatDriverInput(it) },
                    label = "Водитель 1 *",
                    isError = showValidationErrors && driver1.isBlank(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    errorMessage = "Введите имя водителя",
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 2 },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    )
                )
            }

            // Водитель 2
            item {
                OutlinedTextField(
                    value = driver2,
                    onValueChange = { driver2 = formatDriverInput(it) },
                    label = { Text("Водитель 2") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 3 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    )
                )
            }

            // Номер тягача
            item {
                val truckText = truckNumberField.text
                OutlinedTextField(
                    value = truckNumberField,
                    onValueChange = { newValue ->
                        val (formatted, cursorPos) = formatTruckNumber(newValue.text)
                        if (formatted.length <= 7) {
                            truckNumberField = TextFieldValue(
                                text = formatted,
                                selection = androidx.compose.ui.text.TextRange(cursorPos)
                            )
                        }
                    },
                    label = { Text("Номер тягача *") },
                    leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                    isError = showValidationErrors && truckText.length < 7,
                    supportingText = if (showValidationErrors && truckText.length < 7) {
                        { Text("Формат: ABC 123", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 4 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    )
                )
            }

            // Номер прицепа
            item {
                val trailerText = trailerNumberField.text
                OutlinedTextField(
                    value = trailerNumberField,
                    onValueChange = { newValue ->
                        val (formatted, cursorPos) = formatTrailerNumber(newValue.text)
                        if (formatted.length <= 7) {
                            trailerNumberField = TextFieldValue(
                                text = formatted,
                                selection = androidx.compose.ui.text.TextRange(cursorPos)
                            )
                        }
                    },
                    label = { Text("Номер прицепа *") },
                    leadingIcon = { Icon(Icons.Default.RvHookup, contentDescription = null) },
                    isError = showValidationErrors && trailerText.length < 6,
                    supportingText = if (showValidationErrors && trailerText.length < 6) {
                        { Text("Формат: AB 123", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 5 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    )
                )
            }

            // Дата начала
            item {
                DatePickerDialogField(
                    date = startDate,
                    onDateSelected = { startDate = it },
                    label = "Дата начала *",
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationErrors && startDate.isBlank()
                )
            }

            // Время начала
            item {
                TimePickerDialogField(
                    time = startTime,
                    onTimeSelected = { startTime = it },
                    label = "Время начала *",
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationErrors && startTime.isBlank()
                )
            }

            // Одометр — с VisualTransformation
            item {
                OutlinedTextField(
                    value = startOdometerField,
                    onValueChange = { newValue ->
                        // Сохраняем только цифры, позицию курсора обрабатывает VisualTransformation
                        startOdometerField = newValue.copy(
                            text = newValue.text.filter { it.isDigit() }
                        )
                    },
                    label = { Text("Одометр (км) *") },
                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                    isError = showValidationErrors && startOdometerField.text.isBlank(),
                    supportingText = if (showValidationErrors && startOdometerField.text.isBlank()) {
                        { Text("Введите значение", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 8 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation() // ← Форматирование с пробелами
                )
            }

            // Топливо тягача — с VisualTransformation
            item {
                OutlinedTextField(
                    value = startTruckFuelField,
                    onValueChange = { newValue ->
                        startTruckFuelField = newValue.copy(
                            text = newValue.text.filter { it.isDigit() }
                        )
                    },
                    label = { Text("Топливо тягача (л) *") },
                    leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                    isError = showValidationErrors && startTruckFuelField.text.isBlank(),
                    supportingText = if (showValidationErrors && startTruckFuelField.text.isBlank()) {
                        { Text("Введите значение", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 9 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation()
                )
            }

            // Топливо прицепа — с VisualTransformation
            item {
                OutlinedTextField(
                    value = startTrailerFuelField,
                    onValueChange = { newValue ->
                        startTrailerFuelField = newValue.copy(
                            text = newValue.text.filter { it.isDigit() }
                        )
                    },
                    label = { Text("Топливо прицепа (л) *") },
                    leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                    isError = showValidationErrors && startTrailerFuelField.text.isBlank(),
                    supportingText = if (showValidationErrors && startTrailerFuelField.text.isBlank()) {
                        { Text("Введите значение", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 10 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation()
                )
            }

            // Моточасы — с VisualTransformation
            item {
                OutlinedTextField(
                    value = startMHField,
                    onValueChange = { newValue ->
                        startMHField = newValue.copy(
                            text = newValue.text.filter { it.isDigit() }
                        )
                    },
                    label = { Text("Моточасы *") },
                    leadingIcon = {
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.offset(x = (-4).dp, y = (-4).dp)
                            )
                            Icon(
                                Icons.Default.AcUnit,
                                contentDescription = null,
                                modifier = Modifier
                                    .offset(x = 10.dp, y = 6.dp)
                                    .size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    isError = showValidationErrors && startMHField.text.isBlank(),
                    supportingText = if (showValidationErrors && startMHField.text.isBlank()) {
                        { Text("Введите значение", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 11 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation()
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Кнопка создания
            item {
                Button(
                    onClick = {
                        val emptyFields = mutableListOf<String>()

                        if (cadenzzaNumber.isBlank()) emptyFields.add("Номер каденции")
                        if (driver1.isBlank()) emptyFields.add("Водитель 1")
                        if (truckNumberField.text.length < 7) emptyFields.add("Номер тягача (ABC 123)")
                        if (trailerNumberField.text.length < 6) emptyFields.add("Номер прицепа (AB 1234)")
                        if (startDate.isBlank()) emptyFields.add("Дата начала")
                        if (startTime.isBlank()) emptyFields.add("Время начала")
                        if (startOdometerField.text.isBlank()) emptyFields.add("Одометр")
                        if (startTruckFuelField.text.isBlank()) emptyFields.add("Топливо тягача")
                        if (startTrailerFuelField.text.isBlank()) emptyFields.add("Топливо прицепа")
                        if (startMHField.text.isBlank()) emptyFields.add("Моточасы")

                        showValidationErrors = true

                        if (emptyFields.isNotEmpty()) {
                            errorDialogMessage = "Пожалуйста, заполните следующие обязательные поля:\n\n" +
                                    emptyFields.joinToString("\n") { "• $it" }
                            showErrorDialog = true
                        } else {
                            onCreateCadenzza(
                                cadenzzaNumber,
                                driver1,
                                driver2.takeIf { it.isNotBlank() },
                                truckNumberField.text,
                                trailerNumberField.text,
                                startDate,
                                startTime,
                                startOdometerField.text, // Уже только цифры
                                startTruckFuelField.text,
                                startTrailerFuelField.text,
                                startMHField.text
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = MaterialTheme.colorScheme.primary,
                            spotColor = MaterialTheme.colorScheme.primary
                        ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Создать каденцию")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

private fun createCadenzza(
    viewModel: CadenzzaViewModel,
    cadenzzaNumber: String,
    driver1: String,
    driver2: String?,
    truckNumber: String,
    startTrailerNumber: String,
    startDate: String,
    startTime: String,
    startOdometer: String,
    startTruckFuel: String,
    startTrailerFuel: String,
    startMH: String,
    onSuccess: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dateMillis = dateFormat.parse(startDate)?.time ?: System.currentTimeMillis()

    val timeParts = startTime.split(":")
    val timeMillis = if (timeParts.size == 2) {
        (timeParts[0].toInt() * 60 + timeParts[1].toInt()) * 60 * 1000L
    } else {
        0L
    }

    viewModel.createCadenzza(
        cadenzzaNumber = cadenzzaNumber,
        driver1 = driver1,
        driver2 = driver2,
        startDate = dateMillis,
        startTime = timeMillis,
        truckNumber = truckNumber,
        startOdometer = startOdometer.toIntOrNull() ?: 0,
        startTrailerNumber = startTrailerNumber,
        startTruckFuel = startTruckFuel.toIntOrNull() ?: 0,
        startTrailerFuel = startTrailerFuel.toIntOrNull() ?: 0,
        startMH = startMH.toIntOrNull() ?: 0
    )

    onSuccess()
}

@Preview(showBackground = true)
@Composable
fun CreateCadenzzaScreenPreview() {
    CadenceTheme {
        CreateCadenzzaContent(
            isLoading = false,
            error = null,
            suggestedNumber = "5",
            onNavigateBack = {},
            onCreateCadenzza = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onClearError = {}
        )
    }
}

@Preview(showBackground = true, name = "With Validation Errors")
@Composable
fun CreateCadenzzaScreenErrorPreview() {
    CadenceTheme {
        CreateCadenzzaContent(
            isLoading = false,
            error = null,
            suggestedNumber = "",
            onNavigateBack = {},
            onCreateCadenzza = { _, _, _, _, _, _, _, _, _, _, _ -> },
            onClearError = {}
        )
    }
}