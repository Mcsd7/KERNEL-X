package com.example.data.termux

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class TermuxCallbackPayload(
  val stdout: String,
  val stderr: String,
  val exitCode: Int,
  val errCode: Int,
  val errMsg: String,
  val timestamp: Long = System.currentTimeMillis()
)

class TermuxResultReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val stdout = intent.getStringExtra(TermuxConstants.EXTRA_RESULT_STDOUT)
      ?: intent.getStringExtra("com.termux.RUN_COMMAND_RESULT_STDOUT")
      ?: ""

    val stderr = intent.getStringExtra(TermuxConstants.EXTRA_RESULT_STDERR)
      ?: intent.getStringExtra("com.termux.RUN_COMMAND_RESULT_STDERR")
      ?: ""

    val exitCode = intent.getIntExtra(TermuxConstants.EXTRA_RESULT_EXIT_CODE, 0)
    val errCode = intent.getIntExtra(TermuxConstants.EXTRA_RESULT_ERRCODE, 0)
    val errMsg = intent.getStringExtra(TermuxConstants.EXTRA_RESULT_ERRMSG) ?: ""

    val payload = TermuxCallbackPayload(
      stdout = stdout,
      stderr = stderr,
      exitCode = exitCode,
      errCode = errCode,
      errMsg = errMsg
    )

    _callbackFlow.tryEmit(payload)
  }

  companion object {
    private val _callbackFlow = MutableSharedFlow<TermuxCallbackPayload>(extraBufferCapacity = 10)
    val callbackFlow: SharedFlow<TermuxCallbackPayload> = _callbackFlow.asSharedFlow()
  }
}
