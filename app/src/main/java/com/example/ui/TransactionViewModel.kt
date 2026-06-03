package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TransactionEntity
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    // Filter and search query states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow("Semua") // "Semua", "Pemasukan", "Pengeluaran"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("Semua Kategori")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    // Main source of truth from Room database
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered transactions stream based on search, tab and category
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _searchQuery,
        _selectedTab,
        _selectedCategoryFilter
    ) { transactions, query, tab, category ->
        transactions.filter { transaction ->
            // Apply tab filter
            val matchesTab = when (tab) {
                "Pemasukan" -> transaction.isIncome
                "Pengeluaran" -> !transaction.isIncome
                else -> true
            }

            // Apply category filter
            val matchesCategory = if (category == "Semua Kategori") {
                true
            } else {
                transaction.category.equals(category, ignoreCase = true)
            }

            // Apply search query
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                transaction.description.contains(query, ignoreCase = true) ||
                        transaction.category.contains(query, ignoreCase = true)
            }

            matchesTab && matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Financial summaries
    val totalsState: StateFlow<Totals> = allTransactions.map { list ->
        val totalIncome = list.filter { it.isIncome }.sumOf { it.amount }
        val totalExpense = list.filter { !it.isIncome }.sumOf { it.amount }
        Totals(
            saldo = totalIncome - totalExpense,
            pemasukan = totalIncome,
            pengeluaran = totalExpense
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Totals()
    )

    // List of unique active categories based on currently saved entries for quick filtering
    val activeCategories: StateFlow<List<String>> = allTransactions.map { list ->
        val categories = list.map { it.category }.distinct().sorted()
        listOf("Semua Kategori") + categories
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Semua Kategori")
    )

    // Dialog state
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction: StateFlow<TransactionEntity?> = _editingTransaction.asStateFlow()

    // Form inputs state
    var inputDescription = MutableStateFlow("")
    var inputAmount = MutableStateFlow("")
    var inputIsIncome = MutableStateFlow(true)
    var inputCategory = MutableStateFlow("Kotak Amal")
    var inputDateMillis = MutableStateFlow(System.currentTimeMillis())

    // Form validation errors
    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()

    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelect(tab: String) {
        _selectedTab.value = tab
    }

    fun onCategoryFilterSelect(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun openAddDialog() {
        _editingTransaction.value = null
        inputDescription.value = ""
        inputAmount.value = ""
        inputIsIncome.value = true
        inputCategory.value = "Kotak Amal"
        inputDateMillis.value = System.currentTimeMillis()
        _descriptionError.value = null
        _amountError.value = null
        _showDialog.value = true
    }

    fun openEditDialog(transaction: TransactionEntity) {
        _editingTransaction.value = transaction
        inputDescription.value = transaction.description
        inputAmount.value = if (transaction.amount % 1.0 == 0.0) {
            transaction.amount.toLong().toString()
        } else {
            transaction.amount.toString()
        }
        inputIsIncome.value = transaction.isIncome
        inputCategory.value = transaction.category
        inputDateMillis.value = transaction.dateMillis
        _descriptionError.value = null
        _amountError.value = null
        _showDialog.value = true
    }

    fun closeDialog() {
        _showDialog.value = false
        _editingTransaction.value = null
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun saveTransaction() {
        val desc = inputDescription.value.trim()
        val amtStr = inputAmount.value.trim()

        var isValid = true

        if (desc.isEmpty()) {
            _descriptionError.value = "Keterangan/Sebab harus diisi!"
            isValid = false
        } else {
            _descriptionError.value = null
        }

        val amt = amtStr.toDoubleOrNull()
        if (amt == null || amt <= 0) {
            _amountError.value = "Nominal uang harus angka valid dan lebih dari 0!"
            isValid = false
        } else {
            _amountError.value = null
        }

        if (!isValid) return

        viewModelScope.launch {
            val tx = TransactionEntity(
                id = _editingTransaction.value?.id ?: 0,
                description = desc,
                amount = amt!!,
                isIncome = inputIsIncome.value,
                category = inputCategory.value,
                dateMillis = inputDateMillis.value
            )

            if (tx.id == 0) {
                repository.insert(tx)
            } else {
                repository.update(tx)
            }
            closeDialog()
        }
    }

    companion object {
        fun Factory(repository: TransactionRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: java.lang.Class<T>): T {
                return TransactionViewModel(repository) as T
            }
        }
    }
}

data class Totals(
    val saldo: Double = 0.0,
    val pemasukan: Double = 0.0,
    val pengeluaran: Double = 0.0
)
