package by.riewe.cadence.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDriverInput(input: String): String {
    return input.filter { it.isLetterOrDigit() || it.isWhitespace() }
        .uppercase()
        .filter { char ->
            char.isWhitespace() || char.isDigit() || (char.isLetter() && char in 'A'..'Z')
        }
}

fun formatTruckNumber(input: String): Pair<String, Int> {
    val cleaned = input.filter { it.isLetterOrDigit() }.uppercase()
    val letters = cleaned.take(3).filter { it.isLetter() }
    val digits = cleaned.drop(3).filter { it.isDigit() }.take(3)
    val result = if (digits.isNotEmpty()) "$letters $digits" else letters
    return Pair(result, result.length)
}



fun formatTrailerNumber(input: String): Pair<String, Int> {
    val cleaned = input.filter { it.isLetterOrDigit() }.uppercase()
    val letters = cleaned.take(2).filter { it.isLetter() }
    val digits = cleaned.drop(2).filter { it.isDigit() }.take(3) // ← было .take(4)
    val result = if (digits.isNotEmpty()) "$letters $digits" else letters
    return Pair(result, result.length)
}


/*fun formatNumberWithSpaces(input: String): Pair<String, Int> {
    val digitsOnly = input.filter { it.isDigit() }
    if (digitsOnly.length <= 3) return Pair(digitsOnly, digitsOnly.length)
    val reversed = digitsOnly.reversed()
    val spaced = reversed.chunked(3).joinToString(" ")
    val result = spaced.reversed()
    return Pair(result, result.length)
}*/

fun formatNumberWithSpaces(number: String): String {
    val digitsOnly = number.filter { it.isDigit() }
    if (digitsOnly.length <= 3) return digitsOnly
    val reversed = digitsOnly.reversed()
    val spaced = reversed.chunked(3).joinToString(" ")
    return spaced.reversed()
}

fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "-"
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatTime(minutes: Long?): String {
    if (minutes == null) return "-"
    val hours = (minutes / (60 * 60 * 1000)) % 24
    val mins = (minutes / (60 * 1000)) % 60
    return "${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}"
}