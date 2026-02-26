package by.riewe.cadence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.riewe.cadence.data.dao.PeriodDao
import by.riewe.cadence.data.db.PeriodWithData
import by.riewe.cadence.data.entity.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PeriodViewModel(
    private val periodDao: PeriodDao
) : ViewModel() {

    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods.asStateFlow()

    private val _currentPeriod = MutableStateFlow<Period?>(null)
    val currentPeriod: StateFlow<Period?> = _currentPeriod.asStateFlow()

    private val _selectedPeriodWithData = MutableStateFlow<PeriodWithData?>(null)
    val selectedPeriodWithData: StateFlow<PeriodWithData?> = _selectedPeriodWithData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Получить текущий активный период (где endDate = null)
     */
    suspend fun getCurrentPeriod(cadenzzaId: Long): Period? {
        return periodDao.getCurrentPeriod(cadenzzaId)
    }

    /**
     * Получить номер текущего периода для создания записей
     */
    suspend fun getCurrentPeriodNumber(cadenzzaId: Long): Int {
        val current = getCurrentPeriod(cadenzzaId)
        return current?.periodNumber ?: 1
    }

    fun loadPeriods(cadenzzaId: Long) {
        viewModelScope.launch {
            periodDao.getByCadenzzaId(cadenzzaId).collect { list ->
                _periods.value = list.sortedBy { it.periodNumber }
                _currentPeriod.value = list.find { it.endDate == null }
            }
        }
    }

    /**
     * Создать первый период (вызывается автоматически при создании каденции)
     */
    suspend fun createFirstPeriod(cadenzzaId: Long): Long {
        val period = Period(
            cadenzzaId = cadenzzaId,
            periodNumber = 1,
            startDate = System.currentTimeMillis()
        )
        return periodDao.insertPeriod(period)
    }

    /**
     * Закрыть текущий период и автоматически создать новый
     * Все новые записи будут автоматически попадать в новый период
     */
    fun closeCurrentPeriodAndCreateNew(
        cadenzzaId: Long,
        endDate: Long,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val current = periodDao.getCurrentPeriod(cadenzzaId)
                    ?: throw IllegalStateException("Нет активного периода")

                // Закрываем текущий
                val closedPeriod = current.copy(
                    endDate = endDate,
                    notes = notes ?: current.notes
                )
                periodDao.updatePeriod(closedPeriod)

                // Создаем новый период автоматически
                val nextNumber = (periodDao.getMaxPeriodNumber(cadenzzaId) ?: 0) + 1
                val newPeriod = Period(
                    cadenzzaId = cadenzzaId,
                    periodNumber = nextNumber,
                    startDate = endDate // Новый период начинается с даты закрытия предыдущего
                )
                periodDao.insertPeriod(newPeriod)

                // Обновляем список
                loadPeriods(cadenzzaId)

            } catch (e: Exception) {
                _error.value = "Ошибка закрытия периода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Просто закрыть период без создания нового (если каденция закрывается)
     */
    fun closePeriod(
        periodId: Long,
        endDate: Long,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val period = periodDao.getById(periodId) ?: return@launch

                val updated = period.copy(
                    endDate = endDate,
                    notes = notes ?: period.notes
                )
                periodDao.updatePeriod(updated)

                loadPeriods(period.cadenzzaId)
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPeriodDetails(periodId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val details = periodDao.getPeriodWithData(periodId)
                _selectedPeriodWithData.value = details
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPeriodDetailsByNumber(cadenzzaId: Long, periodNumber: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val details = periodDao.getPeriodWithDataByNumber(cadenzzaId, periodNumber)
                _selectedPeriodWithData.value = details
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSelectedPeriod() {
        _selectedPeriodWithData.value = null
    }
}