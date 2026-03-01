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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.riewe.cadence.data.entity.Route
import by.riewe.cadence.data.entity.RouteStatus
import by.riewe.cadence.ui.theme.CadenceTheme
import by.riewe.cadence.ui.theme.StatusActive
import by.riewe.cadence.ui.theme.StatusError
import by.riewe.cadence.ui.theme.StatusWarning
import by.riewe.cadence.ui.viewmodel.RouteViewModel
import by.riewe.cadence.utils.formatDate
import by.riewe.cadence.utils.formatNumberWithSpaces

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    periodId: Long,
    viewModel: RouteViewModel,
    onNavigateBack: () -> Unit,
    onCreateRoute: () -> Unit,
    onCompleteRoute: (routeId: Long) -> Unit,
    onEditRoute: (routeId: Long) -> Unit
) {
    val routes by viewModel.routes.collectAsStateWithLifecycle(initialValue = emptyList())
    val draftRoutes by viewModel.draftRoutes.collectAsStateWithLifecycle(initialValue = emptyList())
    val completedRoutes by viewModel.completedRoutes.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var routeToDelete by remember { mutableStateOf<Route?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(periodId) {
        viewModel.loadRoutes(periodId)
    }

    // Диалог удаления
    if (showDeleteDialog && routeToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                routeToDelete = null
            },
            title = { Text("Удалить рейс?") },
            text = {
                Text("Рейс №${routeToDelete?.routeNumber} будет удалён безвозвратно.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRoute(routeToDelete!!.id)
                        showDeleteDialog = false
                        routeToDelete = null
                    }
                ) {
                    Text("Удалить", color = StatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    routeToDelete = null
                }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог ошибки
    error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Ошибка") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Рейсы")
                        Text(
                            text = "Период #$periodId",
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
        },
        floatingActionButton = {
            BadgedBox(
                badge = {
                    if (draftRoutes.isNotEmpty()) {
                        Badge(
                            containerColor = StatusWarning
                        ) {
                            Text(draftRoutes.size.toString())
                        }
                    }
                }
            ) {
                FloatingActionButton(onClick = onCreateRoute) {
                    Icon(Icons.Default.Add, contentDescription = "Новый рейс")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading && routes.isEmpty()) {
                // Первичная загрузка
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (routes.isEmpty()) {
                // Пустое состояние
                EmptyRouteState(onCreateClick = onCreateRoute)
            } else {
                // Список рейсов
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Секция: Черновики (требуют внимания!)
                    if (draftRoutes.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Черновики",
                                count = draftRoutes.size,
                                color = StatusWarning,
                                description = "Требуют дополнения"
                            )
                        }
                        items(
                            items = draftRoutes,
                            key = { it.id }
                        ) { route ->
                            RouteCard(
                                route = route,
                                onClick = { onCompleteRoute(route.id) },
                                onDelete = {
                                    routeToDelete = route
                                    showDeleteDialog = true
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    // Секция: Завершённые рейсы
                    if (completedRoutes.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Завершённые",
                                count = completedRoutes.size,
                                color = StatusActive,
                                description = "Готовы к просмотру"
                            )
                        }
                        items(
                            items = completedRoutes,
                            key = { it.id }
                        ) { route ->
                            RouteCard(
                                route = route,
                                onClick = { onEditRoute(route.id) },
                                onDelete = {
                                    routeToDelete = route
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    // Нижний отступ
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    color: Color,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RouteCard(
    route: Route,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDraft = route.status == RouteStatus.DRAFT

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDraft) {
                StatusWarning.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isDraft) {
            BorderStroke(
                width = 1.dp,
                color = StatusWarning.copy(alpha = 0.3f)
            )
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDraft) 2.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Верхняя строка: номер и статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isDraft) StatusWarning else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Рейс №${route.routeNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Бейдж статуса
                StatusBadge(status = route.status)

                // Кнопка удаления
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = StatusError.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Маршрут
            RouteInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Загрузка:",
                value = "${route.startCountry} • ${formatDate(route.startDate)}"
            )

            if (isDraft) {
                // Черновик: показываем что выгрузка не заполнена
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = StatusWarning
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Выгрузка: ожидается...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusWarning
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(нажмите для дополнения)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Показываем что известно о грузе
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Груз: ${route.cargoName} • ${route.cargoWeight} кг",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // Завершённый рейс: полная информация
                RouteInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Выгрузка:",
                    value = "${route.finishCountry} • ${formatDate(route.endDate!!)}",
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Пробег и моточасы
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip(
                        label = "Пробег",
                        value = "${formatNumberWithSpaces(route.routeMileage.toString())} км"
                    )
                    InfoChip(
                        label = "Моточасы",
                        value = "${route.totalEH} ч"
                    )
                    InfoChip(
                        label = "Груз",
                        value = "${route.cargoWeight} кг"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: RouteStatus) {
    val (text, color) = when (status) {
        RouteStatus.DRAFT -> "Черновик" to StatusWarning
        RouteStatus.COMPLETED -> "Завершён" to StatusActive
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RouteInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyRouteState(
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocalShipping,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Нет рейсов",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Нажмите + чтобы создать первый рейс",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onCreateClick) {
            Text("Создать рейс")
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
fun RouteListScreenPreview() {
    val sampleRoutes = listOf(
        Route(
            id = 1,
            periodId = 1,
            routeNumber = 1,
            startDate = System.currentTimeMillis(),
            startOdometer = 100000,
            startCountry = "DE",
            cargoName = "Электроника",
            cargoWeight = 15000,
            cmrNumber = "CMR123456",
            cargoTemperature = "+15",
            cargoMode = "Навалом",
            trailerNumber = "AB 1234",
            startEH = 1000,
            endDate = System.currentTimeMillis() + 86400000,
            endOdometer = 100450,
            finishCountry = "PL",
            endEH = 1010,
            totalEH = 10,
            routeMileage = 450,
            status = RouteStatus.COMPLETED
        ),
        Route(
            id = 2,
            periodId = 1,
            routeNumber = 2,
            startDate = System.currentTimeMillis(),
            startOdometer = 100450,
            startCountry = "PL",
            cargoName = "Мебель",
            cargoWeight = 8000,
            cmrNumber = "CMR789012",
            cargoTemperature = "Обычная",
            cargoMode = "Паллеты",
            trailerNumber = "AB 1234",
            startEH = 1010,
            endDate = null,
            endOdometer = null,
            finishCountry = null,
            endEH = null,
            totalEH = null,
            routeMileage = 0,
            status = RouteStatus.DRAFT
        )
    )

    CadenceTheme {
        RouteListContent(
            routes = sampleRoutes,
            draftRoutes = sampleRoutes.filter { it.status == RouteStatus.DRAFT },
            completedRoutes = sampleRoutes.filter { it.status == RouteStatus.COMPLETED },
            isLoading = false,
            onNavigateBack = {},
            onCreateRoute = {},
            onCompleteRoute = {},
            onEditRoute = {},
            onDeleteRoute = {}
        )
    }
}

// Отдельный composable для Preview без ViewModel
@Composable
private fun RouteListContent(
    routes: List<Route>,
    draftRoutes: List<Route>,
    completedRoutes: List<Route>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onCreateRoute: () -> Unit,
    onCompleteRoute: (Long) -> Unit,
    onEditRoute: (Long) -> Unit,
    onDeleteRoute: (Route) -> Unit
) {
    // Упрощённая версия для Preview...
}