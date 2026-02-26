// by/riewe/cadence/ui/components/CardText.kt
package by.riewe.cadence.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.theme.NumbersFont
import by.riewe.cadence.ui.theme.odometerLarge
import by.riewe.cadence.ui.theme.odometerMedium

@Composable
fun CardText(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    useMonospaceForNumbers: Boolean = true // Автоматически определять числа
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ярлык
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            //modifier = Modifier.weight(1f)
        )

        // Точки по центру
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .padding(horizontal = 8.dp)
                .background(Color.Transparent)
                .drawBehind {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(2f, 4f), // длина точки, расстояние
                            0f
                        )
                    )
                }
        )

        // Значение с моноширинными числами
        val annotatedValue = if (useMonospaceForNumbers) {
            buildAnnotatedStringWithNumbers(value)
        } else {
            AnnotatedString(value)
        }

        Text(
            text = annotatedValue,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Создает AnnotatedString где все цифры (и пробелы между ними)
 * отображаются моноширинным шрифтом
 */
private fun buildAnnotatedStringWithNumbers(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val char = text[i]

            if (char.isDigit()) {
                // Начало числа — ищем конец числа (включая пробелы-разделители внутри)
                val numberStart = i
                while (i < text.length && (text[i].isDigit() || text[i].isWhitespace())) {
                    i++
                }
                val numberPart = text.substring(numberStart, i)

                // Применяем моноширинный шрифт к числу
                pushStyle(
                    SpanStyle(
                        fontFamily = NumbersFont,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                append(numberPart)
                pop()
            } else {
                // Обычный символ
                append(char)
                i++
            }
        }
    }
}

// Перегрузка для числовых значений с единицами измерения
@Composable
fun CardNumber(
    label: String,
    number: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
    numberStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.odometerMedium
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Точки
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(horizontal = 4.dp)
        ) {
            val dotRadius = 1.dp.toPx()
            val spacing = 4.dp.toPx()
            var x = 0f

            while (x < size.width) {
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    radius = dotRadius,
                    center = Offset(x, size.height / 2)
                )
                x += spacing
            }
        }

        // Число моноширинным
        Text(
            text = number,
            style = numberStyle,
            color = MaterialTheme.colorScheme.primary
        )

        // Единица измерения
        if (!unit.isNullOrBlank()) {
            Text(
                text = " $unit",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

// === PREVIEWS ===

@Preview(showBackground = true)
@Composable
fun CardTextPreview() {
    CadenceTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CardText("Одометр начало:", "78 256 км")
            CardText("Пробег:", "24 978 км")
            CardText("Топливо:", "450 л")
            CardText("Водитель 1:", "VIKTAR RYVE") // Без monospace
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardNumberPreview() {
    CadenceTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CardNumber("Одометр начало:", "78 256", "км")
            CardNumber("Пробег:", "24 978", "км", numberStyle = MaterialTheme.typography.odometerLarge)
        }
    }
}