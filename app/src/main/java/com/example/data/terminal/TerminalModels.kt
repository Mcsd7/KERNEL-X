package com.example.data.terminal

enum class TerminalLineType {
  PROMPT_COMMAND,
  STDOUT,
  STDERR,
  SYSTEM_INFO,
  SUCCESS
}

data class TerminalLine(
  val id: Long = System.nanoTime(),
  val text: String,
  val type: TerminalLineType = TerminalLineType.STDOUT,
  val timestamp: Long = System.currentTimeMillis()
)

enum class TerminalEngine {
  INTERNAL_SHELL,
  TERMUX_BRIDGE
}

data class ExecutionResult(
  val command: String,
  val stdout: String,
  val stderr: String,
  val exitCode: Int,
  val durationMs: Long
)
