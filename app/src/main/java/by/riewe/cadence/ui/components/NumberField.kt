package by.riewe.cadence.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.riewe.cadence.ui.theme.CadenceTheme

@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorMessage: String = "Введите значение",
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Разрешаем только цифры и пробелы
                onValueChange(newValue.filter { it.isDigit() || it.isWhitespace() })
            },
            label = { Text(label) },
            isError = isError,
            modifier = Modifier.fillMaxWidth()
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = leadingIcon,
            trailingIcon = if (isError) {
                {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Ошибка",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else null
        )
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NumberFieldPreview() {
    CadenceTheme {
        ValidatedTextField(
            value = "text",
            onValueChange = {},
            label = "Label",
            isError = false,
            errorMessage = ""
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NumberFieldErrorPreview() {
    CadenceTheme {
        ValidatedTextField(
            value = "text",
            onValueChange = {},
            label = "Label",
            isError = true,
            errorMessage = "Error message"
        )
    }
}