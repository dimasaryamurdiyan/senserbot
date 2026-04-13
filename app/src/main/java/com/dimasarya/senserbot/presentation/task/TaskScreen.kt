package com.dimasarya.senserbot.presentation.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dimasarya.senserbot.core.websocket.ConnectionState
import com.dimasarya.senserbot.domain.model.Task
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(uiState.error!!)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Shared Task List") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ConnectionBanner(state = uiState.connectionState)

            TaskInputRow(
                text = uiState.inputText,
                onTextChange = viewModel::onInputChanged,
                onAdd = viewModel::onAddTask,
                enabled = uiState.connectionState == ConnectionState.Connected
            )

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.tasks.isEmpty()) {
                    EmptyContent(modifier = Modifier.align(Alignment.Center))
                } else {
                    TaskList(
                        tasks = uiState.tasks,
                        onToggle = viewModel::onToggleTask,
                        onRemove = viewModel::onRemoveTask
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionBanner(state: ConnectionState) {
    val (text, color) = when (state) {
        ConnectionState.Connected -> "Connected" to MaterialTheme.colorScheme.primaryContainer
        ConnectionState.Connecting -> "Connecting..." to MaterialTheme.colorScheme.secondaryContainer
        ConnectionState.Reconnecting -> "Reconnecting..." to MaterialTheme.colorScheme.tertiaryContainer
        ConnectionState.Disconnected -> "Disconnected" to MaterialTheme.colorScheme.errorContainer
        is ConnectionState.Error -> "Error: ${state.message}" to MaterialTheme.colorScheme.errorContainer
    }
    val textColor = when (state) {
        ConnectionState.Connected -> MaterialTheme.colorScheme.onPrimaryContainer
        ConnectionState.Disconnected, is ConnectionState.Error -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(color = color, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state == ConnectionState.Connecting || state == ConnectionState.Reconnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 4.dp),
                    strokeWidth = 2.dp,
                    color = textColor
                )
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = textColor)
        }
    }
}

@Composable
private fun TaskInputRow(
    text: String,
    onTextChange: (String) -> Unit,
    onAdd: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Add a task...") },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAdd() })
        )
        FilledTonalButton(onClick = onAdd, enabled = enabled && text.isNotBlank()) {
            Text("Add")
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskCard(task = task, onToggle = onToggle, onRemove = onRemove)
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task.id) }
            )
            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else Color.Unspecified
            )
            IconButton(onClick = { onRemove(task.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Text(
        text = "No tasks yet. Add one above!",
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
