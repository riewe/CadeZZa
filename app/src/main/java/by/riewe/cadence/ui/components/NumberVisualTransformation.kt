// by/riewe/cadence/ui/components/NumberVisualTransformation.kt
package by.riewe.cadence.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class NumberVisualTransformation(
    private val locale: Locale = Locale.getDefault()
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text

        // Пустая строка — ничего не делаем
        if (input.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Форматируем число с пробелами
        val number = input.toLongOrNull() ?: return TransformedText(text, OffsetMapping.Identity)
        val formatter = NumberFormat.getInstance(locale)
        formatter.isGroupingUsed = true
        val formatted = formatter.format(number)

        // Создаем маппинг позиций курсора
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var count = 0
                var digitsPassed = 0
                var transformedOffset = 0

                for (char in formatted) {
                    if (digitsPassed >= offset) break
                    if (char.isDigit()) {
                        digitsPassed++
                    }
                    transformedOffset++
                }

                return transformedOffset.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var digitsCount = 0
                var originalOffset = 0

                for (i in 0 until offset.coerceAtMost(formatted.length)) {
                    if (formatted[i].isDigit()) {
                        digitsCount++
                    }
                    originalOffset = digitsCount
                }

                return originalOffset.coerceIn(0, input.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}