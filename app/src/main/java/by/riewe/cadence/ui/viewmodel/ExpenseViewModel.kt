package by.riewe.cadence.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.riewe.cadence.data.dao.ExpenseDao
import by.riewe.cadence.data.dao.PeriodDao
import by.riewe.cadence.data.entity.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val expenseDao: ExpenseDao,
    private val periodDao: PeriodDao
) : ViewModel() {

    // Список расходов для периода
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Общая сумма расходов
    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()

    // Расходы по валютам
    private val _totalsByCurrency = MutableStateFlow<Map<String, Double>>(emptyMap())
    val totalsByCurrency: StateFlow<Map<String, Double>> = _totalsByCurrency.asStateFlow()

    // Расходы по картам
    private val _totalsByCard = MutableStateFlow<Map<String, Double>>(emptyMap())
    val totalsByCard: StateFlow<Map<String, Double>> = _totalsByCard.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Загрузить расходы для периода
     */
    fun loadExpenses(periodId: Long) {
        viewModelScope.launch {
            expenseDao.getByPeriodId(periodId).collect { list ->
                _expenses.value = list
                calculateTotals(list)
            }
        }
    }

    /**
     * Добавить расход в текущий активный период каденции
     * Период определяется автоматически
     */
    fun addExpenseToCurrentPeriod(
        cadenzzaId: Long,
        expenseNumber: Int,
        date: Long,
        description: String,
        amount: Double,
        currency: String,
        country: String,
        cardName: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Автоматически получаем текущий период
                val currentPeriod = periodDao.getCurrentPeriod(cadenzzaId)
                    ?: throw IllegalStateException("Нет активного периода. Создайте или откройте каденцию.")

                val newExpense = Expense(
                    periodId = currentPeriod.id,
                    expenseNumber = expenseNumber,
                    date = date,
                    description = description,
                    amount = amount,
                    currency = currency,
                    country = country,
                    cardName = cardName
                )

                expenseDao.insert(newExpense)

            } catch (e: Exception) {
                _error.value = "Ошибка добавления расхода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Добавить расход в конкретный период (прямое указание)
     */
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                expenseDao.insert(expense)
            } catch (e: Exception) {
                _error.value = "Ошибка добавления расхода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Обновить расход
     */
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                expenseDao.update(expense)
            } catch (e: Exception) {
                _error.value = "Ошибка обновления расхода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Удалить расход
     */
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                expenseDao.delete(expense)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления расхода: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Получить сумму по конкретной карте
     */
    fun loadTotalByCard(periodId: Long, cardName: String) {
        viewModelScope.launch {
            val total = expenseDao.getTotalByCard(periodId, cardName) ?: 0.0
            _totalAmount.value = total
        }
    }

    /**
     * Получить общую сумму за период
     */
    fun loadTotalForPeriod(periodId: Long) {
        viewModelScope.launch {
            val total = expenseDao.getTotalForPeriod(periodId) ?: 0.0
            _totalAmount.value = total
        }
    }

    /**
     * Очистить ошибку
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Подсчитать итоги по списку расходов
     */
    private fun calculateTotals(list: List<Expense>) {
        // Общая сумма
        val total = list.sumOf { it.amount }
        _totalAmount.value = total

        // Группировка по валютам
        val byCurrency = list.groupBy { it.currency }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        _totalsByCurrency.value = byCurrency

        // Группировка по картам
        val byCard = list.groupBy { it.cardName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        _totalsByCard.value = byCard
    }
}