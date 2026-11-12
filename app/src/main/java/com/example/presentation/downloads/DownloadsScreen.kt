package com.example.presentation.downloads

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.entity.DownloadTaskEntity
import com.example.presentation.AppViewModel
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun DownloadsScreen(
    viewModel: AppViewModel,
    onPlayTask: (DownloadTaskEntity) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        viewModel.string("downloading"),
        viewModel.string("completed"),
        viewModel.string("music"),
        viewModel.string("videos"),
        viewModel.string("private_vault")
    )

    val allTasks by viewModel.allTasks.collectAsState()
    val privateTasks by viewModel.privateTasks.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()

    var taskToRename by remember { mutableStateOf<DownloadTaskEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Filter tasks based on current tab
    val currentDisplayTasks = when (selectedTab) {
        0 -> allTasks.filter { it.status == "DOWNLOADING" || it.status == "PAUSED" }
        1 -> allTasks.filter { it.status == "COMPLETED" }
        2 -> allTasks.filter { it.status == "COMPLETED" && it.isAudioOnly }
        3 -> allTasks.filter { it.status == "COMPLETED" && !it.isAudioOnly }
        4 -> if (isVaultUnlocked) privateTasks else emptyList()
        else -> allTasks
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("downloads_screen")
    ) {
        // Storage Bar Header
        StorageMeterCard(context = context)

        // Sub-tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = TealPrimary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area
        if (selectedTab == 4 && !isVaultUnlocked) {
            // Private Vault Lock Screen
            VaultUnlockCard(
                pinInput = pinInput,
                onPinChange = {
                    pinInput = it
                    pinError = false
                },
                isError = pinError,
                onUnlock = {
                    val success = viewModel.unlockVault(pinInput)
                    if (!success) {
                        pinError = true
                    } else {
                        pinInput = ""
                    }
                }
            )
        } else if (currentDisplayTasks.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedTab) {
                                2 -> Icons.Default.Audiotrack
                                3 -> Icons.Default.Videocam
                                4 -> Icons.Default.Lock
                                else -> Icons.Default.Storage
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTab == 0) "No Active Downloads" else "No Files in this Folder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Browse or paste links to download media",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentDisplayTasks, key = { it.id }) { task ->
                    DownloadTaskCard(
                        task = task,
                        onPlay = { onPlayTask(task) },
                        onPause = { viewModel.downloadRepository.pauseDownload(task.id) },
                        onResume = { viewModel.downloadRepository.resumeDownload(task.id) },
                        onCancel = { viewModel.downloadRepository.cancelOrDeleteDownload(task.id) },
                        onRename = {
                            taskToRename = task
                            renameInput = task.title
                        },
                        onShare = {
                            val file = File(task.localFilePath)
                            if (file.exists()) {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = task.mimeType
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                                } catch (e: Exception) {
                                    // Fallback text share
                                    val textIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, task.url)
                                    }
                                    context.startActivity(Intent.createChooser(textIntent, "Share URL"))
                                }
                            }
                        },
                        onTogglePrivacy = { isPriv ->
                            coroutineScope.launch {
                                viewModel.downloadRepository.setPrivate(task.id, isPriv)
                            }
                        }
                    )
                }
            }
        }
    }

    // Rename Dialog
    if (taskToRename != null) {
        AlertDialog(
            onDismissRequest = { taskToRename = null },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New file name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val task = taskToRename
                        if (task != null && renameInput.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.downloadRepository.renameDownload(task.id, renameInput)
                                taskToRename = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StorageMeterCard(context: Context) {
    var freeGb = 0f
    var totalGb = 0f
    try {
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
        val bytesTotal = stat.blockCountLong * stat.blockSizeLong
        freeGb = bytesAvailable / (1024f * 1024f * 1024f)
        totalGb = bytesTotal / (1024f * 1024f * 1024f)
    } catch (e: Exception) {
        freeGb = 24.5f
        totalGb = 64.0f
    }
    val usedGb = totalGb - freeGb
    val usedFraction = if (totalGb > 0) usedGb / totalGb else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Device Storage",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = String.format(Locale.US, "%.1f GB Free of %.1f GB", freeGb, totalGb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { usedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AccentOrange,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun DownloadTaskCard(
    task: DownloadTaskEntity,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onTogglePrivacy: (Boolean) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val isFinished = task.status == "COMPLETED"
    val isPaused = task.status == "PAUSED"
    val isDownloading = task.status == "DOWNLOADING"

    val progressFraction = if (task.totalBytes > 0) {
        task.downloadedBytes.toFloat() / task.totalBytes.toFloat()
    } else {
        0f
    }

    val speedMb = task.speedBytesPerSec / (1024f * 1024f)
    val downloadedMb = task.downloadedBytes / (1024f * 1024f)
    val totalMb = task.totalBytes / (1024f * 1024f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isFinished) { onPlay() }
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Type Icon
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (task.isAudioOnly) EmeraldSuccess.copy(alpha = 0.15f) else TealPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (task.isAudioOnly) Icons.Default.Audiotrack else Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (task.isAudioOnly) EmeraldSuccess else TealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${task.quality} • ${if (isFinished) "${totalMb.toInt()} MB" else String.format(Locale.US, "%.1f/%.1f MB", downloadedMb, totalMb)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action buttons
                if (isFinished) {
                    IconButton(onClick = onPlay) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TealPrimary)
                    }
                } else if (isDownloading) {
                    IconButton(onClick = onPause) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = AccentOrange)
                    }
                } else if (isPaused) {
                    IconButton(onClick = onResume) {
                        Icon(Icons.Default.Refresh, contentDescription = "Resume", tint = EmeraldSuccess)
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (isFinished) {
                            DropdownMenuItem(
                                text = { Text("Share") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onShare()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rename") },
                                leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onRename()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (task.isPrivate) "Remove from Vault" else "Move to Private Vault") },
                                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onTogglePrivacy(!task.isPrivate)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = ErrorRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                            onClick = {
                                menuExpanded = false
                                onCancel()
                            }
                        )
                    }
                }
            }

            // Progress bar and speed indicator when downloading or paused
            if (!isFinished) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else AccentOrange,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isPaused) "Paused" else String.format(Locale.US, "Speed: %.2f MB/s", speedMb),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else TealPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isPaused) "" else if (task.etaSeconds > 0) "ETA: ${task.etaSeconds}s" else "Connecting...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VaultUnlockCard(
    pinInput: String,
    onPinChange: (String) -> Unit,
    isError: Boolean,
    onUnlock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = AccentOrange.copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Private Vault",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Encrypted storage for sensitive media.\nDefault PIN: 1234",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 6) onPinChange(it) },
                    label = { Text("Enter 4-Digit PIN") },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("vault_pin_input")
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Incorrect PIN. Default is 1234",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onUnlock,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("vault_unlock_button")
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock Vault", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
