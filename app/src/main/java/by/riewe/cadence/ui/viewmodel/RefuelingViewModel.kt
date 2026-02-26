package by.riewe.cadence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.riewe.cadence.data.dao.PeriodDao
import by.riewe.cadence.data.dao.RefuelingDao
import by.riewe.cadence.data.entity.Refueling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RefuelingViewModel(
    private val refuelingDao: RefuelingDao,
    private val periodDao: PeriodDao
) : ViewModel() {

    // Список заправок для периода
    private val _refuelings = MutableStateFlow<List<Refueling>>(emptyList())
    val refuelings: StateFlow<List<Refueling>> = _refuelings.asStateFlow()

    // Общее количество топлива (тягач + прицеп)
    private val _totalFuel = MutableStateFlow(0)
    val totalFuel: StateFlow<Int> = _totalFuel.asStateFlow()

    // Общее количество AdBlue
    private val _totalAdBlue = MutableStateFlow(0)
    val totalAdBlue: StateFlow<Int> = _totalAdBlue.asStateFlow()

    // Общая сумма по картам
    private val _totalByCard = MutableStateFlow<Map<String, Int>>(emptyMap())
    val totalByCard: StateFlow<Map<String, Int>> = _totalByCard.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Загрузить заправки для конкретного периода
     */
    fun loadRefuelings(periodId: Long) {
        viewModelScope.launch {
            refuelingDao.getByPeriodId(periodId).collect { list ->
                _refuelings.value = list
                calculateTotals(list)
            }
        }
    }

    /**
     * Добавить заправку в текущий активный период каденции
     * Период определяется автоматически
     */
    fun addRefuelingToCurrentPeriod(
        cadenzzaId: Long,
        refuelingNumber: Int,
        date: Long,
        truckFuel: Int?,
        trailerFuel: Int?,
        adBlue: Int?,
        country: String?,
        cardName: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Автоматически получаем текущий период
                val currentPeriod = periodDao.getCurrentPeriod(cadenzzaId)
                    ?: throw IllegalStateException("Нет активного периода. Создайте или откройте каденцию.")

                val newRefueling = Refueling(
                    periodId = currentPeriod.id,
                    refuelingNumber = refuelingNumber,
                    date = date,
                    truckFuel = truckFuel,
                    trailerFuel = trailerFuel,
                    adBlue = adBlue,
                    country = country,
                    cardName = cardName
                )

                refuelingDao.insert(newRefueling)

            } catch (e: Exception) {
                _error.value = "Ошибка добавления заправки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Добавить заправку в конкретный период (прямое указание)
     */
    fun addRefueling(refueling: Refueling) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                refuelingDao.insert(refueling)
            } catch (e: Exception) {
                _error.value = "Ошибка добавления заправки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Обновить заправку
     */
    fun updateRefueling(refueling: Refueling) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                refuelingDao.update(refueling)
            } catch (e: Exception) {
                _error.value = "Ошибка обновления заправки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Удалить заправку
     */
    fun deleteRefueling(refueling: Refueling) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                refuelingDao.delete(refueling)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления заправки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Получить общее количество топлива из базы
     */
    fun loadTotalFuel(periodId: Long) {
        viewModelScope.launch {
            val total = refuelingDao.getTotalFuelForPeriod(periodId) ?: 0
            _totalFuel.value = total
        }
    }

    /**
     * Очистить ошибку
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Подсчитать итоги по списку заправок
     */
    private fun calculateTotals(list: List<Refueling>) {
        val fuel = list.sumOf {
            (it.truckFuel ?: 0) + (it.trailerFuel ?: 0)
        }
        val adBlue = list.sumOf {
            it.adBlue ?: 0
        }

        // Группировка по картам
        val byCard = list.groupBy { it.cardName }
            .mapValues { entry ->
                entry.value.sumOf { (it.truckFuel ?: 0) + (it.trailerFuel ?: 0) }
            }

        _totalFuel.value = fuel
        _totalAdBlue.value = adBlue
        _totalByCard.value = byCard
    }
}