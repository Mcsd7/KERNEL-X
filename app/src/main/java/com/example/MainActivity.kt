package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.MainNavigationItem
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonitorScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.ElectricRedGlow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.AppsViewModel
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MonitorViewModel
import com.example.viewmodel.PermissionsViewModel
import com.example.viewmodel.TerminalViewModel

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        KernelXApp()
      }
    }
  }
}

@Composable
fun KernelXApp(
  mainViewModel: MainViewModel = viewModel(),
  monitorViewModel: MonitorViewModel = viewModel(),
  appsViewModel: AppsViewModel = viewModel(),
  permissionsViewModel: PermissionsViewModel = viewModel(),
  terminalViewModel: TerminalViewModel = viewModel()
) {
  val mainState by mainViewModel.uiState.collectAsState()
  val monitorState by monitorViewModel.uiState.collectAsState()
  val appsState by appsViewModel.uiState.collectAsState()
  val permissionsState by permissionsViewModel.uiState.collectAsState()
  val terminalState by terminalViewModel.uiState.collectAsState()

  // Handle system back gesture
  BackHandler(enabled = mainState.isSettingsOpen || mainState.currentNavigationItem != MainNavigationItem.HOME) {
    mainViewModel.handleBack()
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(DeepBlack),
    contentWindowInsets = WindowInsets.statusBars,
    bottomBar = {
      if (!mainState.isSettingsOpen) {
        NavigationBar(
          containerColor = DarkSurface,
          contentColor = TextPrimary,
          tonalElevation = 8.dp,
          modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar")
        ) {
          MainNavigationItem.values().forEach { item ->
            val isSelected = mainState.currentNavigationItem == item
            NavigationBarItem(
              selected = isSelected,
              onClick = { mainViewModel.navigateTo(item) },
              icon = {
                Icon(
                  imageVector = item.icon,
                  contentDescription = item.title,
                  tint = if (isSelected) ElectricRed else TextMuted
                )
              },
              label = {
                Text(
                  text = item.title.uppercase(),
                  fontFamily = FontFamily.Monospace,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 10.sp,
                  color = if (isSelected) ElectricRed else TextMuted
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricRed,
                selectedTextColor = ElectricRed,
                indicatorColor = ElectricRedGlow,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
              ),
              modifier = Modifier.testTag(item.testTag)
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(DeepBlack)
        .padding(innerPadding)
    ) {
      if (mainState.isSettingsOpen) {
        SettingsScreen(
          onBack = { mainViewModel.closeSettings() },
          termuxStatus = terminalState.termuxStatus,
          refreshInterval = monitorState.refreshIntervalMillis,
          onSetInterval = { millis -> monitorViewModel.setRefreshInterval(millis) },
          onOpenTermuxSetup = {
            mainViewModel.closeSettings()
            mainViewModel.navigateTo(MainNavigationItem.TERMINAL)
            terminalViewModel.toggleTermuxSetupDialog(true)
          }
        )
      } else {
        when (mainState.currentNavigationItem) {
          MainNavigationItem.HOME -> {
            HomeScreen(
              monitorState = monitorState,
              onRefresh = { monitorViewModel.refreshImmediately() },
              onOpenSettings = { mainViewModel.openSettings() },
              onNavigateToMonitor = { mainViewModel.navigateTo(MainNavigationItem.MONITOR) },
              onNavigateToApps = { mainViewModel.navigateTo(MainNavigationItem.APPS) },
              onNavigateToPermissions = { mainViewModel.navigateTo(MainNavigationItem.PERMISSIONS) },
              onNavigateToTerminal = { mainViewModel.navigateTo(MainNavigationItem.TERMINAL) }
            )
          }

          MainNavigationItem.MONITOR -> {
            MonitorScreen(
              monitorState = monitorState,
              onRefresh = { monitorViewModel.refreshImmediately() },
              onSetInterval = { millis -> monitorViewModel.setRefreshInterval(millis) }
            )
          }

          MainNavigationItem.APPS -> {
            AppsScreen(
              uiState = appsState,
              onSearchChange = { q -> appsViewModel.setSearchQuery(q) },
              onFilterChange = { f -> appsViewModel.setFilter(f) },
              onSelectApp = { app -> appsViewModel.selectApp(app) },
              onPromptUninstall = { app -> appsViewModel.promptUninstall(app) },
              onLaunchApp = { pkg -> appsViewModel.launchApp(pkg) },
              onOpenAppDetails = { pkg -> appsViewModel.openAppDetails(pkg) },
              onOpenBatterySettings = { appsViewModel.openBatterySettings() },
              onOpenBatteryUsageSettings = { appsViewModel.openBatteryUsageSettings() },
              onRefresh = { appsViewModel.loadApps() }
            )
          }

          MainNavigationItem.PERMISSIONS -> {
            PermissionsScreen(
              uiState = permissionsState,
              onSelectGroup = { group -> permissionsViewModel.selectGroup(group) },
              onSelectAppDetail = { detail -> permissionsViewModel.selectAppDetail(detail) },
              onSearchChange = { q -> permissionsViewModel.setSearchQuery(q) },
              onToggleOnlyGranted = { permissionsViewModel.toggleOnlyGrantedFilter() },
              onOpenAppDetailsSettings = { pkg -> permissionsViewModel.openAppDetailsSettings(pkg) },
              onRefresh = { permissionsViewModel.loadPermissions() }
            )
          }

          MainNavigationItem.TERMINAL -> {
            TerminalScreen(
              uiState = terminalState,
              onCommandChange = { input -> terminalViewModel.setCommandInput(input) },
              onExecute = { cmd -> terminalViewModel.executeCommand(cmd) },
              onClear = { terminalViewModel.clearTerminal() },
              onCopy = { terminalViewModel.copyOutput() },
              onSetEngine = { engine -> terminalViewModel.setEngine(engine) },
              onToggleTermuxDialog = { show -> terminalViewModel.toggleTermuxSetupDialog(show) },
              onDismissCopyFeedback = { terminalViewModel.dismissCopyFeedback() },
              onOpenTermuxInstall = { terminalViewModel.termuxManager.openTermuxInstallationPage() }
            )
          }
        }
      }
    }
  }
}
