package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.system.SystemSnapshot
import com.example.ui.components.CircularGauge
import com.example.ui.components.GlowingCard
import com.example.ui.components.KernelLogo
import com.example.ui.components.TechBadge
import com.example.ui.components.WaveformChart
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils
import com.example.viewmodel.MonitorUiState

@Composable
fun HomeScreen(
  monitorState: MonitorUiState,
  onRefresh: () -> Unit,
  onOpenSettings: () -> Unit,
  onNavigateToMonitor: () -> Unit,
  onNavigateToApps: () -> Unit,
  onNavigateToPermissions: () -> Unit,
  onNavigateToTerminal: () -> Unit,
  modifier: Modifier = Modifier
) {
  val snapshot = monitorState.snapshot
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DeepBlack)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    // Header with Logo, Status Ticker and Settings Icon
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      KernelLogo(size = 46.dp)

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Live pulse indicator
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(TerminalGreen, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "LIVE",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          color = TerminalGreen
        )
        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
          onClick = onRefresh,
          modifier = Modifier.testTag("btn_home_refresh")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Atualizar Métricas",
            tint = TextSecondary
          )
        }

        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.testTag("btn_home_settings")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Configurações do Kernel X",
            tint = ElectricRed
          )
        }
      }
    }

    if (monitorState.isLoading && snapshot == null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = ElectricRed)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "INICIALIZANDO SUBSISTEMAS...",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TextMuted
          )
        }
      }
    } else if (snapshot != null) {
      // 1. Device Core Specs Card
      GlowingCard(
        title = "Dispositivo & Arquitetura",
        icon = Icons.Default.Smartphone,
        accentColor = ElectricRed,
        testTag = "card_device_specs"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${snapshot.hardware.manufacturer} ${snapshot.hardware.model}",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Plataforma: ${snapshot.hardware.board} (${snapshot.hardware.hardware})",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }

          TechBadge(
            text = "API ${snapshot.hardware.apiLevel}",
            color = ElectricRed
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SpecPill(
            label = "ANDROID",
            value = snapshot.hardware.androidVersion,
            modifier = Modifier.weight(1f)
          )
          SpecPill(
            label = "ARQUITETURA",
            value = snapshot.cpu.architecture,
            modifier = Modifier.weight(1f)
          )
          SpecPill(
            label = "NÚCLEOS",
            value = "${snapshot.cpu.coresCount} Cores",
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "UPTIME: ${FormatUtils.formatDuration(snapshot.hardware.uptimeMillis)}",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Real-time Telemetry: RAM & Storage Dual Gauges
      GlowingCard(
        title = "Telemetria de Recursos",
        icon = Icons.Default.Speed,
        accentColor = ElectricRed,
        testTag = "card_telemetry_gauges"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // RAM Gauge
          CircularGauge(
            percentage = snapshot.ram.usedPercent,
            title = "RAM",
            subtitle = "${FormatUtils.formatBytes(snapshot.ram.usedBytes)}",
            gaugeColor = if (snapshot.ram.usedPercent > 0.85f) ElectricRed else TerminalCyan
          )

          // Storage Gauge
          CircularGauge(
            percentage = snapshot.storage.usedPercent,
            title = "Armazenamento",
            subtitle = "${FormatUtils.formatBytes(snapshot.storage.usedBytes)}",
            gaugeColor = if (snapshot.storage.usedPercent > 0.90f) ElectricRed else TerminalGreen
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Breakdown bars
        Text(
          text = "MEMÓRIA RAM: ${FormatUtils.formatBytes(snapshot.ram.usedBytes)} usados de ${FormatUtils.formatBytes(snapshot.ram.totalBytes)} (${FormatUtils.formatBytes(snapshot.ram.availableBytes)} livres)",
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { snapshot.ram.usedPercent },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
          color = if (snapshot.ram.usedPercent > 0.85f) ElectricRed else TerminalCyan,
          trackColor = DarkSurfaceBorder
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "ARMAZENAMENTO INTERNO: ${FormatUtils.formatBytes(snapshot.storage.usedBytes)} usados de ${FormatUtils.formatBytes(snapshot.storage.totalBytes)} (${FormatUtils.formatBytes(snapshot.storage.freeBytes)} livres)",
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { snapshot.storage.usedPercent },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
          color = if (snapshot.storage.usedPercent > 0.90f) ElectricRed else TerminalGreen,
          trackColor = DarkSurfaceBorder
        )

        if (monitorState.ramHistory.isNotEmpty()) {
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "HISTÓRICO DE CONSUMO EM TEMPO REAL",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = TextMuted
          )
          Spacer(modifier = Modifier.height(6.dp))
          WaveformChart(
            history = monitorState.ramHistory,
            lineColor = ElectricRed,
            height = 50.dp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. System Stats Grid: Battery & Installed Apps
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Battery Card
        GlowingCard(
          title = "Bateria",
          icon = Icons.Default.BatteryChargingFull,
          accentColor = TerminalYellow,
          modifier = Modifier.weight(1f),
          testTag = "card_battery_summary"
        ) {
          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "${snapshot.battery.level}%",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp,
              color = TextPrimary
            )
            TechBadge(
              text = if (snapshot.battery.isCharging) "CARREGANDO" else "DESCARGA",
              color = if (snapshot.battery.isCharging) TerminalGreen else TerminalYellow
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Temp: ${snapshot.battery.temperatureCelsius}°C",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
          )
          Text(
            text = "Saúde: ${snapshot.battery.health}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
          )
        }

        // Apps Count Card
        GlowingCard(
          title = "Aplicativos",
          icon = Icons.Default.Apps,
          accentColor = TerminalCyan,
          modifier = Modifier.weight(1f),
          testTag = "card_apps_summary"
        ) {
          Text(
            text = "${snapshot.totalInstalledApps}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Usuário: ${snapshot.userAppsCount}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TerminalGreen
          )
          Text(
            text = "Sistema: ${snapshot.systemAppsCount}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Quick Action Hub / Shortcuts
      Text(
        text = "PAINEL DE CONTROLE",
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        QuickActionButton(
          label = "Monitor",
          sub = "CPU & RAM",
          icon = Icons.Default.Speed,
          onClick = onNavigateToMonitor,
          modifier = Modifier.weight(1f),
          testTag = "btn_quick_monitor"
        )
        QuickActionButton(
          label = "Apps",
          sub = "Gerenciador",
          icon = Icons.Default.Apps,
          onClick = onNavigateToApps,
          modifier = Modifier.weight(1f),
          testTag = "btn_quick_apps"
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        QuickActionButton(
          label = "Permissões",
          sub = "Auditoria",
          icon = Icons.Default.Security,
          onClick = onNavigateToPermissions,
          modifier = Modifier.weight(1f),
          testTag = "btn_quick_permissions"
        )
        QuickActionButton(
          label = "Terminal",
          sub = "Shell & Termux",
          icon = Icons.Default.Code,
          onClick = onNavigateToTerminal,
          modifier = Modifier.weight(1f),
          testTag = "btn_quick_terminal"
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun SpecPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 8.dp)
  ) {
    Column {
      Text(
        text = label,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        letterSpacing = 0.5.sp,
        color = TextMuted
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = TextPrimary
      )
    }
  }
}

@Composable
private fun QuickActionButton(
  label: String,
  sub: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  Box(
    modifier = modifier
      .background(DarkSurfaceCard, RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(12.dp)
      .testTag(testTag)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .background(ElectricRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = ElectricRed,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = label,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = TextPrimary
        )
        Text(
          text = sub,
          fontSize = 10.sp,
          color = TextSecondary
        )
      }
    }
  }
}
