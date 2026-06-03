package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.TransactionEntity
import com.example.ui.theme.Emerald40
import com.example.ui.theme.Emerald80
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val totals by viewModel.totalsState.collectAsStateWithLifecycle()
    val activeCategories by viewModel.activeCategories.collectAsStateWithLifecycle()
    
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val showDialog by viewModel.showDialog.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Masjid Al-Barakah Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Masjid Al-Barakah",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Keuangan & Kas Lazna",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = !showMenu },
                        modifier = Modifier.testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu Lainnya"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mulai Ulang Data (Reset)") },
                            onClick = {
                                viewModel.clearAllData()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.testTag("clear_data_button")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Tambah") },
                text = { Text(stringResource(R.string.btn_add_transaction)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_transaction_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header Banner: Total Saldo, Pemasukan & Pengeluaran
                item {
                    BalanceBannerCard(
                        saldo = totals.saldo,
                        pemasukan = totals.pemasukan,
                        pengeluaran = totals.pengeluaran
                    )
                }

                // Search Bar and Tab Selector Group sticky or sectioned
                item {
                    SearchBarAndFilterSection(
                        searchQuery = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) },
                        selectedTab = selectedTab,
                        onTabSelect = { viewModel.onTabSelect(it) },
                        categories = activeCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.onCategoryFilterSelect(it) }
                    )
                }

                // Transactions List
                if (transactions.isEmpty()) {
                    item {
                        EmptyStateView()
                    }
                } else {
                    items(
                        items = transactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionItemCard(
                            transaction = transaction,
                            onEditClick = { viewModel.openEditDialog(transaction) },
                            onDeleteClick = { viewModel.deleteTransaction(transaction) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    // Add & Edit Dialog
    if (showDialog) {
        AddEditTransactionDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeDialog() }
        )
    }
}

@Composable
fun BalanceBannerCard(
    saldo: Double,
    pemasukan: Double,
    pengeluaran: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.label_current_balance),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatRupiah(saldo),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("saldo_total_text")
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Incoming section
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Kas Masuk",
                                tint = Color(0xFF62D2A2),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kas Masuk",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRupiah(pemasukan),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8CE3BA)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Outgoing section
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Kas Keluar",
                                tint = Color(0xFFFF8585),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kas Keluar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRupiah(pengeluaran),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD56B)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBarAndFilterSection(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedTab: String,
    onTabSelect: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Search bar input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_input"),
            placeholder = { Text(stringResource(R.string.hint_search)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // M3 Navigation Tabs for Type (All, Income, Expense)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Semua", "Pemasukan", "Pengeluaran")
            tabs.forEach { tabName ->
                val isSelected = selectedTab == tabName
                val testTagSuffix = tabName.lowercase()
                Button(
                    onClick = { onTabSelect(tabName) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = CardDefaults.outlinedCardBorder(enabled = !isSelected),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("filter_tab_$testTagSuffix")
                ) {
                    Text(
                        text = tabName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        if (categories.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = { Text(text = category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded }
            .testTag("transaction_item_${transaction.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle category icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.isIncome) {
                                Color(0xFF62D2A2).copy(alpha = 0.15f)
                            } else {
                                Color(0xFFFF8585).copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.isIncome) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (transaction.isIncome) "Kas Masuk" else "Kas Keluar",
                        tint = if (transaction.isIncome) Emerald40 else Color(0xFFCE3A3A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Mid detail: Description, Category, Date
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = transaction.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        Text(
                            text = formatDate(transaction.dateMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Amount
                Text(
                    text = "${if (transaction.isIncome) "+" else "-"} ${formatRupiah(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.isIncome) Emerald40 else Color(0xFFCE3A3A),
                    modifier = Modifier.testTag("transaction_amount_${transaction.id}")
                )
            }

            // Expanded Actions edit/delete
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onEditClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("edit_button_${transaction.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ubah")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFCE3A3A)),
                        modifier = Modifier.testTag("delete_button_${transaction.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_state_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.empty_state_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
    viewModel: TransactionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val isEdit = viewModel.editingTransaction.collectAsStateWithLifecycle().value != null
    val desc by viewModel.inputDescription.collectAsStateWithLifecycle()
    val amount by viewModel.inputAmount.collectAsStateWithLifecycle()
    val isIncome by viewModel.inputIsIncome.collectAsStateWithLifecycle()
    val category by viewModel.inputCategory.collectAsStateWithLifecycle()
    val dateMillis by viewModel.inputDateMillis.collectAsStateWithLifecycle()

    val descError by viewModel.descriptionError.collectAsStateWithLifecycle()
    val amountError by viewModel.amountError.collectAsStateWithLifecycle()

    val categories = listOf(
        "Kotak Amal Jumaat",
        "Zakat Kas",
        "Donasi Renovasi",
        "Operasional AC/Air",
        "Kajian Sosial",
        "Uang Kas Remaja Syiar",
        "Sarana Prasarana",
        "Infaq Anak Yatim",
        "Lainnya"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_edit_dialog_container")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .animateContentSize()
            ) {
                Text(
                    text = if (isEdit) "Ubah Data Transaksi" else stringResource(R.string.title_add_dialog),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Pemasukan / Pengeluaran (Masuk / Keluar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val incomingBtnColor = if (isIncome) Emerald40 else MaterialTheme.colorScheme.surface
                    val incomingTextColor = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    val outgoingBtnColor = if (!isIncome) Color(0xFFCE3A3A) else MaterialTheme.colorScheme.surface
                    val outgoingTextColor = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                    Button(
                        onClick = { viewModel.inputIsIncome.value = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_income_switch"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = incomingBtnColor,
                            contentColor = incomingTextColor
                        ),
                        border = if (!isIncome) CardDefaults.outlinedCardBorder() else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Masuk", fontSize = 14.sp)
                    }

                    Button(
                        onClick = { viewModel.inputIsIncome.value = false },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_expense_switch"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = outgoingBtnColor,
                            contentColor = outgoingTextColor
                        ),
                        border = if (isIncome) CardDefaults.outlinedCardBorder() else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keluar", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Description
                OutlinedTextField(
                    value = desc,
                    onValueChange = { viewModel.inputDescription.value = it },
                    label = { Text(stringResource(R.string.label_description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_description_input"),
                    isError = descError != null,
                    supportingText = descError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.inputAmount.value = it },
                    label = { Text(stringResource(R.string.label_amount)) },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_amount_input"),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker trigger button
                Text(
                    text = stringResource(R.string.label_date),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                viewModel.inputDateMillis.value = selectedCal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_date_picker_trigger"),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDate(dateMillis),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pilih Tanggal Calendar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Chips Selector
                Text(
                    text = stringResource(R.string.label_category),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = 3
                ) {
                    categories.forEach { catName ->
                        val isCatSelected = category == catName
                        SuggestionChip(
                            onClick = { viewModel.inputCategory.value = catName },
                            label = { Text(catName, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isCatSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                labelColor = if (isCatSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (!isCatSelected) SuggestionChipDefaults.suggestionChipBorder(enabled = true) else null,
                            modifier = Modifier.testTag("dialog_category_chip_$catName")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action CTA buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_button")
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.saveTransaction() },
                        modifier = Modifier.testTag("dialog_save_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}

// Utility formats Indonesian Rupiah
fun formatRupiah(amount: Double): String {
    val formatter = DecimalFormat.getCurrencyInstance(Locale.forLanguageTag("id"))
    val formatted = formatter.format(amount)
    // Simplify Rp text representation
    return formatted.replace(",00", "").replace("Rp", "Rp ")
}

// Utility dates formatter
fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id"))
    return sdf.format(Date(millis))
}
