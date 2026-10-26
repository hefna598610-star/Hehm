package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.model.TriggerAction
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MagnoScanTheme
import com.example.viewmodel.MagnetViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MagnetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Observe trigger events from the ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.triggerEvent.collect { action ->
                    handleTriggerAction(action)
                }
            }
        }

        setContent {
            MagnoScanTheme {
                MainScreen(
                    viewModel = viewModel,
                    onMoveToBack = { moveTaskToBack(true) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }

    private fun handleTriggerAction(action: TriggerAction) {
        when (action) {
            TriggerAction.MOVE_TO_BACK -> {
                Toast.makeText(
                    this,
                    "تم رصد المغناطيس! إرسال التطبيق للخلفية...",
                    Toast.LENGTH_SHORT
                ).show()
                moveTaskToBack(true)
            }
            TriggerAction.FIRECRACKER_POP -> {
                // Handled in ViewModel via feedbackHelper with pop burst and shockwaves
            }
            TriggerAction.HAPTIC_ALERT -> {
                // Handled in ViewModel via feedbackHelper
            }
            TriggerAction.AUDIO_RADAR -> {
                // Handled in ViewModel via feedbackHelper
            }
            TriggerAction.VISUAL_FLASH -> {
                // Handled via Compose UI trigger state
            }
        }
    }
}
