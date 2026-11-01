package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainNavigationItem(
  val route: String,
  val title: String,
  val icon: ImageVector,
  val testTag: String
) {
  HOME("home", "Home", Icons.Default.Dashboard, "nav_home"),
  MONITOR("monitor", "Monitor", Icons.Default.Speed, "nav_monitor"),
  APPS("apps", "Apps", Icons.Default.Apps, "nav_apps"),
  PERMISSIONS("permissions", "Permissões", Icons.Default.Security, "nav_permissions"),
  TERMINAL("terminal", "Terminal", Icons.Default.Code, "nav_terminal")
}
