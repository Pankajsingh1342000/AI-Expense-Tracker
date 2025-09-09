package com.example.aiexpensetracker.presentation.screens.chat.presentation.component

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aiexpensetracker.ai.speech.VoiceRecognitionService
import com.example.aiexpensetracker.presentation.screens.chat.presentation.viewmodel.AssistantViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val voiceInputText by viewModel.voiceInputText.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create voice service that will be cleaned up properly
    val voiceService = remember { VoiceRecognitionService(context) }

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Cleanup voice service when composable is disposed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (isListening) {
                        voiceService.stopListening()
                        viewModel.setListening(false)
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    voiceService.stopListening()
                    viewModel.setListening(false)
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceService.stopListening()
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Show error messages as Toast
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // Check microphone permission
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (voiceService.isRecognitionAvailable()) {
                startVoiceRecognition(voiceService, viewModel)
            } else {
                viewModel.setError("Speech recognition not available on this device")
            }
        } else {
            viewModel.setError("Microphone permission required for voice input")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Assistant")
                        if (isListening) {
                            Text(
                                "Listening...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Stop voice recognition before navigating back
                        if (isListening) {
                            voiceService.stopListening()
                            viewModel.setListening(false)
                        }
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { message ->
                    ChatMessageBubble(
                        message = message,
                        modifier = Modifier.animateItem()
                    )
                }

                // Show typing indicator when processing
                if (isProcessing) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Voice Input Display
            if (voiceInputText.isNotEmpty()) {
                VoiceInputDisplay(
                    text = voiceInputText,
                    onClear = { viewModel.clearVoiceInput() }
                )
            }

            // Input Area
            ChatInputArea(
                textInput = textInput,
                onTextChange = { textInput = it },
                onSendMessage = {
                    if (textInput.isNotBlank()) {
                        viewModel.processTextInput(textInput)
                        textInput = ""
                    }
                },
                onVoiceInput = {
                    handleVoiceInput(
                        context = context,
                        voiceService = voiceService,
                        viewModel = viewModel,
                        isListening = isListening,
                        permissionLauncher = microphonePermissionLauncher
                    )
                },
                isListening = isListening,
                isEnabled = !isProcessing
            )
        }
    }
}

private fun handleVoiceInput(
    context: Context,
    voiceService: VoiceRecognitionService,
    viewModel: AssistantViewModel,
    isListening: Boolean,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
) {
    if (isListening) {
        // Stop listening
        voiceService.stopListening()
        viewModel.setListening(false)
        return
    }

    // Check permission first
    when (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)) {
        PackageManager.PERMISSION_GRANTED -> {
            Timber.d("Permission granted, checking speech recognition availability")
            if (voiceService.isRecognitionAvailable()) {
                startVoiceRecognition(voiceService, viewModel)
            } else {
                viewModel.setError("Speech recognition not available. Please install Google app from Play Store.")
            }
        }
        else -> {
            Timber.d("Requesting microphone permission")
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

private fun startVoiceRecognition(
    voiceService: VoiceRecognitionService,
    viewModel: AssistantViewModel
) {
    Timber.d("Starting voice recognition...")
    voiceService.startListening(
        onResult = { result ->
            Timber.d("Voice recognition result: '$result'")
            viewModel.processVoiceInput(result)
        },
        onError = { error ->
            Timber.e("Voice recognition error: $error")
            viewModel.setListening(false)
            viewModel.setError(error)
        },
        onListeningStateChange = { listening ->
            Timber.d("Listening state changed: $listening")
            viewModel.setListening(listening)
        }
    )
}