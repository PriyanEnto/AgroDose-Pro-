package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.AppDatabase
import com.example.data.CalculationRepository
import com.example.ui.screens.MainUi
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to Edge capability enablement
        enableEdgeToEdge()
        
        // Database & Repository manual construction
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CalculationRepository(database.calculationDao())
        
        // ViewModel manual instantiation linked to activity lifecycle
        val viewModel = MainViewModel(repository)

        setContent {
            MyApplicationTheme {
                MainUi(viewModel = viewModel)
            }
        }
    }
}
