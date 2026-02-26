package by.riewe.cadence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.riewe.cadence.data.dao.PeriodDao
import by.riewe.cadence.data.dao.TrailerCouplingsDao
import by.riewe.cadence.data.entity.TrailerCouplings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrailerCouplingsViewModel(
    private val trailerCouplingsDao: TrailerCouplingsDao,
    private val periodDao: PeriodDao
) : ViewModel() {

    // Список перецепов для периода
    private val _couplings = MutableStateFlow<List<TrailerCouplings>>(emptyList())
    val couplings: StateFlow<List<TrailerCouplings>> = _couplings.asStateFlow()

    // Общее количество перецепов
    private val _totalCouplings = MutableStateFlow(0)
    val totalCouplings: StateFlow<Int> = _totalCouplings.asStateFlow()

    // Общая наработка моточасов
    private val _totalEngineHours = MutableStateFlow(0)
    val totalEngineHours: StateFlow<Int> = _totalEngineHours.asStateFlow()

    // Открытые перецепы (не закрытые)
    private val _openCouplings = MutableStateFlow<List<TrailerCouplings>>(emptyList())
    val openCouplings: StateFlow<List<TrailerCouplings>> = _openCouplings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Загрузить перецепы для периода
     */
    fun loadCouplings(periodId: Long) {
        viewModelScope.launch {
            trailerCouplingsDao.getByPeriodId(periodId).collect { list ->
                _couplings.value = list
                calculateTotals(list)
            }
        }
    }

    /**
     * Добавить перецеп в текущий активный период каденции
     * Период определяется автоматически
     */
    fun addCouplingToCurrentPeriod(
        cadenzzaId: Long,
        number: Int,
        fromTruck: String,
        trailerNumber: String,
        startDate: Long,
        startEH: Int,
        startFuel: Int,
        startCountry: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Автоматически получаем текущий период
                val currentPeriod = periodDao.getCurrentPeriod(cadenzzaId)
                    ?: throw IllegalStateException("Нет активного периода. Создайте или откройте каденцию.")

                val newCoupling = TrailerCouplings(
                    periodId = currentPeriod.id,
                    number = number,
                    fromTruck = fromTruck,
                    trailerNumber = trailerNumber,
                    startDate = startDate,
                    endDate = null,
                    startEH = startEH,
                    endEH = null,
                    totalEH = null,
                    startFuel = startFuel,
                    endFuel = null,
                    startCountry = startCountry,
                    endCountry = null
                )

                trailerCouplingsDao.insert(newCoupling)

            } catch (e: Exception) {
                _error.value = "Ошибка добавления перецепа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Добавить перецеп в конкретный период (прямое указание)
     */
    fun addCoupling(coupling: TrailerCouplings) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                trailerCouplingsDao.insert(coupling)
            } catch (e: Exception) {
                _error.value = "Ошибка добавления перецепа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Закрыть перецеп (сдача прицепа)
     */
    fun closeCoupling(
        coupling: TrailerCouplings,
        endDate: Long,
        endEH: Int,
        endFuel: Int,
        endCountry: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val updatedCoupling = coupling.copy(
                    endDate = endDate,
                    endEH = endEH,
                    endFuel = endFuel,
                    endCountry = endCountry,
                    totalEH = endEH - coupling.startEH
                )

                trailerCouplingsDao.update(updatedCoupling)

            } catch (e: Exception) {
                _error.value = "Ошибка закрытия перецепа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Обновить перецеп полностью
     */
    fun updateCoupling(coupling: TrailerCouplings) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                trailerCouplingsDao.update(coupling)
            } catch (e: Exception) {
                _error.value = "Ошибка обновления перецепа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Удалить перецеп
     */
    fun deleteCoupling(coupling: TrailerCouplings) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                trailerCouplingsDao.delete(coupling)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления перецепа: ${e.message}"
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
     * Подсчитать итоги по списку перецепов
     */
    private fun calculateTotals(list: List<TrailerCouplings>) {
        _totalCouplings.value = list.size

        val totalEH = list.sumOf {
            it.totalEH ?: 0
        }
        _totalEngineHours.value = totalEH

        // Открытые перецепы (без даты окончания)
        _openCouplings.value = list.filter { it.endDate == null }
    }
}