package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.apps.AppManagerRepository
import com.example.data.apps.PermissionAppDetail
import com.example.data.apps.PermissionGroupSummary
import com.example.data.apps.SensitivePermissionGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PermissionsUiState(
  val groupSummaries: List<PermissionGroupSummary> = emptyList(),
  val selectedGroup: SensitivePermissionGroup? = null,
  val selectedAppDetail: PermissionAppDetail? = null,
  val isLoading: Boolean = true,
  val searchQuery: String = "",
  val onlyGrantedFilter: Boolean = false
)

class PermissionsViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = AppManagerRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(PermissionsUiState())
  val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

  init {
    loadPermissions()
  }

  fun loadPermissions() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      val allApps = repository.loadInstalledApps()
      val summaries = repository.loadPermissionSummaries(allApps)
      _uiState.value = _uiState.value.copy(
        groupSummaries = summaries,
        isLoading = false
      )
    }
  }

  fun selectGroup(group: SensitivePermissionGroup?) {
    _uiState.value = _uiState.value.copy(selectedGroup = group, searchQuery = "")
  }

  fun selectAppDetail(detail: PermissionAppDetail?) {
    _uiState.value = _uiState.value.copy(selectedAppDetail = detail)
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun toggleOnlyGrantedFilter() {
    _uiState.value = _uiState.value.copy(onlyGrantedFilter = !_uiState.value.onlyGrantedFilter)
  }

  fun openAppDetailsSettings(packageName: String) {
    repository.openAppDetailsSettings(packageName)
  }
}
