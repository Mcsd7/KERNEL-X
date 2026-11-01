package com.example.data.termux

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat

enum class TermuxInstallStatus {
  INSTALLED_READY,
  INSTALLED_PERMISSION_REQUIRED,
  NOT_INSTALLED
}

class TermuxManager(private val context: Context) {

  private val packageManager: PackageManager = context.packageManager

  fun checkStatus(): TermuxInstallStatus {
    val isInstalled = try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(
          TermuxConstants.TERMUX_PACKAGE_NAME,
          PackageManager.PackageInfoFlags.of(0)
        )
      } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(TermuxConstants.TERMUX_PACKAGE_NAME, 0)
      }
      true
    } catch (e: Exception) {
      false
    }

    if (!isInstalled) {
      return TermuxInstallStatus.NOT_INSTALLED
    }

    val permissionGranted = ContextCompat.checkSelfPermission(
      context,
      TermuxConstants.PERMISSION_RUN_COMMAND
    ) == PackageManager.PERMISSION_GRANTED

    return if (permissionGranted) {
      TermuxInstallStatus.INSTALLED_READY
    } else {
      TermuxInstallStatus.INSTALLED_PERMISSION_REQUIRED
    }
  }

  fun executeInTermux(
    command: String,
    runInBackground: Boolean = true
  ): Result<String> {
    val status = checkStatus()
    if (status == TermuxInstallStatus.NOT_INSTALLED) {
      return Result.failure(IllegalStateException("O aplicativo Termux não está instalado no dispositivo."))
    }

    return try {
      val intent = Intent(TermuxConstants.ACTION_RUN_COMMAND).apply {
        component = ComponentName(
          TermuxConstants.TERMUX_PACKAGE_NAME,
          TermuxConstants.RUN_COMMAND_SERVICE_NAME
        )
        putExtra(TermuxConstants.EXTRA_RUN_COMMAND_PATH, TermuxConstants.DEFAULT_BASH_PATH)
        putExtra(TermuxConstants.EXTRA_RUN_COMMAND_ARGUMENTS, arrayOf("-c", command))
        putExtra(TermuxConstants.EXTRA_RUN_COMMAND_WORKDIR, TermuxConstants.DEFAULT_WORKDIR)
        putExtra(TermuxConstants.EXTRA_RUN_COMMAND_BACKGROUND, runInBackground)
        putExtra(
          TermuxConstants.EXTRA_RUN_COMMAND_SESSION_ACTION,
          if (runInBackground) TermuxConstants.VALUE_SESSION_ACTION_KEEP_CURRENT else TermuxConstants.VALUE_SESSION_ACTION_SWITCH_TO
        )

        // PendingIntent callback
        val callbackIntent = Intent(context, TermuxResultReceiver::class.java).apply {
          action = TermuxConstants.ACTION_TERMUX_RESULT
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
          PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          0,
          callbackIntent,
          flags
        )
        putExtra(TermuxConstants.EXTRA_PENDING_INTENT, pendingIntent)
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && runInBackground) {
        try {
          context.startService(intent)
        } catch (e: Exception) {
          // If background service start fails due to Android background limitations,
          // launch foreground activity in Termux or startForegroundService
          context.startForegroundService(intent)
        }
      } else {
        context.startService(intent)
      }

      Result.success("Comando despachado com sucesso para o Termux via RUN_COMMAND.")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun openTermuxInstallationPage() {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/com.termux/")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/termux/termux-app/releases")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(fallback)
    }
  }

  fun openTermuxApp(): Boolean {
    return try {
      val intent = packageManager.getLaunchIntentForPackage(TermuxConstants.TERMUX_PACKAGE_NAME)
      if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
      } else false
    } catch (e: Exception) {
      false
    }
  }

  fun getSetupInstructions(): List<String> = listOf(
    "1. Certifique-se de que o Termux oficial (F-Droid ou GitHub) está instalado.",
    "2. Abra o Termux e digite o seguinte comando para permitir a execução de apps externos:\n\n" +
      "echo \"allow-external-apps = true\" >> ~/.termux/termux.properties && termux-reload-settings",
    "3. Conceda a permissão 'com.termux.permission.RUN_COMMAND' nas configurações se solicitada.",
    "4. Seus comandos do KERNEL X serão executados com acesso total ao ambiente do Termux."
  )
}
