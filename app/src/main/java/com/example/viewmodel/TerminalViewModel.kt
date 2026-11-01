package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.terminal.ShellExecutor
import com.example.data.terminal.TerminalEngine
import com.example.data.terminal.TerminalLine
import com.example.data.terminal.TerminalLineType
import com.example.data.termux.TermuxInstallStatus
import com.example.data.termux.TermuxManager
import com.example.data.termux.TermuxResultReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TerminalUiState(
  val lines: List<TerminalLine> = emptyList(),
  val commandInput: String = "",
  val isExecuting: Boolean = false,
  val activeEngine: TerminalEngine = TerminalEngine.INTERNAL_SHELL,
  val currentDirectory: String = "",
  val termuxStatus: TermuxInstallStatus = TermuxInstallStatus.NOT_INSTALLED,
  val commandHistory: List<String> = emptyList(),
  val showTermuxSetupDialog: Boolean = false,
  val showCopyFeedback: Boolean = false
)

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

  private val shellExecutor = ShellExecutor(application.applicationContext)
  val termuxManager = TermuxManager(application.applicationContext)
  private val clipboardManager =
    application.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

  private val _uiState = MutableStateFlow(TerminalUiState())
  val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

  init {
    val initialDir = shellExecutor.getCurrentWorkingDirectory()
    val termuxStatus = termuxManager.checkStatus()

    val welcomeLines = listOf(
      TerminalLine(
        text = "==================================================",
        type = TerminalLineType.SYSTEM_INFO
      ),
      TerminalLine(
        text = "  KERNEL X — ADVANCED ANDROID SYSTEM CONTROL v1.0",
        type = TerminalLineType.SUCCESS
      ),
      TerminalLine(
        text = "  Architecture: ${System.getProperty("os.arch")} | Sandbox Shell Active",
        type = TerminalLineType.SYSTEM_INFO
      ),
      TerminalLine(
        text = "  Type a command or choose Termux Bridge for full GNU/Linux utilities.",
        type = TerminalLineType.STDOUT
      ),
      TerminalLine(
        text = "==================================================",
        type = TerminalLineType.SYSTEM_INFO
      )
    )

    _uiState.value = _uiState.value.copy(
      lines = welcomeLines,
      currentDirectory = initialDir,
      termuxStatus = termuxStatus
    )

    listenToTermuxCallbacks()
  }

  fun refreshTermuxStatus() {
    _uiState.value = _uiState.value.copy(termuxStatus = termuxManager.checkStatus())
  }

  fun setCommandInput(input: String) {
    _uiState.value = _uiState.value.copy(commandInput = input)
  }

  fun setEngine(engine: TerminalEngine) {
    _uiState.value = _uiState.value.copy(activeEngine = engine)
    refreshTermuxStatus()
    appendLine(
      TerminalLine(
        text = ">> Motor de execução alterado para: ${if (engine == TerminalEngine.INTERNAL_SHELL) "Shell Interno (Sandbox Android)" else "Termux RUN_COMMAND Bridge"}",
        type = TerminalLineType.SYSTEM_INFO
      )
    )
  }

  fun toggleTermuxSetupDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showTermuxSetupDialog = show)
  }

  fun executeCommand(commandToRun: String? = null) {
    val cmd = (commandToRun ?: _uiState.value.commandInput).trim()
    if (cmd.isEmpty() || _uiState.value.isExecuting) return

    val history = _uiState.value.commandHistory.toMutableList()
    if (!history.contains(cmd)) {
      history.add(cmd)
    }

    _uiState.value = _uiState.value.copy(
      commandInput = "",
      isExecuting = true,
      commandHistory = history
    )

    val promptPrefix = if (_uiState.value.activeEngine == TerminalEngine.INTERNAL_SHELL) {
      "kernel-x@android:${_uiState.value.currentDirectory.substringAfterLast('/')}$ "
    } else {
      "termux@bridge:~$ "
    }

    appendLine(
      TerminalLine(
        text = "$promptPrefix$cmd",
        type = TerminalLineType.PROMPT_COMMAND
      )
    )

    viewModelScope.launch {
      if (_uiState.value.activeEngine == TerminalEngine.INTERNAL_SHELL) {
        val result = shellExecutor.execute(cmd)
        _uiState.value = _uiState.value.copy(
          currentDirectory = shellExecutor.getCurrentWorkingDirectory()
        )

        if (result.stdout.isNotEmpty()) {
          appendLine(TerminalLine(text = result.stdout, type = TerminalLineType.STDOUT))
        }

        if (result.stderr.isNotEmpty()) {
          appendLine(TerminalLine(text = result.stderr, type = TerminalLineType.STDERR))
        }

        val statusMsg = if (result.exitCode == 0) {
          "[OK] Executado em ${result.durationMs}ms (exit code 0)"
        } else {
          "[ERRO] Processo finalizado com exit code ${result.exitCode} (${result.durationMs}ms)"
        }

        appendLine(
          TerminalLine(
            text = statusMsg,
            type = if (result.exitCode == 0) TerminalLineType.SUCCESS else TerminalLineType.STDERR
          )
        )
      } else {
        // Termux RUN_COMMAND Bridge
        val dispatchResult = termuxManager.executeInTermux(cmd, runInBackground = true)
        dispatchResult.onSuccess { msg ->
          appendLine(TerminalLine(text = ">> $msg", type = TerminalLineType.SYSTEM_INFO))
          appendLine(
            TerminalLine(
              text = "Aguardando resposta do Termux via PendingIntent callback...",
              type = TerminalLineType.STDOUT
            )
          )
        }.onFailure { err ->
          appendLine(
            TerminalLine(
              text = "Falha ao despachar para o Termux: ${err.message}",
              type = TerminalLineType.STDERR
            )
          )
          appendLine(
            TerminalLine(
              text = "Verifique se o Termux está instalado e se configurou 'allow-external-apps=true'.",
              type = TerminalLineType.SYSTEM_INFO
            )
          )
        }
      }

      _uiState.value = _uiState.value.copy(isExecuting = false)
    }
  }

  fun clearTerminal() {
    _uiState.value = _uiState.value.copy(
      lines = listOf(
        TerminalLine(
          text = "Terminal limpo. KERNEL X pronto.",
          type = TerminalLineType.SYSTEM_INFO
        )
      )
    )
  }

  fun copyOutput() {
    val fullText = _uiState.value.lines.joinToString("\n") { it.text }
    val clip = ClipData.newPlainText("Kernel X Terminal", fullText)
    clipboardManager?.setPrimaryClip(clip)
    _uiState.value = _uiState.value.copy(showCopyFeedback = true)
  }

  fun dismissCopyFeedback() {
    _uiState.value = _uiState.value.copy(showCopyFeedback = false)
  }

  private fun appendLine(line: TerminalLine) {
    val list = _uiState.value.lines.toMutableList()
    list.add(line)
    if (list.size > 500) {
      list.removeAt(0)
    }
    _uiState.value = _uiState.value.copy(lines = list)
  }

  private fun listenToTermuxCallbacks() {
    viewModelScope.launch {
      TermuxResultReceiver.callbackFlow.collect { payload ->
        appendLine(
          TerminalLine(
            text = "--- [TERMUX CALLBACK RECEBIDO] ---",
            type = TerminalLineType.SYSTEM_INFO
          )
        )
        if (payload.stdout.isNotEmpty()) {
          appendLine(TerminalLine(text = payload.stdout, type = TerminalLineType.STDOUT))
        }
        if (payload.stderr.isNotEmpty()) {
          appendLine(TerminalLine(text = payload.stderr, type = TerminalLineType.STDERR))
        }
        if (payload.errMsg.isNotEmpty()) {
          appendLine(
            TerminalLine(
              text = "Erro Termux (code ${payload.errCode}): ${payload.errMsg}",
              type = TerminalLineType.STDERR
            )
          )
        }
        appendLine(
          TerminalLine(
            text = "Termux Exit Code: ${payload.exitCode}",
            type = if (payload.exitCode == 0) TerminalLineType.SUCCESS else TerminalLineType.STDERR
          )
        )
      }
    }
  }
}
