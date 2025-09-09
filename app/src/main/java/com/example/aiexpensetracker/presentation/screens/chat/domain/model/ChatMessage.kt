package com.example.aiexpensetracker.presentation.screens.chat.domain.model

import com.example.aiexpensetracker.domain.model.expense.Expense
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val messageType: MessageType = MessageType.TEXT,
    val expenseData: Expense? = null
)