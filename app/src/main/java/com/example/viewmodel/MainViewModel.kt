package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ui.navigation.MainNavigationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MainUiState(
  val currentNavigationItem: MainNavigationItem = MainNavigationItem.HOME,
  val isSettingsOpen: Boolean = false
)

class MainViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(MainUiState())
  val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

  fun navigateTo(item: MainNavigationItem) {
    _uiState.value = _uiState.value.copy(
      currentNavigationItem = item,
      isSettingsOpen = false
    )
  }

  fun openSettings() {
    _uiState.value = _uiState.value.copy(isSettingsOpen = true)
  }

  fun closeSettings() {
    _uiState.value = _uiState.value.copy(isSettingsOpen = false)
  }

  fun handleBack(): Boolean {
    if (_uiState.value.isSettingsOpen) {
      closeSettings()
      return true
    }
    if (_uiState.value.currentNavigationItem != MainNavigationItem.HOME) {
      navigateTo(MainNavigationItem.HOME)
      return true
    }
    return false
  }
}
