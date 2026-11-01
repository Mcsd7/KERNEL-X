package com.example.data.terminal

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class ShellExecutor(private val context: Context) {

  private var currentDirectory: File = context.filesDir

  fun getCurrentWorkingDirectory(): String = currentDirectory.absolutePath

  suspend fun execute(command: String): ExecutionResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    val trimmed = command.trim()

    // Handle internal cd command
    if (trimmed.startsWith("cd ") || trimmed == "cd") {
      val targetDirName = trimmed.removePrefix("cd").trim()
      val targetDir = when {
        targetDirName.isEmpty() || targetDirName == "~" -> context.filesDir
        targetDirName.startsWith("/") -> File(targetDirName)
        else -> File(currentDirectory, targetDirName)
      }

      val canonical = try {
        targetDir.canonicalFile
      } catch (e: Exception) {
        targetDir
      }

      return@withContext if (canonical.exists() && canonical.isDirectory) {
        currentDirectory = canonical
        ExecutionResult(
          command = command,
          stdout = "Working directory changed to: ${canonical.absolutePath}",
          stderr = "",
          exitCode = 0,
          durationMs = System.currentTimeMillis() - startTime
        )
      } else {
        ExecutionResult(
          command = command,
          stdout = "",
          stderr = "cd: no such file or directory: $targetDirName",
          exitCode = 1,
          durationMs = System.currentTimeMillis() - startTime
        )
      }
    }

    try {
      val processBuilder = ProcessBuilder("sh", "-c", command)
      processBuilder.directory(currentDirectory)
      processBuilder.environment().apply {
        put("HOME", context.filesDir.absolutePath)
        put("TMPDIR", context.cacheDir.absolutePath)
        put("PATH", System.getenv("PATH") ?: "/system/bin:/system/xbin")
      }

      val process = processBuilder.start()

      val stdoutLines = StringBuilder()
      val stderrLines = StringBuilder()

      val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
      val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

      // Wait with timeout
      val finished = process.waitFor(15, TimeUnit.SECONDS)

      var line: String?
      while (stdoutReader.readLine().also { line = it } != null) {
        stdoutLines.appendLine(line)
      }

      while (stderrReader.readLine().also { line = it } != null) {
        stderrLines.appendLine(line)
      }

      val exitCode = if (finished) process.exitValue() else {
        process.destroy()
        124 // Timed out
      }

      ExecutionResult(
        command = command,
        stdout = stdoutLines.toString().trimEnd(),
        stderr = if (!finished) "Command timed out after 15 seconds\n" + stderrLines.toString().trimEnd() else stderrLines.toString().trimEnd(),
        exitCode = exitCode,
        durationMs = System.currentTimeMillis() - startTime
      )
    } catch (e: Exception) {
      ExecutionResult(
        command = command,
        stdout = "",
        stderr = "Execution error: ${e.localizedMessage ?: e.message ?: "Unknown error"}",
        exitCode = -1,
        durationMs = System.currentTimeMillis() - startTime
      )
    }
  }
}
