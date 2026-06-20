package com.mxt.anitrend.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mxt.anitrend.data.auth.AuthRepository
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authRepository: AuthRepository,
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val isAuthenticated by authRepository.observeAuthState().collectAsStateWithLifecycle(initialValue = false)
    var skipped by remember { mutableStateOf(false) }

    LaunchedEffect(isAuthenticated, skipped) {
        delay(600L)
        if (skipped) return@LaunchedEffect
        if (isAuthenticated) onNavigateToMain() else onNavigateToLogin()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "AniTrend",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            skipped = true
            onNavigateToMain()
        }) {
            Text("Skip — Browse anime")
        }
    }
}
