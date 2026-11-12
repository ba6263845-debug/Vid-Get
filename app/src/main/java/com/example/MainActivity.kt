package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.AppViewModel
import com.example.presentation.browser.BrowserScreen
import com.example.presentation.components.DisclaimerDialog
import com.example.presentation.components.FormatSelectorBottomSheet
import com.example.presentation.discover.DiscoverScreen
import com.example.presentation.downloads.DownloadsScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.me.MeScreen
import com.example.presentation.onboarding.OnboardingScreen
import com.example.presentation.player.PlayerScreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.VidGetTheme

enum class NavItem(
    val titleKey: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
) {
    HOME("home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    DISCOVER("discover", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_discover"),
    BROWSER("browser", Icons.Filled.Language, Icons.Outlined.Language, "nav_browser"),
    DOWNLOADS("downloads", Icons.Filled.Download, Icons.Outlined.Download, "nav_downloads"),
    ME("me", Icons.Filled.Person, Icons.Outlined.Person, "nav_me")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val isDarkTheme by appViewModel.isDarkTheme.collectAsState()

            VidGetTheme(darkTheme = isDarkTheme) {
                VidGetApp(viewModel = appViewModel)
            }
        }
    }
}

@Composable
fun VidGetApp(viewModel: AppViewModel) {
    var currentNav by remember { mutableStateOf(NavItem.HOME) }
    var showOnboarding by remember { mutableStateOf(!viewModel.isDisclaimerAccepted.value) }

    val isDisclaimerAccepted by viewModel.isDisclaimerAccepted.collectAsState()
    val activePlayback by viewModel.currentPlayback.collectAsState()
    val selectedMediaForDownload by viewModel.selectedMediaForDownload.collectAsState()
    val browserTargetUrl by viewModel.browserTargetUrl.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val downloadingCount = allTasks.count { it.status == "DOWNLOADING" }

    // Navigation back press handler
    BackHandler(enabled = activePlayback != null || currentNav != NavItem.HOME) {
        if (activePlayback != null) {
            viewModel.stopPlayback()
        } else if (currentNav != NavItem.HOME) {
            currentNav = NavItem.HOME
        }
    }

    if (showOnboarding && !isDisclaimerAccepted) {
        OnboardingScreen(
            onFinish = {
                showOnboarding = false
            }
        )
    } else if (!isDisclaimerAccepted) {
        DisclaimerDialog(
            onAccept = {
                viewModel.acceptDisclaimer()
            },
            title = viewModel.string("legal_disclaimer"),
            message = viewModel.string("disclaimer_text"),
            acceptButtonText = viewModel.string("i_agree")
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (activePlayback == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavItem.values().forEach { item ->
                        val isSelected = currentNav == item
                        val label = viewModel.string(item.titleKey)

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentNav = item },
                            icon = {
                                if (item == NavItem.DOWNLOADS && downloadingCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = AccentOrange,
                                                contentColor = androidx.compose.ui.graphics.Color.White
                                            ) {
                                                Text("$downloadingCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = label
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = label
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DeepNavy,
                                selectedTextColor = TealPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = TealPrimary
                            ),
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNav) {
                NavItem.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToBrowser = { url ->
                        viewModel.navigateToBrowser(url)
                        currentNav = NavItem.BROWSER
                    },
                    onPlayMedia = { item ->
                        viewModel.playMedia(item)
                    }
                )
                NavItem.DISCOVER -> DiscoverScreen(
                    viewModel = viewModel,
                    onPlayMedia = { item ->
                        viewModel.playMedia(item)
                    }
                )
                NavItem.BROWSER -> BrowserScreen(
                    viewModel = viewModel,
                    initialUrl = browserTargetUrl,
                    onBackToHome = {
                        viewModel.clearBrowserTarget()
                        currentNav = NavItem.HOME
                    }
                )
                NavItem.DOWNLOADS -> DownloadsScreen(
                    viewModel = viewModel,
                    onPlayTask = { task ->
                        viewModel.playLocalTask(task)
                    }
                )
                NavItem.ME -> MeScreen(viewModel = viewModel)
            }

            // Fullscreen Player Overlay
            activePlayback?.let { playback ->
                PlayerScreen(
                    playback = playback,
                    onClose = { viewModel.stopPlayback() }
                )
            }

            // Format Selector Bottom Sheet
            selectedMediaForDownload?.let { mediaItem ->
                FormatSelectorBottomSheet(
                    mediaItem = mediaItem,
                    onDismiss = { viewModel.closeDownloadSheet() },
                    onDownload = { option, customTitle, isPrivate ->
                        viewModel.triggerDownload(mediaItem, option, customTitle, isPrivate)
                    },
                    onPlayOfficial = {
                        viewModel.playMedia(it)
                    }
                )
            }
        }
    }
}
