package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.terminal.TerminalEngine
import com.example.data.terminal.TerminalLine
import com.example.data.terminal.TerminalLineType
import com.example.data.termux.TermuxInstallStatus
import com.example.ui.components.TechBadge
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricRed
import com.example.ui.theme.ElectricRedDark
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TerminalUiState

@Composable
fun TerminalScreen(
  uiState: TerminalUiState,
  onCommandChange: (String) -> Unit,
  onExecute: (String?) -> Unit,
  onClear: () -> Unit,
  onCopy: () -> Unit,
  onSetEngine: (TerminalEngine) -> Unit,
  onToggleTermuxDialog: (Boolean) -> Unit,
  onDismissCopyFeedback: () -> Unit,
  onOpenTermuxInstall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val listState = rememberLazyListState()

  // Auto-scroll on new lines
  LaunchedEffect(uiState.lines.size) {
    if (uiState.lines.isNotEmpty()) {
      listState.animateScrollToItem(uiState.lines.size - 1)
    }
  }

  val quickCommands = listOf(
    "uname -a",
    "uptime",
    "df -h",
    "getprop ro.build.version.release",
    "cat /proc/version",
    "cat /proc/cpuinfo",
    "date",
    "whoami",
    "id",
    "ls -la"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DeepBlack)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    // Header & Controls
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = null,
            tint = ElectricRed,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "KERNEL TERMINAL",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
            color = TextPrimary
          )
        }
        Text(
          text = "DIR: ${uiState.currentDirectory}",
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Termux Info / Setup
        IconButton(
          onClick = { onToggleTermuxDialog(true) },
          modifier = Modifier.testTag("btn_termux_info")
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Configuração do Termux",
            tint = if (uiState.termuxStatus == TermuxInstallStatus.INSTALLED_READY) TerminalGreen else TerminalYellow
          )
        }

        IconButton(
          onClick = onCopy,
          modifier = Modifier.testTag("btn_terminal_copy")
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copiar Saída",
            tint = TextSecondary
          )
        }

        IconButton(
          onClick = onClear,
          modifier = Modifier.testTag("btn_terminal_clear")
        ) {
          Icon(
            imageVector = Icons.Default.ClearAll,
            contentDescription = "Limpar Terminal",
            tint = TextSecondary
          )
        }
      }
    }

    // Engine Selector Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FilterChip(
        selected = uiState.activeEngine == TerminalEngine.INTERNAL_SHELL,
        onClick = { onSetEngine(TerminalEngine.INTERNAL_SHELL) },
        label = {
          Text(
            text = "SHELL INTERNO (SANDBOX)",
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
        modifier = Modifier.testTag("tab_engine_internal")
      )

      FilterChip(
        selected = uiState.activeEngine == TerminalEngine.TERMUX_BRIDGE,
        onClick = { onSetEngine(TerminalEngine.TERMUX_BRIDGE) },
        label = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "TERMUX BRIDGE",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            val badgeColor = when (uiState.termuxStatus) {
              TermuxInstallStatus.INSTALLED_READY -> TerminalGreen
              TermuxInstallStatus.INSTALLED_PERMISSION_REQUIRED -> TerminalYellow
              TermuxInstallStatus.NOT_INSTALLED -> TerminalError
            }
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(badgeColor, RoundedCornerShape(3.dp))
            )
          }
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = ElectricRed,
          selectedLabelColor = Color.White,
          containerColor = DarkSurfaceElevated,
          labelColor = TextSecondary
        ),
        modifier = Modifier.testTag("tab_engine_termux")
      )
    }

    // Quick Command Bar
    val quickScroll = rememberScrollState()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(quickScroll)
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      quickCommands.forEach { cmd ->
        Box(
          modifier = Modifier
            .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable { onExecute(cmd) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("quick_cmd_$cmd")
        ) {
          Text(
            text = cmd,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TerminalCyan
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Main Terminal Console Canvas
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .background(Color(0xFF030303), RoundedCornerShape(8.dp))
        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
        .padding(10.dp)
        .testTag("console_output_box")
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        items(
          items = uiState.lines,
          key = { it.id }
        ) { line ->
          TerminalLineRow(line)
        }
      }

      if (uiState.isExecuting) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .background(DarkSurfaceElevated.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
              color = ElectricRed,
              strokeWidth = 2.dp,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "EXECUTANDO...",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = TextPrimary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Input row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = uiState.commandInput,
        onValueChange = onCommandChange,
        modifier = Modifier
          .weight(1f)
          .testTag("input_terminal_command"),
        placeholder = {
          Text(
            text = if (uiState.activeEngine == TerminalEngine.INTERNAL_SHELL) "Digitar comando shell..." else "Comando para Termux RUN_COMMAND...",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TextMuted
          )
        },
        leadingIcon = {
          Text(
            text = ">",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ElectricRed,
            modifier = Modifier.padding(start = 12.dp)
          )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onExecute(null) }),
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

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = { onExecute(null) },
        enabled = !uiState.isExecuting && uiState.commandInput.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = ElectricRed,
          disabledContainerColor = ElectricRedDark
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("btn_terminal_execute")
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Executar",
          tint = Color.White
        )
      }
    }
  }

  // Copy Feedback Banner
  if (uiState.showCopyFeedback) {
    LaunchedEffect(Unit) {
      kotlinx.coroutines.delay(2000)
      onDismissCopyFeedback()
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      Snackbar(
        containerColor = DarkSurfaceElevated,
        contentColor = TerminalGreen
      ) {
        Text(
          text = "Saída copiada para a área de transferência!",
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp
        )
      }
    }
  }

  // Termux Integration Setup Dialog
  if (uiState.showTermuxSetupDialog) {
    AlertDialog(
      onDismissRequest = { onToggleTermuxDialog(false) },
      containerColor = DarkSurfaceCard,
      titleContentColor = TextPrimary,
      textContentColor = TextSecondary,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Terminal, contentDescription = null, tint = ElectricRed)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "INTEGRAÇÃO REAL COM TERMUX",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      },
      text = {
        val scroll = rememberScrollState()
        Column(modifier = Modifier.verticalScroll(scroll)) {
          // Status badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Status da Conexão:", fontSize = 12.sp, color = TextSecondary)
            val (statusText, statusColor) = when (uiState.termuxStatus) {
              TermuxInstallStatus.INSTALLED_READY -> "PRONTO / AUTORIZADO" to TerminalGreen
              TermuxInstallStatus.INSTALLED_PERMISSION_REQUIRED -> "INSTALADO (FALTA PERMISSÃO)" to TerminalYellow
              TermuxInstallStatus.NOT_INSTALLED -> "NÃO ENCONTRADO" to TerminalError
            }
            TechBadge(text = statusText, color = statusColor)
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "O KERNEL X utiliza a API oficial com.termux.RUN_COMMAND para despachar comandos diretamente para o ecossistema GNU/Linux do Termux.",
            fontSize = 12.sp,
            color = TextPrimary,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "PASSO A PASSO PARA HABILITAR:",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = TerminalCyan
          )

          Spacer(modifier = Modifier.height(6.dp))

          val configCmd = "echo \"allow-external-apps = true\" >> ~/.termux/termux.properties && termux-reload-settings"
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.Black, RoundedCornerShape(6.dp))
              .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "1. No Termux, execute:",
                fontSize = 10.sp,
                color = TextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = configCmd,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGreen
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Button(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
              clipboard?.setPrimaryClip(ClipData.newPlainText("Termux Config", configCmd))
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextPrimary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("COPIAR COMANDO DE ATIVAÇÃO", color = TextPrimary, fontSize = 11.sp)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "2. Permissão com.termux.permission.RUN_COMMAND declarada no manifesto do KERNEL X.\n" +
              "3. Os resultados são devolvidos de forma assíncrona pelo broadcast receiver com.aistudio.kernelx.TERMUX_RESULT.\n" +
              "4. Não requer root. Opera de acordo com as diretrizes de segurança do Android.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp
          )
        }
      },
      confirmButton = {
        if (uiState.termuxStatus == TermuxInstallStatus.NOT_INSTALLED) {
          Button(
            onClick = onOpenTermuxInstall,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricRed)
          ) {
            Text("BAIXAR TERMUX (F-DROID)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
          }
        } else {
          Button(
            onClick = { onToggleTermuxDialog(false) },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricRed)
          ) {
            Text("ENTENDIDO", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { onToggleTermuxDialog(false) }) {
          Text("FECHAR", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun TerminalLineRow(line: TerminalLine) {
  val textColor = when (line.type) {
    TerminalLineType.PROMPT_COMMAND -> ElectricRed
    TerminalLineType.STDOUT -> TextPrimary
    TerminalLineType.STDERR -> TerminalError
    TerminalLineType.SYSTEM_INFO -> TerminalCyan
    TerminalLineType.SUCCESS -> TerminalGreen
  }

  Text(
    text = line.text,
    fontFamily = FontFamily.Monospace,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    color = textColor
  )
}
