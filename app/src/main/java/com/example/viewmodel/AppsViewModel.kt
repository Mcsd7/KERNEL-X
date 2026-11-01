package com.example.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.apps.AppItem
import com.example.data.apps.AppManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppFilter {
  ALL,
  USER,
  SYSTEM
}

data class AppsUiState(
  val allApps: List<AppItem> = emptyList(),
  val filteredApps: List<AppItem> = emptyList(),
  val isLoading: Boolean = true,
  val searchQuery: String = "",
  val activeFilter: AppFilter = AppFilter.ALL,
  val selectedApp: AppItem? = null,
  val appPendingUninstall: AppItem? = null
)

class AppsViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = AppManagerRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(AppsUiState())
  val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

  init {
    loadApps()
  }

  fun loadApps() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      val apps = repository.loadInstalledApps()
      _uiState.value = _uiState.value.copy(
        allApps = apps,
        isLoading = false
      )
      applyFilter()
    }
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
    applyFilter()
  }

  fun setFilter(filter: AppFilter) {
    _uiState.value = _uiState.value.copy(activeFilter = filter)
    applyFilter()
  }

  fun selectApp(app: AppItem?) {
    _uiState.value = _uiState.value.copy(selectedApp = app)
  }

  fun promptUninstall(app: AppItem?) {
    _uiState.value = _uiState.value.copy(appPendingUninstall = app)
  }

  fun getUninstallIntent(packageName: String): Intent {
    return repository.createUninstallIntent(packageName)
  }

  fun launchApp(packageName: String): Boolean {
    return repository.launchApp(packageName)
  }

  fun openAppDetails(packageName: String) {
    repository.openAppDetailsSettings(packageName)
  }

  fun openBatterySettings() {
    repository.openBatterySettings()
  }

  fun openBatteryUsageSettings() {
    repository.openBatteryUsageSettings()
  }

  fun openGeneralSettings() {
    repository.openGeneralAppsSettings()
  }

  private fun applyFilter() {
    val query = _uiState.value.searchQuery.trim().lowercase()
    val filter = _uiState.value.activeFilter
    val list = _uiState.value.allApps.filter { app ->
      val matchesType = when (filter) {
        AppFilter.ALL -> true
        AppFilter.USER -> !app.isSystemApp
        AppFilter.SYSTEM -> app.isSystemApp
      }
      val matchesQuery = query.isEmpty() ||
        app.appName.lowercase().contains(query) ||
        app.packageName.lowercase().contains(query)

      matchesType && matchesQuery
    }
    _uiState.value = _uiState.value.copy(filteredApps = list)
  }
}
