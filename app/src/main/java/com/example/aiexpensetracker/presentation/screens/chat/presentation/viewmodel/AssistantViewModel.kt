package com.example.aiexpensetracker.presentation.screens.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiexpensetracker.domain.model.ai_processing_state.AIProcessingResult
import com.example.aiexpensetracker.domain.repository.CategoryRepository
import com.example.aiexpensetracker.ai.ExpenseAIAssistant
import com.example.aiexpensetracker.presentation.screens.chat.domain.model.ChatMessage
import com.example.aiexpensetracker.presentation.screens.chat.domain.model.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val expenseAIAssistant: ExpenseAIAssistant,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _voiceInputText = MutableStateFlow("")
    val voiceInputText: StateFlow<String> = _voiceInputText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        addWelcomeMessage()
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel cleanup - voice service cleanup handled in Composable
    }

    fun processTextInput(input: String) {
        if (input.isBlank()) return

        // Clear any error messages
        _errorMessage.value = null

        // Add user message
        addMessage(
            ChatMessage(
                content = input,
                isFromUser = true,
                timestamp = System.currentTimeMillis()
            )
        )

        processWithAI(input)
    }

    fun processVoiceInput(voiceText: String) {
        _voiceInputText.value = voiceText
        processTextInput(voiceText)
    }

    fun clearVoiceInput() {
        _voiceInputText.value = ""
    }

    fun setListening(listening: Boolean) {
        _isListening.value = listening
    }

    fun setError(error: String?) {
        _errorMessage.value = error
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun addWelcomeMessage() {
        addMessage(
            ChatMessage(
                content = "Hi! I'm your AI expense assistant. You can:\n\n" +
                        "💰 Add expenses: \"I bought coffee for 50 rupees\"\n" +
                        "📂 Add categories: \"Add fitness to the category\"\n" +
                        "📊 Ask questions: \"How much did I spend this month?\"\n\n" +
                        "How can I help you today?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun processWithAI(input: String) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                val result = expenseAIAssistant.processInput(input)

                val responseMessage = when (result) {
                    is AIProcessingResult.ExpenseAdded -> {
                        val expense = result.expense
                        ChatMessage(
                            content = "✅ Expense Added Successfully!\n\n" +
                                    "💰 Amount: ₹${String.format("%.2f", expense.amount)}\n" +
                                    "📝 Description: ${expense.description}\n" +
                                    "📂 Category: ${expense.category}\n" +
                                    "📅 Date: ${formatDate(expense.date)}",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis(),
                            messageType = MessageType.EXPENSE_ADDED,
                            expenseData = expense
                        )
                    }

                    is AIProcessingResult.CategoryAdded -> {
                        ChatMessage(
                            content = "📂 Category \"${result.categoryName}\" added successfully!\n\n" +
                                    "You can now use this category for your expenses.",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis(),
                            messageType = MessageType.CATEGORY_ADDED
                        )
                    }

                    is AIProcessingResult.QueryAnswer -> {
                        ChatMessage(
                            content = result.answer,
                            isFromUser = false,
                            timestamp = System.currentTimeMillis(),
                            messageType = MessageType.QUERY_RESPONSE,
                            expenseData = result.data.firstOrNull()
                        )
                    }

                    is AIProcessingResult.Error -> {
                        ChatMessage(
                            content = "❌ ${result.message}",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis(),
                            messageType = MessageType.ERROR
                        )
                    }

                    is AIProcessingResult.InvalidInput -> {
                        ChatMessage(
                            content = "🤔 I couldn't understand that. Could you please rephrase?\n\n" +
                                    "Try saying something like:\n" +
                                    "• \"I bought lunch for 150 rupees\"\n" +
                                    "• \"How much did I spend on food?\"\n" +
                                    "• \"Add gym to the category\"",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis(),
                            messageType = MessageType.HELP
                        )
                    }
                }

                addMessage(responseMessage)

            } catch (e: Exception) {
                Timber.e(e, "Error processing AI input")
                addMessage(
                    ChatMessage(
                        content = "Sorry, something went wrong. Please try again.",
                        isFromUser = false,
                        timestamp = System.currentTimeMillis(),
                        messageType = MessageType.ERROR
                    )
                )
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _chatMessages.value = _chatMessages.value + message
    }

    private fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}