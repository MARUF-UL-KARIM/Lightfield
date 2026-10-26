package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainEditorScreen
import com.example.ui.screens.MinimalHomeScreen
import com.example.ui.theme.LightfieldTheme
import com.example.ui.theme.StudioDarkBackground
import com.example.viewmodel.EditorViewModel

enum class AppScreen {
    HOME,
    EDITOR
}

class MainActivity : ComponentActivity() {

    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LightfieldTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

                // Photo picker to import/edit a new photo
                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        editorViewModel.importPhotoFromUri(it)
                        currentScreen = AppScreen.EDITOR
                    }
                }

                // DNG preset picker to apply Adobe Lightroom DNG settings onto current photo
                val dngPresetPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        editorViewModel.applyDngPresetDirectly(it)
                        currentScreen = AppScreen.EDITOR
                    }
                }

                BackHandler(enabled = currentScreen == AppScreen.EDITOR) {
                    currentScreen = AppScreen.HOME
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioDarkBackground
                ) {
                    when (currentScreen) {
                        AppScreen.HOME -> {
                            MinimalHomeScreen(
                                viewModel = editorViewModel,
                                onOpenPhotoEditor = { currentScreen = AppScreen.EDITOR },
                                onPickImageToEdit = { photoPickerLauncher.launch("image/*") },
                                onPickDngPreset = { dngPresetPickerLauncher.launch("*/*") }
                            )
                        }
                        AppScreen.EDITOR -> {
                            MainEditorScreen(
                                viewModel = editorViewModel,
                                onBackToHome = { currentScreen = AppScreen.HOME },
                                onImportClick = { photoPickerLauncher.launch("image/*") },
                                onPickDngToApply = { dngPresetPickerLauncher.launch("*/*") }
                            )
                        }
                    }
                }
            }
        }
    }
}
