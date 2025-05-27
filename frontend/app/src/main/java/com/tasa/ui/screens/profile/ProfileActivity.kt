package com.tasa.ui.screens.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tasa.DependenciesContainer

class ProfileActivity : ComponentActivity() {
    private val userInfoRepository by lazy { (application as DependenciesContainer).userInfoRepository }
    private val repo by lazy { (application as DependenciesContainer).repo }

    private val viewModel by viewModels<ProfileScreenViewModel>(
        factoryProducer = {
            ProfileScreenViewModelFactory(
                userInfoRepository,
                repo,
            )
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.fetchProfile()
        setContent {
            ProfileScreen(
                viewModel = viewModel,
                onEditAction = { profile -> viewModel.setEditState(profile) },
                onNavigateBack = { finish() },
            )
        }
    }
}
