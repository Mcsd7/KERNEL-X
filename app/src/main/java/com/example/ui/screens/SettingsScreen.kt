package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.termux.TermuxInstallStatus
import com.example.ui.components.GlowingCard
import com.example.ui.components.KernelLogo
import com.example.ui.components.TechBadge
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

@Composable
fun SettingsScreen(
  onBack: () -> Unit,
  termuxStatus: TermuxInstallStatus,
  refreshInterval: Long,
  onSetInterval: (Long) -> Unit,
  onOpenTermuxSetup: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DeepBlack)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .verticalScroll(scrollState)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("btn_settings_back")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Voltar ao Dashboard",
          tint = TextPrimary
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = "CONFIGURAÇÕES",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          letterSpacing = 1.sp,
          color = TextPrimary
        )
        Text(
          text = "KERNEL X SYSTEM CONTROL",
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = ElectricRed
        )
      }
    }

    // 1. App Info & Logo Card
    GlowingCard(
      title = "Sobre o KERNEL X",
      icon = Icons.Default.Info,
      accentColor = ElectricRed,
      testTag = "card_settings_info"
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        KernelLogo(size = 48.dp, showSubtext = false)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(
            text = "KERNEL X",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = TextPrimary
          )
          Text(
            text = "Versão 1.0.0 (Build 2026.1)",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = ElectricRed
          )
          Text(
            text = "Central Nativa de Administração Android",
            fontSize = 11.sp,
            color = TextSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Termux Connection Status
    GlowingCard(
      title = "Integração Termux",
      icon = Icons.Default.Terminal,
      accentColor = TerminalCyan,
      testTag = "card_settings_termux"
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Status da Conexão:",
          fontSize = 13.sp,
          color = TextSecondary
        )
        val (text, color) = when (termuxStatus) {
          TermuxInstallStatus.INSTALLED_READY -> "CONECTADO / AUTORIZADO" to TerminalGreen
          TermuxInstallStatus.INSTALLED_PERMISSION_REQUIRED -> "FALTA PERMISSÃO" to TerminalYellow
          TermuxInstallStatus.NOT_INSTALLED -> "NÃO INSTALADO" to ElectricRed
        }
        TechBadge(text = text, color = color)
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "O mecanismo RUN_COMMAND permite que o KERNEL X execute rotinas Linux completas no Termux sob demanda.",
        fontSize = 11.sp,
        color = TextMuted,
        lineHeight = 16.sp
      )

      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = onOpenTermuxSetup,
        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("VER INSTRUÇÕES DE CONFIGURAÇÃO", color = TextPrimary, fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Telemetry Update Interval
    GlowingCard(
      title = "Frequência de Atualização",
      icon = Icons.Default.Speed,
      accentColor = TerminalYellow,
      testTag = "card_settings_telemetry"
    ) {
      Text(
        text = "Defina o intervalo de amostragem das métricas de RAM, CPU e armazenamento em tempo real:",
        fontSize = 12.sp,
        color = TextSecondary
      )
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(1000L to "1 segundo (Alta)", 2000L to "2 segundos (Padrão)", 5000L to "5 segundos (Econômico)").forEach { (ms, lbl) ->
          val isSelected = refreshInterval == ms
          FilterChip(
            selected = isSelected,
            onClick = { onSetInterval(ms) },
            label = {
              Text(
                text = lbl,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ElectricRed,
              selectedLabelColor = Color.White,
              containerColor = DarkSurfaceElevated,
              labelColor = TextSecondary
            ),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. Visual Theme Specs
    GlowingCard(
      title = "Identidade Visual & Tema",
      icon = Icons.Default.Palette,
      accentColor = ElectricRed,
      testTag = "card_settings_theme"
    ) {
      Text(
        text = "Tema Cyberpunk de Alto Contraste",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Fundo Deep Black (#080808) com realces em Vermelho Elétrico (#F00035) e painéis em cinza escuro texturizado para máxima eficiência em telas OLED/AMOLED.",
        fontSize = 12.sp,
        color = TextSecondary,
        lineHeight = 16.sp
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 5. Security & Transparency
    GlowingCard(
      title = "Segurança & Transparência",
      icon = Icons.Default.Security,
      accentColor = TerminalGreen,
      testTag = "card_settings_security"
    ) {
      Text(
        text = "• Operação 100% sem Root (Rootless Compliance)",
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = TerminalGreen
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "• APIs Oficiais do Android (ActivityManager, PackageManager, StatFs)\n" +
          "• Não faz desinstalação silenciosa nem remoção oculta de aplicativos\n" +
          "• Não altera permissões sem autorização explícita do usuário\n" +
          "• Não envia telemetria para servidores externos",
        fontSize = 11.sp,
        color = TextSecondary,
        lineHeight = 17.sp
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 6. Project Credits
    GlowingCard(
      title = "Créditos do Projeto",
      accentColor = TextMuted,
      testTag = "card_settings_credits"
    ) {
      Text(
        text = "KERNEL X — ANDROID SYSTEM CONTROL",
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Desenvolvido nativamente com Kotlin, Jetpack Compose, Material 3, Clean Architecture MVVM, Coroutines & Flow.",
        fontSize = 11.sp,
        color = TextSecondary
      )
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
