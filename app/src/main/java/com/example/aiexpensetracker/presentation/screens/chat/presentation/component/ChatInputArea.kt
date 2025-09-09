package com.example.aiexpensetracker.presentation.screens.chat.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.aiexpensetracker.R

@Composable
fun ChatInputArea(
    textInput: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onVoiceInput: () -> Unit,
    isListening: Boolean,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        if (isListening) "Listening..."
                        else "Type a message or use voice..."
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = isEnabled && !isListening,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (textInput.isNotBlank()) onSendMessage() }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Voice Input Button
            FloatingActionButton(
                onClick = onVoiceInput,
                modifier = Modifier.size(48.dp),
                containerColor = if (isListening)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    if (isListening) painterResource(R.drawable.ic_stop) else painterResource(R.drawable.ic_mic),
                    contentDescription = if (isListening) "Stop" else "Voice input",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}