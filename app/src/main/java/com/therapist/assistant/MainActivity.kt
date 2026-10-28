package com.therapist.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.therapist.assistant.ui.navigation.AppNavGraph
import com.therapist.assistant.ui.theme.TherapistAssistantTheme
import com.therapist.assistant.viewmodel.AppointmentViewModel
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val sharedText = extractSharedText(intent)

        setContent {
            val viewModel: AppointmentViewModel = viewModel()
            val themeId by viewModel.themeId.collectAsState()
            val darkMode by viewModel.darkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val useDark = when (darkMode) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }
            var pendingShare by remember { mutableStateOf(sharedText) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(15_000)
                    viewModel.shouldAutoLockNow()
                    viewModel.refreshOngoingNotification()
                }
            }

            TherapistAssistantTheme(themeId = themeId, darkTheme = useDark) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        AppNavGraph(
                            navController = navController,
                            viewModel = viewModel,
                            sharedBackupText = pendingShare,
                            onSharedBackupConsumed = { pendingShare = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true || intent.type == "application/json") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)
                } else null
            }
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    try {
                        contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    } catch (_: Exception) { null }
                }
            }
            else -> null
        }
    }
}
