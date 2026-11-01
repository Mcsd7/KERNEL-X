package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.apps.AppItem
import com.example.ui.components.TechBadge
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalGreenDark
import com.example.ui.theme.TerminalYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils
import com.example.viewmodel.AppFilter
import com.example.viewmodel.AppsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
  uiState: AppsUiState,
  onSearchChange: (String) -> Unit,
  onFilterChange: (AppFilter) -> Unit,
  onSelectApp: (AppItem?) -> Unit,
  onPromptUninstall: (AppItem?) -> Unit,
  onLaunchApp: (String) -> Boolean,
  onOpenAppDetails: (String) -> Unit,
  onOpenBatterySettings: () -> Unit,
  onOpenBatteryUsageSettings: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showHibernationGuideDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DeepBlack)
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "GERENCIADOR DE APPS",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          letterSpacing = 1.sp,
          color = TextPrimary
        )
        Text(
          text = "${uiState.filteredApps.size} APLICATIVOS ENCONTRADOS",
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = ElectricRed
        )
      }

      Row {
        IconButton(
          onClick = { showHibernationGuideDialog = true },
          modifier = Modifier.testTag("btn_open_hibernation_info")
        ) {
          Icon(
            imageVector = Icons.Default.BatteryAlert,
            contentDescription = "Hibernação & Bateria",
            tint = TerminalYellow
          )
        }

        IconButton(
          onClick = onRefresh,
          modifier = Modifier.testTag("btn_apps_refresh")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Recarregar Lista",
            tint = TextSecondary
          )
        }
      }
    }

    // Search bar
    OutlinedTextField(
      value = uiState.searchQuery,
      onValueChange = onSearchChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("input_apps_search"),
      placeholder = {
        Text("Buscar por nome ou pacote...", fontSize = 13.sp, color = TextMuted)
      },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextSecondary)
      },
      trailingIcon = {
        if (uiState.searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchChange("") }) {
            Icon(Icons.Default.Close, contentDescription = "Limpar Busca", tint = TextSecondary)
          }
        }
      },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ElectricRed,
        unfocusedBorderColor = DarkSurfaceBorder,
        focusedContainerColor = DarkSurfaceCard,
        unfocusedContainerColor = DarkSurfaceCard,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      shape = RoundedCornerShape(10.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Filter Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf(
        AppFilter.ALL to "TODOS (${uiState.allApps.size})",
        AppFilter.USER to "USUÁRIO",
        AppFilter.SYSTEM to "SISTEMA"
      ).forEach { (filter, label) ->
        val isSelected = uiState.activeFilter == filter
        FilterChip(
          selected = isSelected,
          onClick = { onFilterChange(filter) },
          label = {
            Text(
              text = label,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricRed,
            selectedLabelColor = TextPrimary,
            containerColor = DarkSurfaceElevated,
            labelColor = TextSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = DarkSurfaceBorder,
            selectedBorderColor = ElectricRed
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Apps list
    if (uiState.isLoading) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = ElectricRed)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "INDEXANDO PACOTES DO ANDROID...",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      }
    } else if (uiState.filteredApps.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Nenhum aplicativo encontrado.",
          fontFamily = FontFamily.Monospace,
          fontSize = 13.sp,
          color = TextSecondary
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("list_installed_apps"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(
          items = uiState.filteredApps,
          key = { it.packageName }
        ) { app ->
          AppListItem(
            app = app,
            onClick = { onSelectApp(app) },
            onLaunch = { onLaunchApp(app.packageName) },
            onUninstall = { onPromptUninstall(app) }
          )
        }
      }
    }
  }

  // App Detail Modal Bottom Sheet
  uiState.selectedApp?.let { app ->
    ModalBottomSheet(
      onDismissRequest = { onSelectApp(null) },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      containerColor = DarkSurfaceCard,
      contentColor = TextPrimary
    ) {
      AppDetailSheetContent(
        app = app,
        onLaunch = { onLaunchApp(app.packageName) },
        onOpenSettings = { onOpenAppDetails(app.packageName) },
        onOpenBattery = onOpenBatterySettings,
        onUninstall = {
          onSelectApp(null)
          onPromptUninstall(app)
        },
        onDismiss = { onSelectApp(null) }
      )
    }
  }

  // Uninstall Confirmation Dialog
  uiState.appPendingUninstall?.let { app ->
    AlertDialog(
      onDismissRequest = { onPromptUninstall(null) },
      containerColor = DarkSurfaceCard,
      titleContentColor = TextPrimary,
      textContentColor = TextSecondary,
      title = {
        Text(
          text = "CONFIRMAR DESINSTALAÇÃO",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = ElectricRed
        )
      },
      text = {
        Column {
          Text(
            text = "Deseja solicitar a remoção do aplicativo \"${app.appName}\"?",
            fontSize = 14.sp,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Pacote: ${app.packageName}",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextMuted
          )
          Spacer(modifier = Modifier.height(12.dp))
          if (app.isSystemApp) {
            Box(
              modifier = Modifier
                .background(TerminalYellow.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(8.dp)
            ) {
              Text(
                text = "AVISO: Este é um aplicativo protegido do sistema. O instalador padrão do Android pode recusar a desinstalação.",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TerminalYellow
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val intent = (context.applicationContext as? android.app.Application)?.let {
              com.example.data.apps.AppManagerRepository(it).createUninstallIntent(app.packageName)
            } ?: com.example.data.apps.AppManagerRepository(context).createUninstallIntent(app.packageName)
            try {
              context.startActivity(intent)
            } catch (e: Exception) {
              // ignore
            }
            onPromptUninstall(null)
          },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricRed),
          modifier = Modifier.testTag("btn_confirm_uninstall")
        ) {
          Text("DESINSTALAR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { onPromptUninstall(null) }) {
          Text("CANCELAR", color = TextSecondary)
        }
      }
    )
  }

  // Hibernation & Background Guidance Dialog
  if (showHibernationGuideDialog) {
    AlertDialog(
      onDismissRequest = { showHibernationGuideDialog = false },
      containerColor = DarkSurfaceCard,
      titleContentColor = TextPrimary,
      textContentColor = TextSecondary,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = TerminalYellow)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "HIBERNAÇÃO & SEGUNDO PLANO",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
          )
        }
      },
      text = {
        Column(modifier = Modifier.padding(top = 4.dp)) {
          Text(
            text = "Como o Android gerencia apps em segundo plano:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• O KERNEL X opera de forma segura e transparente sem root, utilizando as políticas oficiais de Doze Mode e App Standby do Android.\n\n" +
              "• Para restringir apps que consomem bateria excessiva, use a otimização de bateria do sistema ou coloque o app em 'Restrito'.\n\n" +
              "• Aplicativos do sistema possuem proteção e não devem ser finalizados forçadamente.",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showHibernationGuideDialog = false
            onOpenBatteryUsageSettings()
          },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricRed)
        ) {
          Text("USO DA BATERIA", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
      },
      dismissButton = {
        TextButton(onClick = { showHibernationGuideDialog = false }) {
          Text("FECHAR", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun AppListItem(
  app: AppItem,
  onClick: () -> Unit,
  onLaunch: () -> Unit,
  onUninstall: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkSurfaceCard, RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(12.dp)
      .testTag("app_item_${app.packageName}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // App Icon
      val bitmap = remember(app.packageName) {
        try {
          app.icon?.toBitmap(80, 80, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
          null
        }
      }

      if (bitmap != null) {
        Image(
          bitmap = bitmap.asImageBitmap(),
          contentDescription = app.appName,
          modifier = Modifier.size(42.dp)
        )
      } else {
        Box(
          modifier = Modifier
            .size(42.dp)
            .background(DarkSurfaceElevated, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = app.appName.take(1).uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = ElectricRed
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = app.appName,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
          Spacer(modifier = Modifier.width(6.dp))
          TechBadge(
            text = if (app.isSystemApp) "SISTEMA" else "USUÁRIO",
            color = if (app.isSystemApp) TextMuted else TerminalGreen
          )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = app.packageName,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "v${app.versionName}",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TextMuted
          )
          if (app.apkSizeBytes > 0L) {
            Text(
              text = "• ${FormatUtils.formatBytes(app.apkSizeBytes)}",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = TextMuted
            )
          }
        }
      }

      if (app.hasLaunchIntent) {
        IconButton(
          onClick = onLaunch,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Launch,
            contentDescription = "Abrir ${app.appName}",
            tint = TerminalGreen,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun AppDetailSheetContent(
  app: AppItem,
  onLaunch: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenBattery: () -> Unit,
  onUninstall: () -> Unit,
  onDismiss: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val bitmap = remember(app.packageName) {
        try {
          app.icon?.toBitmap(120, 120, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
          null
        }
      }
      if (bitmap != null) {
        Image(
          bitmap = bitmap.asImageBitmap(),
          contentDescription = app.appName,
          modifier = Modifier.size(54.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = app.appName,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = TextPrimary
        )
        Text(
          text = app.packageName,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = TextSecondary
        )
      }

      TechBadge(
        text = if (app.isSystemApp) "SISTEMA" else "USUÁRIO",
        color = if (app.isSystemApp) TextMuted else TerminalGreen
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Tech Specs
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
        .padding(12.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Versão:", fontSize = 12.sp, color = TextSecondary)
          Text("${app.versionName} (${app.versionCode})", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Tamanho APK:", fontSize = 12.sp, color = TextSecondary)
          Text(FormatUtils.formatBytes(app.apkSizeBytes), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Target / Min SDK:", fontSize = 12.sp, color = TextSecondary)
          Text("API ${app.targetSdk} / Min ${app.minSdk}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Permissões Solicitadas:", fontSize = 12.sp, color = TextSecondary)
          Text("${app.requestedPermissions.size} declaradas (${app.grantedPermissions.size} concedidas)", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TerminalCyan)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Instalado em:", fontSize = 12.sp, color = TextSecondary)
          Text(FormatUtils.formatDate(app.firstInstallTime), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Actions
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      if (app.hasLaunchIntent) {
        Button(
          onClick = {
            onLaunch()
            onDismiss()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_detail_launch"),
          colors = ButtonDefaults.buttonColors(containerColor = TerminalGreenDark)
        ) {
          Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("ABRIR APLICATIVO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
      }

      OutlinedButton(
        onClick = onOpenSettings,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_detail_settings")
      ) {
        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("CONFIGURAÇÕES DO SISTEMA (PERMISSÕES & DADOS)", color = TextPrimary, fontSize = 11.sp)
      }

      OutlinedButton(
        onClick = onOpenBattery,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_detail_battery")
      ) {
        Icon(Icons.Default.BatteryAlert, contentDescription = null, modifier = Modifier.size(16.dp), tint = TerminalYellow)
        Spacer(modifier = Modifier.width(8.dp))
        Text("CONFIGURAÇÕES DE BATERIA & HIBERNAÇÃO", color = TextPrimary, fontSize = 11.sp)
      }

      Button(
        onClick = onUninstall,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_detail_uninstall"),
        colors = ButtonDefaults.buttonColors(containerColor = ElectricRed)
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("SOLICITAR DESINSTALAÇÃO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}
