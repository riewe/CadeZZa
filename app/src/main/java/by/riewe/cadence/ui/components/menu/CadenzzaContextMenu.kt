package by.riewe.cadence.ui.components.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import by.riewe.cadence.ui.theme.CadenceTheme

/**
 * Действия для контекстного меню каденции
 */
sealed class CadenzzaAction(val label: String) {
    data object StartNewPeriod : CadenzzaAction("Начать новый период")
    data object CloseCadenzza : CadenzzaAction("Закрыть каденцию")
    data object EditCadenzza : CadenzzaAction("Редактировать")
    data object DeleteCadenzza : CadenzzaAction("Удалить")

}

/**
 * Контекстное меню для каденции
 */
@Composable
fun CadenzzaContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onAction: (CadenzzaAction) -> Unit,
    isCadenzzaClosed: Boolean = false
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 200.dp),
        properties = PopupProperties(focusable = true)
    ) {
        // Действия для открытой каденции
        if (!isCadenzzaClosed) {
            DropdownMenuItem(
                text = { Text(CadenzzaAction.StartNewPeriod.label) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    onAction(CadenzzaAction.StartNewPeriod)
                    onDismiss()
                }
            )

            DropdownMenuItem(
                text = { Text(CadenzzaAction.CloseCadenzza.label) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    onAction(CadenzzaAction.CloseCadenzza)
                    onDismiss()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
        }

        DropdownMenuItem(
            text = { Text(CadenzzaAction.EditCadenzza.label) },
            leadingIcon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null
                )
            },
            onClick = {
                onAction(CadenzzaAction.EditCadenzza)
                onDismiss()
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Опасное действие в конце
        DropdownMenuItem(
            text = {
                Text(
                    CadenzzaAction.DeleteCadenzza.label,
                    color = MaterialTheme.colorScheme.error
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            onClick = {
                onAction(CadenzzaAction.DeleteCadenzza)
                onDismiss()
            }
        )
    }
}

@Preview(showBackground = true, name = "Cadenzza Context Menu Open")
@Composable
fun CadenzzaContextMenuPreview() {
    CadenceTheme {
        Box {
            CadenzzaContextMenu(
                expanded = true,
                onDismiss = {},
                onAction = {},
                isCadenzzaClosed = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Cadenzza Context Menu Closed")
@Composable
fun CadenzzaContextMenuClosedPreview() {
    CadenceTheme {
        Box {
            CadenzzaContextMenu(
                expanded = true,
                onDismiss = {},
                onAction = {},
                isCadenzzaClosed = true
            )
        }
    }
}