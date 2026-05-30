package com.mxt.anitrend.ui.medialist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val statusOptions = listOf("CURRENT", "PLANNING", "COMPLETED", "DROPPED", "PAUSED", "REPEATING")
private val statusDisplay = mapOf(
    "CURRENT" to "Current",
    "PLANNING" to "Planning",
    "COMPLETED" to "Completed",
    "DROPPED" to "Dropped",
    "PAUSED" to "Paused",
    "REPEATING" to "Repeating",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListEditScreen(
    mediaId: Long,
    mediaTitle: String,
    listEntryId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: MediaListEditViewModel = koinViewModel(parameters = { parametersOf(mediaId) }),
) {
    var selectedStatus by remember { mutableStateOf("") }
    var scoreText by remember { mutableStateOf("") }
    var progressText by remember { mutableStateOf("") }
    var progressVolumesText by remember { mutableStateOf("") }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MediaListEditEvent.Saved -> onNavigateBack()
                is MediaListEditEvent.Deleted -> onNavigateBack()
                is MediaListEditEvent.Error -> {
                    isSaving = false
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mediaTitle, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Status", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = statusDisplay[selectedStatus] ?: "Select status",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = statusDropdownExpanded,
                    onDismissRequest = { statusDropdownExpanded = false },
                ) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(statusDisplay[status] ?: status) },
                            onClick = {
                                selectedStatus = status
                                statusDropdownExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Score (0-10)", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = scoreText,
                onValueChange = { scoreText = it },
                placeholder = { Text("e.g. 8.5") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(16.dp))

            Text("Progress", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = progressText,
                onValueChange = { progressText = it },
                placeholder = { Text("Episodes or chapters") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(16.dp))

            Text("Volumes", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = progressVolumesText,
                onValueChange = { progressVolumesText = it },
                placeholder = { Text("Volumes read") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(
                    onClick = {
                        isSaving = true
                        val score = scoreText.toDoubleOrNull()
                        val progress = progressText.toIntOrNull()
                        val progressVolumes = progressVolumesText.toIntOrNull()
                        viewModel.saveEntry(
                            status = selectedStatus.ifEmpty { null },
                            score = score,
                            progress = progress,
                            progressVolumes = progressVolumes,
                        )
                    },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isSaving) "Saving..." else "Save")
                }
                if (listEntryId != null) {
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            viewModel.deleteEntry(listEntryId.toInt())
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Remove from List")
                    }
                }
            }
        }
    }
}
