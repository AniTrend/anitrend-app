package com.mxt.anitrend.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: (() -> Unit)? = null,
    onNavigateToProfile: (() -> Unit)? = null,
    onNavigateToAbout: (() -> Unit)? = null,
    onNavigateToLogs: (() -> Unit)? = null,
) {
    var notificationsEnabled by remember { mutableStateOf(false) }
    var pushNotificationsEnabled by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf("System") }
    var notificationFrequency by remember { mutableStateOf("1h") }
    var syncInterval by remember { mutableStateOf("6h") }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var syncExpanded by remember { mutableStateOf(false) }

    val themes = listOf("System", "Light", "Dark", "AMOLED")
    val frequencies = listOf("Never", "1h", "6h", "24h")
    val intervals = listOf("1h", "6h", "24h", "Manual")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        SectionHeader("Account")
        if (onNavigateToProfile != null) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onNavigateToProfile).padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Profile", style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider()
        }

        if (onLogout != null) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Logout", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            }
            HorizontalDivider()
        }

        Spacer(Modifier.height(16.dp))

        SectionHeader("Display")
        themes.forEach { theme ->
            Row(
                Modifier.fillMaxWidth().clickable { selectedTheme = theme }.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(theme, style = MaterialTheme.typography.bodyLarge)
                RadioButton(
                    selected = selectedTheme == theme,
                    onClick = { selectedTheme = theme },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionHeader("Notifications")
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Airing notifications", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
        }
        HorizontalDivider()

        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Push notifications", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = pushNotificationsEnabled, onCheckedChange = { pushNotificationsEnabled = it })
        }
        HorizontalDivider()

        ExposedDropdownMenuBox(
            expanded = frequencyExpanded,
            onExpandedChange = { frequencyExpanded = it },
        ) {
            OutlinedTextField(
                value = notificationFrequency,
                onValueChange = {},
                readOnly = true,
                label = { Text("Notification frequency") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = frequencyExpanded,
                onDismissRequest = { frequencyExpanded = false },
            ) {
                frequencies.forEach { freq ->
                    DropdownMenuItem(
                        text = { Text(freq) },
                        onClick = { notificationFrequency = freq; frequencyExpanded = false },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        Spacer(Modifier.height(16.dp))

        SectionHeader("Data")
        ExposedDropdownMenuBox(
            expanded = syncExpanded,
            onExpandedChange = { syncExpanded = it },
        ) {
            OutlinedTextField(
                value = syncInterval,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sync interval") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = syncExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = syncExpanded,
                onDismissRequest = { syncExpanded = false },
            ) {
                intervals.forEach { interval ->
                    DropdownMenuItem(
                        text = { Text(interval) },
                        onClick = { syncInterval = interval; syncExpanded = false },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth().clickable { }.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Clear cache", style = MaterialTheme.typography.bodyLarge)
        }
        HorizontalDivider()

        Row(
            Modifier.fillMaxWidth().clickable { }.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Export data", style = MaterialTheme.typography.bodyLarge)
        }
        HorizontalDivider()

        Spacer(Modifier.height(16.dp))

        SectionHeader("About")
        Row(
            Modifier.fillMaxWidth().clickable { }.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Check for updates", style = MaterialTheme.typography.bodyLarge)
                Text("v1.11.11 - look for new GitHub releases", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider()

        if (onNavigateToLogs != null) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onNavigateToLogs).padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("View Logs", style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider()
        }

        if (onNavigateToAbout != null) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onNavigateToAbout).padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("About", style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider()
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(4.dp))
}
