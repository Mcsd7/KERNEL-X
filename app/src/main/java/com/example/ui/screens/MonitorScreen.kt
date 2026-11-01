package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.ui.components.CircularGauge
import com.example.ui.components.GlowingCard
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
fun MonitorScreen(
  monitorState: MonitorUiState,
  onRefresh: () -> Unit,
  onSetInterval: (Long) -> Unit,
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
          text = "MONITOR DO SISTEMA",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          letterSpacing = 1.sp,
          color = TextPrimary
        )
        Text(
          text = "TELEMETRIA DE HARDWARE & RECURSOS",
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = ElectricRed
        )
      }

      IconButton(
        onClick = onRefresh,
        modifier = Modifier.testTag("btn_monitor_refresh")
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Atualizar Monitor",
          tint = TextSecondary
        )
      }
    }

    // Refresh interval selector
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "Taxa de Amostragem:",
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = TextMuted
      )

      listOf(1000L to "1s", 2000L to "2s", 5000L to "5s").forEach { (interval, label) ->
        val isSelected = monitorState.refreshIntervalMillis == interval
        FilterChip(
          selected = isSelected,
          onClick = { onSetInterval(interval) },
          label = {
            Text(
              text = label,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricRed,
            selectedLabelColor = Color.White,
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

    if (snapshot == null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = ElectricRed)
      }
    } else {
      // 1. RAM Monitor Card
      GlowingCard(
        title = "Memória RAM",
        icon = Icons.Default.Memory,
        accentColor = ElectricRed,
        testTag = "card_ram_monitor"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          CircularGauge(
            percentage = snapshot.ram.usedPercent,
            title = "UTILIZAÇÃO",
            gaugeColor = if (snapshot.ram.usedPercent > 0.85f) ElectricRed else TerminalCyan,
            size = 120.dp
          )

          Column(
            modifier = Modifier
              .weight(1f)
              .padding(start = 16.dp)
          ) {
            MetricRow(
              label = "Total de Memória",
              value = FormatUtils.formatBytes(snapshot.ram.totalBytes)
            )
            MetricRow(
              label = "Memória Utilizada",
              value = FormatUtils.formatBytes(snapshot.ram.usedBytes),
              valueColor = if (snapshot.ram.usedPercent > 0.85f) ElectricRed else TextPrimary
            )
            MetricRow(
              label = "Memória Disponível",
              value = FormatUtils.formatBytes(snapshot.ram.availableBytes),
              valueColor = TerminalGreen
            )
            MetricRow(
              label = "Threshold Crítico",
              value = FormatUtils.formatBytes(snapshot.ram.thresholdBytes)
            )

            if (snapshot.ram.isLowMemory) {
              Spacer(modifier = Modifier.height(4.dp))
              TechBadge(
                text = "ALERTA: MEMÓRIA BAIXA",
                color = ElectricRed
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "GRÁFICO DE CONSUMO EM TEMPO REAL",
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          letterSpacing = 1.sp,
          color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        WaveformChart(
          history = monitorState.ramHistory,
          lineColor = ElectricRed,
          height = 70.dp
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Storage Monitor Card
      GlowingCard(
        title = "Armazenamento Interno",
        icon = Icons.Default.SdCard,
        accentColor = TerminalGreen,
        testTag = "card_storage_monitor"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          CircularGauge(
            percentage = snapshot.storage.usedPercent,
            title = "DISCO",
            gaugeColor = if (snapshot.storage.usedPercent > 0.90f) ElectricRed else TerminalGreen,
            size = 120.dp
          )

          Column(
            modifier = Modifier
              .weight(1f)
              .padding(start = 16.dp)
          ) {
            MetricRow(
              label = "Capacidade Total",
              value = FormatUtils.formatBytes(snapshot.storage.totalBytes)
            )
            MetricRow(
              label = "Espaço Ocupado",
              value = FormatUtils.formatBytes(snapshot.storage.usedBytes)
            )
            MetricRow(
              label = "Espaço Livre",
              value = FormatUtils.formatBytes(snapshot.storage.freeBytes),
              valueColor = TerminalGreen
            )
            MetricRow(
              label = "Percentual Usado",
              value = "${(snapshot.storage.usedPercent * 100).toInt()}%"
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        LinearProgressIndicator(
          progress = { snapshot.storage.usedPercent },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
          color = if (snapshot.storage.usedPercent > 0.90f) ElectricRed else TerminalGreen,
          trackColor = DarkSurfaceBorder
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. CPU & Processor Card
      GlowingCard(
        title = "Processador & CPU",
        icon = Icons.Default.DeveloperBoard,
        accentColor = TerminalCyan,
        testTag = "card_cpu_monitor"
      ) {
        MetricRow(label = "Processador / Hardware", value = snapshot.cpu.processorName)
        MetricRow(label = "Arquitetura", value = snapshot.cpu.architecture)
        MetricRow(label = "Número de Núcleos", value = "${snapshot.cpu.coresCount} núcleos detectados")
        MetricRow(label = "Conjunto de Instruções", value = snapshot.cpu.abiList)

        if (snapshot.cpu.cores.isNotEmpty()) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "NÚCLEOS DE PROCESSAMENTO",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = TextMuted
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            snapshot.cpu.cores.chunked(2).forEach { rowCores ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                rowCores.forEach { core ->
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                      .padding(8.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = "CORE #${core.coreIndex}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextPrimary
                      )
                      val freqStr = core.frequencyKHz?.let { "${it / 1000} MHz" } ?: "Ativo"
                      TechBadge(
                        text = freqStr,
                        color = TerminalCyan
                      )
                    }
                  }
                }
                if (rowCores.size == 1) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun MetricRow(
  label: String,
  value: String,
  valueColor: Color = TextPrimary
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      color = TextSecondary
    )
    Text(
      text = value,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp,
      color = valueColor
    )
  }
}
