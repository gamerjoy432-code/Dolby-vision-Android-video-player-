package com.example

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.player.PlayerViewModel
import com.example.ui.screens.DisplayDiagnosticsScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    LIBRARY,
    PLAYER,
    DIAGNOSTICS
}

class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Prevent screen from sleeping while using video player
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0C10)
                ) {
                    AppNavigation(
                        viewModel = playerViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Incoming Stream"
                playerViewModel.addLocalVideo(uri.toString(), fileName)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Media3 handles audio playback and background transition
    }
}

@Composable
fun AppNavigation(
    viewModel: PlayerViewModel
) {
    var currentScreen by remember { mutableStateOf(AppScreen.LIBRARY) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.LIBRARY -> {
                LibraryScreen(
                    viewModel = viewModel,
                    onPlayMedia = {
                        currentScreen = AppScreen.PLAYER
                    },
                    onOpenDiagnostics = {
                        currentScreen = AppScreen.DIAGNOSTICS
                    }
                )
            }
            AppScreen.PLAYER -> {
                PlayerScreen(
                    viewModel = viewModel,
                    onBack = {
                        currentScreen = AppScreen.LIBRARY
                    }
                )
            }
            AppScreen.DIAGNOSTICS -> {
                DisplayDiagnosticsScreen(
                    report = viewModel.displayReport,
                    onBack = {
                        currentScreen = AppScreen.LIBRARY
                    }
                )
            }
        }
    }
}
