package com.abrarshakhi.smsman.ui.screens.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupRestoreState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
)

sealed interface BackupRestoreEffect {
    data class LaunchExportPicker(val suggestedName: String) : BackupRestoreEffect
    data object LaunchImportPicker : BackupRestoreEffect
    data class ShowMessage(val text: String) : BackupRestoreEffect
}

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupRestoreState())
    val state: StateFlow<BackupRestoreState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<BackupRestoreEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<BackupRestoreEffect> = _effects.asSharedFlow()

    fun onExportClick() {
        viewModelScope.launch {
            _effects.emit(BackupRestoreEffect.LaunchExportPicker("sms_backup_${System.currentTimeMillis()}.json"))
        }
    }

    fun onImportClick() {
        viewModelScope.launch {
            _effects.emit(BackupRestoreEffect.LaunchImportPicker)
        }
    }

    fun onExportUriSelected(uri: Uri?, contentResolver: android.content.ContentResolver) {
        uri ?: return
        _state.update { it.copy(isExporting = true) }
        viewModelScope.launch {
            runCatching {
                contentResolver.openOutputStream(uri)!!.use { stream ->
                    backupRepository.export(stream)
                }
            }.fold(
                onSuccess = { count ->
                    _effects.emit(BackupRestoreEffect.ShowMessage("Exported $count messages"))
                },
                onFailure = { e ->
                    _effects.emit(BackupRestoreEffect.ShowMessage("Export failed: ${e.message}"))
                },
            )
            _state.update { it.copy(isExporting = false) }
        }
    }

    fun onImportUriSelected(uri: Uri?, contentResolver: android.content.ContentResolver) {
        uri ?: return
        _state.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            runCatching {
                contentResolver.openInputStream(uri)!!.use { stream ->
                    backupRepository.import(stream)
                }
            }.fold(
                onSuccess = { count ->
                    _effects.emit(BackupRestoreEffect.ShowMessage("Imported $count messages"))
                },
                onFailure = { e ->
                    _effects.emit(BackupRestoreEffect.ShowMessage("Import failed: ${e.message}"))
                },
            )
            _state.update { it.copy(isImporting = false) }
        }
    }
}
