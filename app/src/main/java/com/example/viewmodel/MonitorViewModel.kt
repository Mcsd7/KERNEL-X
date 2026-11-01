package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.system.SystemMonitorRepository
import com.example.data.system.SystemSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MonitorUiState(
  val snapshot: SystemSnapshot? = null,
  val isLoading: Boolean = true,
  val ramHistory: List<Float> = emptyList(), // Last 20 usage fractions
  val refreshIntervalMillis: Long = 2000L
)

class MonitorViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = SystemMonitorRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(MonitorUiState())
  val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

  private var monitoringJob: Job? = null

  init {
    startMonitoring()
  }

  fun setRefreshInterval(millis: Long) {
    if (_uiState.value.refreshIntervalMillis == millis) return
    _uiState.value = _uiState.value.copy(refreshIntervalMillis = millis)
    startMonitoring()
  }

  fun refreshImmediately() {
    viewModelScope.launch {
      val snapshot = repository.getSnapshot()
      updateWithSnapshot(snapshot)
    }
  }

  private fun startMonitoring() {
    monitoringJob?.cancel()
    monitoringJob = viewModelScope.launch {
      repository.monitorFlow(_uiState.value.refreshIntervalMillis).collect { snapshot ->
        updateWithSnapshot(snapshot)
      }
    }
  }

  private fun updateWithSnapshot(snapshot: SystemSnapshot) {
    val currentHistory = _uiState.value.ramHistory.toMutableList()
    currentHistory.add(snapshot.ram.usedPercent)
    if (currentHistory.size > 24) {
      currentHistory.removeAt(0)
    }
    _uiState.value = _uiState.value.copy(
      snapshot = snapshot,
      isLoading = false,
      ramHistory = currentHistory
    )
  }
}
