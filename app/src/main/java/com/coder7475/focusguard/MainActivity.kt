package com.coder7475.focusguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.coder7475.focusguard.data.preferences.FocusPreferences
import com.coder7475.focusguard.data.preferences.focusDataStore
import com.coder7475.focusguard.ui.home.HomeScreen
import com.coder7475.focusguard.ui.onboarding.OnboardingScreen
import com.coder7475.focusguard.ui.theme.FocusGuardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FocusGuardTheme {
                val preferences = remember { FocusPreferences(applicationContext.focusDataStore) }
                val isOnboardingCompleted by preferences.isOnboardingCompleted.collectAsState(initial = false)

                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            onOnboardingComplete = {
                                // Handled reactively via StateFlow
                            },
                            modifier = Modifier.padding(paddingValues)
                        )
                    } else {
                        HomeScreen(
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
