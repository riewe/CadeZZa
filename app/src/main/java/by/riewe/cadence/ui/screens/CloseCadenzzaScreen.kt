// by/riewe/cadence/ui/screens/CloseCadenzzaScreen.kt
package by.riewe.cadence.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.RvHookup
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.riewe.cadence.data.entity.CadenzzaEntity
import by.riewe.cadence.ui.components.CardText
import by.riewe.cadence.ui.components.DatePickerDialogField
import by.riewe.cadence.ui.components.NumberVisualTransformation
import by.riewe.cadence.ui.components.TimePickerDialogField
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.theme.StatusActive
import by.riewe.cadence.ui.theme.StatusClosed
import by.riewe.cadence.ui.viewmodel.CadenzzaViewModel
import by.riewe.cadence.utils.formatDate
import by.riewe.cadence.utils.formatTime
import by.riewe.cadence.utils.formatTrailerNumber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseCadenzzaScreen(
    viewModel: CadenzzaViewModel,
    cadenzzaId: Long,
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedCadenzza by viewModel.selectedCadenzza.collectAsState()

    LaunchedEffect(cadenzzaId) {
        viewModel.loadCadenzzaDetails(cadenzzaId)
    }

    val startCadenzza = selectedCadenzza?.cadenzza

    CloseCadenzzaContent(
        isLoading = isLoading,
        error = error,
        startCadenzza = startCadenzza,
        onNavigateBack = onNavigateBack,
        onCloseCadenzza = { endDate, endTime, endOdometer, endTrailerNumber, endTruckFuel, endTrailerFuel, endMH ->
            viewModel.closeCadenzza(
                cadenzzaId = cadenzzaId,
                endDate = endDate,
                endTime = endTime,
                endOdometer = endOdometer,
                endTrailerNumber = endTrailerNumber,
                endTruckFuel = endTruckFuel,
                endTrailerFuel = endTrailerFuel,
                endMH = endMH
            )
            onNavigateBack()
        },
        onClearError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseCadenzzaContent(
    isLoading: Boolean,
    error: String?,
    startCadenzza: CadenzzaEntity?,
    onNavigateBack: () -> Unit,
    onCloseCadenzza: (Long, Long, Int, String, Int, Int, Int) -> Unit,
    onClearError: () -> Unit
) {
    var endDate by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    // Числовые поля — храним только цифры, отображаем с пробелами через VisualTransformation
    var endOdometerField by remember { mutableStateOf(TextFieldValue("")) }
    var endTrailerNumberField by remember { mutableStateOf(TextFieldValue("")) }
    var endTruckFuelField by remember { mutableStateOf(TextFieldValue("")) }
    var endTrailerFuelField by remember { mutableStateOf(TextFieldValue("")) }
    var endMHField by remember { mutableStateOf(TextFieldValue("")) }

    var showValidationErrors by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }

    var focusedItemIndex by remember { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()

    // Расчет пробега
    val totalMileage = remember(endOdometerField.text, startCadenzza?.startOdometer) {
        val end = endOdometerField.text.toIntOrNull() ?: 0
        val start = startCadenzza?.startOdometer ?: 0
        if (end > start) end - start else 0
    }

    // Расчет дней
    val totalDays = remember(endDate, startCadenzza?.startDate) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val end = if (endDate.isNotBlank()) dateFormat.parse(endDate)?.time else null
        val start = startCadenzza?.startDate
        if (end != null && start != null) {
            ((end - start) / (1000 * 60 * 60 * 24)).toInt()
        } else 0
    }

    LaunchedEffect(focusedItemIndex) {
        if (focusedItemIndex >= 0) {
            delay(300)
            listState.animateScrollToItem(focusedItemIndex)
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            onClearError()
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Ошибка заполнения") },
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
                title = { Text("Закрыть каденцию") },
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

            // Информация о начале каденции
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val (backgroundColor, textColor) =
                                StatusActive to Color.White          // Зелёный для активной

                            Surface(
                                color = backgroundColor,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Каденция № ${startCadenzza?.cadenzzaNumber} (" + startCadenzza?.truckNumber + ")",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = textColor
                                )
                            }
                        }

                        CardText("Начало:", "${formatDate(startCadenzza?.startDate)} ${formatTime(startCadenzza?.startTime)}")
                        CardText("Одометр начало:", "${startCadenzza?.startOdometer?.let { formatNumberWithSpaces(it.toString()) } ?: "-"} км")
                        CardText("Начало с прицепом:", startCadenzza?.startTrailerNumber ?: "-")
                    }
                }
            }

            // Дата окончания
            item {
                DatePickerDialogField(
                    date = endDate,
                    onDateSelected = { endDate = it },
                    label = "Дата окончания *",
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    isError = showValidationErrors && endDate.isBlank()
                )
            }

            // Время окончания
            item {
                TimePickerDialogField(
                    time = endTime,
                    onTimeSelected = { endTime = it },
                    label = "Время окончания *",
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    isError = showValidationErrors && endTime.isBlank()
                )
            }

            // Конечный одометр — с VisualTransformation
            item {
                OutlinedTextField(
                    value = endOdometerField,
                    onValueChange = { newValue ->
                        endOdometerField = newValue.copy(
                            text = newValue.text.filter { it.isDigit() }
                        )
                    },
                    label = { Text("Одометр конец (км) *") },
                    isError = showValidationErrors && endOdometerField.text.isBlank(),
                    supportingText = if (showValidationErrors && endOdometerField.text.isBlank()) {
                        { Text("Введите значение", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 3 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation()
                )
            }

            // Топливо тягача конец — с VisualTransformation
            item {
                OutlinedTextField(
                    value = endTruckFuelField,
                    onValueChange = { newValue ->
                        endTruckFuelField = newValue.copy(
                            text = newValue.text.filter { it.isDigit() }
                        )
                    },
                    label = { Text("Остаток топлива в тягаче (л) *") },
                    isError = showValidationErrors && endTruckFuelField.text.isBlank(),
                    supportingText = if (showValidationErrors && endTruckFuelField.text.isBlank()) {
                        { Text("Введите значение", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 5 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation()
                )
            }

            // Номер прицепа при сдаче
            item {
                OutlinedTextField(
                    value = endTrailerNumberField,
                    onValueChange = { newValue ->
                        val (formatted, cursorPos) = formatTrailerNumber(newValue.text)
                        if (formatted.length <= 7) {
                            endTrailerNumberField = TextFieldValue(
                                text = formatted,
                                selection = androidx.compose.ui.text.TextRange(cursorPos)
                            )
                        }
                    },
                    label = { Text("Номер прицепа *") },
                    isError = showValidationErrors && endTrailerNumberField.text.length < 6,
                    supportingText = if (showValidationErrors && endTrailerNumberField.text.length < 6) {
                        { Text("Формат: AB 1234", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = { Icon(Icons.Default.RvHookup, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 4 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            // Топливо прицепа конец — с VisualTransformation
            item {
                OutlinedTextField(
                    value = endTrailerFuelField,
                    onValueChange = { newValue ->
                        endTrailerFuelField = newValue.copy(
                            text = newValue.text.filter { it.isDigit() }
                        )
                    },
                    label = { Text("Остаток топлива в прицепе (л) *") },
                    isError = showValidationErrors && endTrailerFuelField.text.isBlank(),
                    supportingText = if (showValidationErrors && endTrailerFuelField.text.isBlank()) {
                        { Text("Введите значение", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 6 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation()
                )
            }

            // Моточасы конец — с VisualTransformation
            item {
                OutlinedTextField(
                    value = endMHField,
                    onValueChange = { newValue ->
                        endMHField = newValue.copy(
                            text = newValue.text.filter { it.isDigit() }
                        )
                    },
                    label = { Text("Моточасы *") },
                    isError = showValidationErrors && endMHField.text.isBlank(),
                    supportingText = if (showValidationErrors && endMHField.text.isBlank()) {
                        { Text("Введите значение", color = MaterialTheme.colorScheme.error) }
                    } else null,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        .onFocusChanged { if (it.isFocused) focusedItemIndex = 7 },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation()
                )
            }

            // Итоги
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Итоги каденции",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        CardText("Пробег:", "${formatNumberWithSpaces(totalMileage.toString())} км")
                        CardText("Продолжительность каденции:", "$totalDays")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Кнопка закрытия
            item {
                Button(
                    onClick = {
                        val emptyFields = mutableListOf<String>()

                        if (endDate.isBlank()) emptyFields.add("Дата окончания")
                        if (endTime.isBlank()) emptyFields.add("Время окончания")
                        if (endOdometerField.text.isBlank()) emptyFields.add("Одометр конец")
                        if (endTrailerNumberField.text.length < 6) emptyFields.add("Номер прицепа при сдаче")
                        if (endTruckFuelField.text.isBlank()) emptyFields.add("Топливо тягача конец")
                        if (endTrailerFuelField.text.isBlank()) emptyFields.add("Топливо прицепа конец")
                        if (endMHField.text.isBlank()) emptyFields.add("Моточасы конец")

                        val endOdometer = endOdometerField.text.toIntOrNull() ?: 0
                        val startOdometer = startCadenzza?.startOdometer ?: 0
                        if (endOdometer in 1..startOdometer) {
                            emptyFields.add("Одометр конец должен быть больше начального ($startOdometer)")
                        }

                        showValidationErrors = true

                        if (emptyFields.isNotEmpty()) {
                            errorDialogMessage = "Пожалуйста, исправьте следующие ошибки:\n\n" +
                                    emptyFields.joinToString("\n") { "• $it" }
                            showErrorDialog = true
                        } else {
                            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            val dateMillis = dateFormat.parse(endDate)?.time ?: System.currentTimeMillis()

                            val timeParts = endTime.split(":")
                            val timeMillis = if (timeParts.size == 2) {
                                (timeParts[0].toInt() * 60 + timeParts[1].toInt()) * 60 * 1000L
                            } else 0L

                            onCloseCadenzza(
                                dateMillis,
                                timeMillis,
                                endOdometerField.text.toIntOrNull() ?: 0,
                                endTrailerNumberField.text,
                                endTruckFuelField.text.toIntOrNull() ?: 0,
                                endTrailerFuelField.text.toIntOrNull() ?: 0,
                                endMHField.text.toIntOrNull() ?: 0
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
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Закрыть каденцию")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}


// Вспомогательная функция для форматирования (используется в CardText)
private fun formatNumberWithSpaces(number: String): String {
    val digitsOnly = number.filter { it.isDigit() }
    if (digitsOnly.length <= 3) return digitsOnly
    val reversed = digitsOnly.reversed()
    val spaced = reversed.chunked(3).joinToString(" ")
    return spaced.reversed()
}

@Preview(showBackground = true)
@Composable
fun CloseCadenzzaScreenPreview() {
    CadenceTheme {
        CloseCadenzzaContent(
            isLoading = false,
            error = null,
            startCadenzza = CadenzzaEntity(
                id = 2,
                cadenzzaNumber = "2",
                startDate = 1760425200L,
                startTime = 0L,
                driver1 = "John Doe",
                driver2 = "Vin Diesel",
                endDate = null,
                endTime = null,
                truckNumber = "ABC 123",
                startTrailerNumber = "XYZ 789",
                endTrailerNumber = null,
                startOdometer = 100000,
                endOdometer = null,
                startTruckFuel = 500,
                endTruckFuel = null,
                startTrailerFuel = 0,
                endTrailerFuel = null,
                startMH = 100,
                endMH = null,
                totalMileage = 0,
                totalDays = 0
            ),
            onNavigateBack = {},
            onCloseCadenzza = { _, _, _, _, _, _, _ -> },
            onClearError = {}
        )
    }
}