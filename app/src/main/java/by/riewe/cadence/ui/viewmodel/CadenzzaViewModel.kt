package by.riewe.cadence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.riewe.cadence.data.dao.CadenzzaDao
import by.riewe.cadence.data.dao.PeriodDao
import by.riewe.cadence.data.db.CadenzzaWithPeriods
import by.riewe.cadence.data.entity.CadenzzaEntity
import by.riewe.cadence.data.entity.Period
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CadenzzaViewModel(
    private val cadenzzaDao: CadenzzaDao,
    private val periodDao: PeriodDao
) : ViewModel() {

    val allCadenzza = cadenzzaDao.getAll()

    private val _selectedCadenzza = MutableStateFlow<CadenzzaWithPeriods?>(null)
    val selectedCadenzza: StateFlow<CadenzzaWithPeriods?> = _selectedCadenzza.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Для автоматического номера каденции
    private val _suggestedCadenzzaNumber = MutableStateFlow<String?>(null)
    val suggestedCadenzzaNumber: StateFlow<String?> = _suggestedCadenzzaNumber.asStateFlow()

    /**
     * Загрузить следующий предлагаемый номер каденции
     */
    fun loadNextCadenzzaNumber() {
        viewModelScope.launch {
            try {
                val allCadenzzas = cadenzzaDao.getAll().first()
                val nextNumber = if (allCadenzzas.isEmpty()) {
                    1
                } else {
                    val maxNumber = allCadenzzas.mapNotNull { cadenzza ->
                        cadenzza.cadenzzaNumber.filter { it.isDigit() }.toIntOrNull()
                    }.maxOrNull() ?: 0
                    maxNumber + 1
                }
                _suggestedCadenzzaNumber.value = nextNumber.toString()
            } catch (e: Exception) {
                _suggestedCadenzzaNumber.value = "1"
            }
        }
    }

    /**
     * Очистить предложенный номер
     */
    fun clearSuggestedNumber() {
        _suggestedCadenzzaNumber.value = null
    }

    /**
     * Создать новую каденцию
     */
    fun createCadenzza(
        cadenzzaNumber: String,
        driver1: String,
        driver2: String?,
        startDate: Long,
        startTime: Long,
        truckNumber: String,
        startOdometer: Int,
        startTrailerNumber: String,
        startTruckFuel: Int,
        startTrailerFuel: Int,
        startMH: Int
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val finalNumber = cadenzzaNumber.takeIf { it.isNotBlank() }
                    ?: _suggestedCadenzzaNumber.value
                    ?: "1"

                val newCadenzza = CadenzzaEntity(
                    cadenzzaNumber = finalNumber,
                    driver1 = driver1,
                    driver2 = driver2,
                    startDate = startDate,
                    startTime = startTime,
                    truckNumber = truckNumber,
                    startTrailerNumber = startTrailerNumber,
                    startOdometer = startOdometer,
                    startTruckFuel = startTruckFuel,
                    startTrailerFuel = startTrailerFuel,
                    startMH = startMH,
                    endDate = null,
                    endTime = null,
                    endTrailerNumber = null,
                    endOdometer = null,
                    endTruckFuel = null,
                    endTrailerFuel = null,
                    endMH = null,
                    totalMileage = 0,
                    totalDays = 0
                )

                val id = cadenzzaDao.insertCadenzza(newCadenzza)
                createFirstPeriod(id)

            } catch (e: Exception) {
                _error.value = "Ошибка создания каденции: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Закрыть каденцию
     */
    fun closeCadenzza(
        cadenzzaId: Long,
        endDate: Long,
        endTime: Long,
        endOdometer: Int,
        endTrailerNumber: String,
        endTruckFuel: Int,
        endTrailerFuel: Int,
        endMH: Int,
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val cadenzza = cadenzzaDao.getById(cadenzzaId)

                if (cadenzza != null) {
                    val mileage = endOdometer - cadenzza.startOdometer
                    val days = ((endDate - cadenzza.startDate) / (1000 * 60 * 60 * 24))

                    cadenzzaDao.closeCadenzza(
                        cadenzzaId = cadenzzaId,
                        endDate = endDate,
                        endTime = endTime,
                        endOdometer = endOdometer,
                        endTrailerNumber = endTrailerNumber,
                        endTruckFuel = endTruckFuel,
                        endTrailerFuel = endTrailerFuel,
                        endMH = endMH,
                        totalMileage = mileage,
                        totalDays = days.toInt()
                    )
                }
            } catch (e: Exception) {
                _error.value = "Ошибка закрытия каденции: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Загрузить детали каденции
     */
    fun loadCadenzzaDetails(cadenzzaId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val details = cadenzzaDao.getCadenzzaWithPeriods(cadenzzaId)
                _selectedCadenzza.value = details
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки деталей: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Удалить каденцию
     */
    fun deleteCadenzza(cadenzzaId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                cadenzzaDao.deleteCadenzza(cadenzzaId)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    /**
     * Очистить ошибку
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Создать первый период при создании каденции
     */
    private suspend fun createFirstPeriod(cadenzzaId: Long) {
        val firstPeriod = Period(
            cadenzzaId = cadenzzaId,
            periodNumber = 1
        )
        periodDao.insertPeriod(firstPeriod)
    }

    /**
     * Добавить новый период к каденции
     */
    fun addPeriod(cadenzzaId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val periodCount = periodDao.getPeriodCount(cadenzzaId)
                val nextPeriodNumber = periodCount + 1

                val newPeriod = Period(
                    cadenzzaId = cadenzzaId,
                    periodNumber = nextPeriodNumber
                )
                periodDao.insertPeriod(newPeriod)

            } catch (e: Exception) {
                _error.value = "Ошибка создания периода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun clearSelectedCadenzza() {
        _selectedCadenzza.value = null
    }
}