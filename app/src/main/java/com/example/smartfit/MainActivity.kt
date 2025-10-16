package com.example.smartfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.smartfit.data.datastore.UserPreferences
import com.example.smartfit.data.local.SmartFitDatabase
import com.example.smartfit.data.remote.NetworkModule
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.ui.navigation.NavGraph
import com.example.smartfit.ui.theme.SmartFitTheme
import com.example.smartfit.viewmodel.ActivityViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ActivityViewModel by lazy {
        val database = SmartFitDatabase.getDatabase(applicationContext)
        val activityDao = database.activityDao()
        val apiService = NetworkModule.apiService
        val wgerApiService = NetworkModule.wgerApiService
        val repository = ActivityRepository(activityDao, apiService, wgerApiService)
        val userPreferences = UserPreferences(applicationContext)

        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
                        return ActivityViewModel(repository, userPreferences) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${'$'}modelClass")
                }
            }
        )[ActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPreferences by viewModel.userPreferencesState.collectAsState()

            SmartFitTheme(darkTheme = userPreferences.darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
