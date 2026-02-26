package by.riewe.cadence.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import by.riewe.cadence.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogField(
    modifier: Modifier = Modifier,
    date: String,
    onDateSelected: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        onDateSelected(sdf.format(Date(millis)))
                    }
                    showDialog = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = date,
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Выбрать дату")
            }
        },
        isError = isError,
        supportingText = supportingText,
        colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogField(
    modifier: Modifier = Modifier,
    time: String,
    onTimeSelected: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(is24Hour = true)

    if (showDialog) {
        // Material3 Dialog вместо android.app.TimePickerDialog
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.wrapContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Заголовок
                    Text(
                        text = stringResource(R.string.select_time),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // TimePicker
                    TimePicker(state = timePickerState)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Кнопки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(onClick = {
                            val hour = timePickerState.hour
                            val minute = timePickerState.minute
                            val newTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                            onTimeSelected(newTime)
                            showDialog = false
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }

    OutlinedTextField(
        value = time,
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.AccessTime, contentDescription = "Выбрать время")
            }
        },
        isError = isError,
        supportingText = supportingText,
        colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
    )
}

// Улучшенная версия форматирования времени
private fun Int.padStart(length: Int, padChar: Char): String =
    toString().padStart(length, padChar)



@Preview(showBackground = true)
@Composable
fun DatePickerDialogFieldPreview() {
    MaterialTheme {
        var date by remember { mutableStateOf("25.12.2024") }
        DatePickerDialogField(
            date = date,
            onDateSelected = { date = it },
            label = "Дата"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimePickerDialogFieldPreview() {
    MaterialTheme {
        var time by remember { mutableStateOf("12:30") }
        TimePickerDialogField(
            time = time,
            onTimeSelected = { time = it },
            label = "Время"
        )
    }
}