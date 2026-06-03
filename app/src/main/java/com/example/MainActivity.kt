package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.MasjidDatabase
import com.example.data.TransactionRepository
import com.example.ui.DashboardScreen
import com.example.ui.TransactionViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database, DAO and Repository
        val database = MasjidDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = TransactionRepository(database.transactionDao())
        
        // Instantiate the ViewModel securely using custom Factory
        val viewModel = ViewModelProvider(
            this, 
            TransactionViewModel.Factory(repository)
        )[TransactionViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
