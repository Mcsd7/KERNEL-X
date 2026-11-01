package com.example.data.termux

object TermuxConstants {
  const val TERMUX_PACKAGE_NAME = "com.termux"
  const val TERMUX_APP_NAME = "Termux"

  // Intent Action
  const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"

  // Component
  const val RUN_COMMAND_SERVICE_NAME = "com.termux.app.RunCommandService"

  // Extras
  const val EXTRA_RUN_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
  const val EXTRA_RUN_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
  const val EXTRA_RUN_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
  const val EXTRA_RUN_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
  const val EXTRA_RUN_COMMAND_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"
  const val EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"

  // Session actions
  const val VALUE_SESSION_ACTION_SWITCH_TO = "0"
  const val VALUE_SESSION_ACTION_KEEP_CURRENT = "1"

  // Callback action
  const val ACTION_TERMUX_RESULT = "com.aistudio.kernelx.TERMUX_RESULT"

  // Result Extras returned by Termux
  const val EXTRA_RESULT_STDOUT = "stdout"
  const val EXTRA_RESULT_STDERR = "stderr"
  const val EXTRA_RESULT_EXIT_CODE = "exitCode"
  const val EXTRA_RESULT_ERRCODE = "errCode"
  const val EXTRA_RESULT_ERRMSG = "errmsg"

  // Permission
  const val PERMISSION_RUN_COMMAND = "com.termux.permission.RUN_COMMAND"

  // Default paths in Termux
  const val DEFAULT_BASH_PATH = "/data/data/com.termux/files/usr/bin/bash"
  const val DEFAULT_SH_PATH = "/data/data/com.termux/files/usr/bin/sh"
  const val DEFAULT_WORKDIR = "/data/data/com.termux/files/home"
}
