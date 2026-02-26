package by.riewe.cadence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.riewe.cadence.data.dao.PeriodDao
import by.riewe.cadence.data.dao.RouteDao
import by.riewe.cadence.data.entity.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteViewModel(
    private val routeDao: RouteDao,
    private val periodDao: PeriodDao
) : ViewModel() {

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _totalMileage = MutableStateFlow(0)
    val totalMileage: StateFlow<Int> = _totalMileage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Загрузить рейсы для конкретного периода
     */
    fun loadRoutes(periodId: Long) {
        viewModelScope.launch {
            routeDao.getByPeriodId(periodId).collect { list ->
                _routes.value = list
                _totalMileage.value = list.sumOf { it.routeMileage }
            }
        }
    }

    /**
     * Добавить рейс в текущий активный период каденции
     * Период определяется автоматически
     */
    fun addRouteToCurrentPeriod(
        cadenzzaId: Long,
        routeNumber: Int,
        startDate: Long,
        startOdometer: Int,
        endDate: Long?,
        endOdometer: Int,
        departureCountry: String,
        arrivalCountry: String,
        cargoName: String,
        cmrNumber: String,
        cargoTemperature: String,
        cargoWeight: Int,
        cargoMode: String,
        trailerNumber: String,
        startEH: Int,
        endEH: Int
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Автоматически получаем текущий период
                val currentPeriod = periodDao.getCurrentPeriod(cadenzzaId)
                    ?: throw IllegalStateException("Нет активного периода. Создайте или откройте каденцию.")

                val mileage = if (endOdometer >= startOdometer) {
                    endOdometer - startOdometer
                } else {
                    throw IllegalArgumentException("Конечный одометр меньше начального")
                }

                val newRoute = Route(
                    periodId = currentPeriod.id,
                    routeNumber = routeNumber,
                    startDate = startDate,
                    startOdometer = startOdometer,
                    endDate = endDate,
                    endOdometer = endOdometer,
                    startCountry = departureCountry,
                    finishCountry = arrivalCountry,
                    cargoName = cargoName,
                    cmrNumber = cmrNumber,
                    cargoTemperature = cargoTemperature,
                    cargoWeight = cargoWeight,
                    cargoMode = cargoMode,
                    trailerNumber = trailerNumber,
                    startEH = startEH,
                    endEH = endEH,
                    totalEH = endEH - startEH,
                    routeMileage = mileage
                )

                routeDao.insertRoute(newRoute)

            } catch (e: Exception) {
                _error.value = "Ошибка добавления рейса: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Добавить рейс в конкретный период (прямое указание periodId)
     */
    fun addRoute(
        periodId: Long,
        routeNumber: Int,
        startDate: Long,
        startOdometer: Int,
        endDate: Long?,
        endOdometer: Int,
        departureCountry: String,
        arrivalCountry: String,
        cargoName: String,
        cmrNumber: String,
        cargoTemperature: String,
        cargoWeight: Int,
        cargoMode: String,
        trailerNumber: String,
        startEH: Int,
        endEH: Int
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val mileage = if (endOdometer >= startOdometer) {
                    endOdometer - startOdometer
                } else 0

                val newRoute = Route(
                    periodId = periodId,
                    routeNumber = routeNumber,
                    startDate = startDate,
                    startOdometer = startOdometer,
                    endDate = endDate,
                    endOdometer = endOdometer,
                    startCountry = departureCountry,
                    finishCountry = arrivalCountry,
                    cargoName = cargoName,
                    cmrNumber = cmrNumber,
                    cargoTemperature = cargoTemperature,
                    cargoWeight = cargoWeight,
                    cargoMode = cargoMode,
                    trailerNumber = trailerNumber,
                    startEH = startEH,
                    endEH = endEH,
                    totalEH = endEH - startEH,
                    routeMileage = mileage
                )

                routeDao.insertRoute(newRoute)

            } catch (e: Exception) {
                _error.value = "Ошибка добавления рейса: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Обновить рейс
     */
    fun updateRoute(route: Route) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                routeDao.updateRoute(route)
            } catch (e: Exception) {
                _error.value = "Ошибка обновления рейса: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Удалить рейс
     */
    fun deleteRoute(route: Route) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                routeDao.deleteRoute(route)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления рейса: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Получить общий пробег из базы
     */
    fun loadTotalMileage(periodId: Long) {
        viewModelScope.launch {
            val total = routeDao.getTotalMileageForPeriod(periodId) ?: 0
            _totalMileage.value = total
        }
    }

    /**
     * Очистить ошибку
     */
    fun clearError() {
        _error.value = null
    }
}