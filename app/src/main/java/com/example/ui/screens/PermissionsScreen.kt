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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.apps.PermissionAppDetail
import com.example.data.apps.PermissionGroupSummary
import com.example.data.apps.SensitivePermissionGroup
import com.example.ui.components.GlowingCard
import com.example.ui.components.TechBadge
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PermissionsUiState

@Composable
fun PermissionsScreen(
  uiState: PermissionsUiState,
  onSelectGroup: (SensitivePermissionGroup?) -> Unit,
  onSelectAppDetail: (PermissionAppDetail?) -> Unit,
  onSearchChange: (String) -> Unit,
  onToggleOnlyGranted: () -> Unit,
  onOpenAppDetailsSettings: (String) -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
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
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (uiState.selectedGroup != null) {
          IconButton(
            onClick = { onSelectGroup(null) },
            modifier = Modifier.testTag("btn_back_permission_groups")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Voltar",
              tint = TextPrimary
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
        }

        Column {
          Text(
            text = if (uiState.selectedGroup != null) uiState.selectedGroup.title.uppercase() else "AUDITORIA DE PERMISSÕES",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            letterSpacing = 1.sp,
            color = TextPrimary
          )
          Text(
            text = if (uiState.selectedGroup != null) uiState.selectedGroup.description else "MONITORAMENTO DE ACESSOS CRÍTICOS",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = ElectricRed
          )
        }
      }

      IconButton(
        onClick = onRefresh,
        modifier = Modifier.testTag("btn_permissions_refresh")
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Atualizar Permissões",
          tint = TextSecondary
        )
      }
    }

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
            text = "ANALISANDO DECLARAÇÕES DE SEGURANÇA...",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      }
    } else if (uiState.selectedGroup == null) {
      // Show Grid / List of Sensitive Permission Categories
      val scrollState = rememberScrollState()
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        uiState.groupSummaries.forEach { summary ->
          PermissionCategoryCard(
            summary = summary,
            onClick = { onSelectGroup(summary.group) }
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    } else {
      // Category Drill-down
      val currentSummary = uiState.groupSummaries.firstOrNull { it.group == uiState.selectedGroup }
      if (currentSummary != null) {
        // Search & Filter row
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = onSearchChange,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_perm_search"),
          placeholder = { Text("Buscar aplicativo...", fontSize = 12.sp, color = TextMuted) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
          trailingIcon = {
            if (uiState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchChange("") }) {
                Icon(Icons.Default.Close, contentDescription = "Limpar", tint = TextSecondary)
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
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${currentSummary.grantedAppsCount} com acesso concedido de ${currentSummary.declaredAppsCount} declarados",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextSecondary
          )

          FilterChip(
            selected = uiState.onlyGrantedFilter,
            onClick = onToggleOnlyGranted,
            label = { Text("Apenas Concedidas", fontSize = 10.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ElectricRed,
              selectedLabelColor = TextPrimary,
              containerColor = DarkSurfaceElevated,
              labelColor = TextSecondary
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val filteredApps = currentSummary.apps.filter { app ->
          val query = uiState.searchQuery.trim().lowercase()
          val matchesQuery = query.isEmpty() ||
            app.appName.lowercase().contains(query) ||
            app.packageName.lowercase().contains(query)
          val matchesGrant = if (uiState.onlyGrantedFilter) app.grantedPermissions.isNotEmpty() else true
          matchesQuery && matchesGrant
        }

        if (filteredApps.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text("Nenhum aplicativo encontrado neste filtro.", color = TextMuted)
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .testTag("list_permission_apps"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(filteredApps, key = { it.packageName }) { appDetail ->
              PermissionAppItem(
                app = appDetail,
                onClick = { onSelectAppDetail(appDetail) },
                onOpenSettings = { onOpenAppDetailsSettings(appDetail.packageName) }
              )
            }
          }
        }
      }
    }
  }

  // App Permission Detail Dialog
  uiState.selectedAppDetail?.let { detail ->
    AlertDialog(
      onDismissRequest = { onSelectAppDetail(null) },
      containerColor = DarkSurfaceCard,
      titleContentColor = TextPrimary,
      textContentColor = TextSecondary,
      title = {
        Column {
          Text(
            text = detail.appName,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
          )
          Text(
            text = detail.packageName,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      },
      text = {
        val scroll = rememberScrollState()
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
        ) {
          Text(
            text = "PERMISSÕES NESTE GRUPO:",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(8.dp))

          detail.declaredPermissions.forEach { perm ->
            val isGranted = perm in detail.grantedPermissions
            val permName = perm.substringAfterLast('.')

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = permName,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
              )
              TechBadge(
                text = if (isGranted) "CONCEDIDA" else "NEGADA",
                color = if (isGranted) TerminalGreen else TerminalError
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "O Android exige que permissões de aplicativos de terceiros sejam alteradas através da tela de gerenciamento de permissões do sistema.",
            fontSize = 11.sp,
            color = TextMuted,
            lineHeight = 16.sp
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onOpenAppDetailsSettings(detail.packageName)
            onSelectAppDetail(null)
          },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricRed),
          modifier = Modifier.testTag("btn_perm_open_system_settings")
        ) {
          Text("AJUSTAR NO ANDROID", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
      },
      dismissButton = {
        TextButton(onClick = { onSelectAppDetail(null) }) {
          Text("FECHAR", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun PermissionCategoryCard(
  summary: PermissionGroupSummary,
  onClick: () -> Unit
) {
  val icon = when (summary.group) {
    SensitivePermissionGroup.CAMERA -> Icons.Default.CameraAlt
    SensitivePermissionGroup.MICROPHONE -> Icons.Default.Mic
    SensitivePermissionGroup.LOCATION -> Icons.Default.Place
    SensitivePermissionGroup.CONTACTS -> Icons.Default.Contacts
    SensitivePermissionGroup.PHONE -> Icons.Default.Phone
    SensitivePermissionGroup.SMS -> Icons.Default.Sms
    SensitivePermissionGroup.STORAGE -> Icons.Default.Folder
    SensitivePermissionGroup.NOTIFICATIONS -> Icons.Default.Notifications
  }

  val accentColor = when (summary.group) {
    SensitivePermissionGroup.CAMERA -> ElectricRed
    SensitivePermissionGroup.MICROPHONE -> TerminalYellow
    SensitivePermissionGroup.LOCATION -> TerminalCyan
    SensitivePermissionGroup.STORAGE -> TerminalGreen
    SensitivePermissionGroup.SMS -> ElectricRed
    else -> TerminalCyan
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkSurfaceCard, RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(14.dp)
      .testTag("perm_card_${summary.group.id}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = summary.group.title,
          tint = accentColor,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = summary.group.title,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = summary.group.description,
          fontSize = 11.sp,
          color = TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${summary.grantedAppsCount}",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = if (summary.grantedAppsCount > 0) accentColor else TextMuted
        )
        Text(
          text = "de ${summary.declaredAppsCount} apps",
          fontFamily = FontFamily.Monospace,
          fontSize = 9.sp,
          color = TextMuted
        )
      }
    }
  }
}

@Composable
private fun PermissionAppItem(
  app: PermissionAppDetail,
  onClick: () -> Unit,
  onOpenSettings: () -> Unit
) {
  val hasGrant = app.grantedPermissions.isNotEmpty()

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkSurfaceCard, RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
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
          modifier = Modifier.size(36.dp)
        )
      } else {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(DarkSurfaceElevated, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = app.appName.take(1).uppercase(),
            fontFamily = FontFamily.Monospace,
            color = ElectricRed
          )
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = app.appName,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
        }
        Text(
          text = app.packageName,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      TechBadge(
        text = if (hasGrant) "CONCEDIDA" else "NEGADA",
        color = if (hasGrant) TerminalGreen else TerminalError
      )
    }
  }
}
