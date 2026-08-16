package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.GalaxyWearableDevice
import com.example.model.SyncProgress
import com.example.model.SyncState
import com.example.model.WatchFaceEntity
import com.example.service.GalaxySyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Estado UI de conexión del reloj Wear OS / Galaxy Watch
 */
sealed interface WatchConnectionUiState {
    data object Checking : WatchConnectionUiState
    data class Disconnected(val reason: String = "No hay relojes vinculados o conectados") : WatchConnectionUiState
    data class Connected(
        val activeDevice: GalaxyWearableDevice,
        val allDevices: List<GalaxyWearableDevice>,
        val isReadyForTransfer: Boolean = true
    ) : WatchConnectionUiState
}

/**
 * Resultado de verificación previa a la transmisión de datos
 */
sealed interface DataTransferEligibility {
    data object Eligible : DataTransferEligibility
    data class Ineligible(val message: String, val canRetry: Boolean = true) : DataTransferEligibility
}

/**
 * ViewModel especializado en la gestión del estado de conexión,
 * verificación previa de vinculación y sincronización de datos con el reloj Wear OS / Samsung Galaxy Watch.
 */
class WatchConnectionViewModel(
    application: Application,
    val syncManager: GalaxySyncManager = GalaxySyncManager(application)
) : AndroidViewModel(application) {

    val devices: StateFlow<List<GalaxyWearableDevice>> = syncManager.devices
    val selectedDeviceId: StateFlow<String> = syncManager.selectedDeviceId
    val syncProgress: StateFlow<SyncProgress> = syncManager.syncProgress

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isCheckingConnection = MutableStateFlow(false)
    val isCheckingConnection: StateFlow<Boolean> = _isCheckingConnection.asStateFlow()

    // Estado reactivo de la conexión
    val connectionState: StateFlow<WatchConnectionUiState> = combine(
        syncManager.devices,
        syncManager.selectedDeviceId,
        _isCheckingConnection
    ) { deviceList, selectedId, isChecking ->
        if (isChecking) {
            WatchConnectionUiState.Checking
        } else if (deviceList.isEmpty()) {
            WatchConnectionUiState.Disconnected("No se encontraron dispositivos vinculados en el sistema.")
        } else {
            val selected = deviceList.find { it.id == selectedId } ?: deviceList.firstOrNull()
            if (selected != null && selected.isConnected) {
                WatchConnectionUiState.Connected(
                    activeDevice = selected,
                    allDevices = deviceList,
                    isReadyForTransfer = true
                )
            } else if (selected != null) {
                WatchConnectionUiState.Disconnected("El reloj '${selected.modelName}' no responde o está desconectado.")
            } else {
                WatchConnectionUiState.Disconnected("Selecciona un reloj Galaxy vinculado.")
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WatchConnectionUiState.Checking
    )

    init {
        refreshConnection()
    }

    /**
     * Fuerza la comprobación y actualización del estado de enlace y Bluetooth/Wear OS Data Layer
     */
    fun refreshConnection() {
        viewModelScope.launch {
            _isCheckingConnection.value = true
            syncManager.refreshPairedDevices()
            kotlinx.coroutines.delay(400)
            _isCheckingConnection.value = false
        }
    }

    /**
     * Selecciona un reloj específico de la lista
     */
    fun selectDevice(deviceId: String) {
        syncManager.selectDevice(deviceId)
    }

    /**
     * Valida si el reloj objetivo está actualmente vinculado y en línea antes de transferir datos
     */
    fun checkTransferEligibility(): DataTransferEligibility {
        val currentDevices = syncManager.devices.value
        val selectedId = syncManager.selectedDeviceId.value

        if (currentDevices.isEmpty()) {
            return DataTransferEligibility.Ineligible(
                "No hay ningún reloj vinculado. Empareja tu Samsung Galaxy Watch en la app Galaxy Wearable."
            )
        }

        val targetDevice = currentDevices.find { it.id == selectedId } ?: currentDevices.first()
        if (!targetDevice.isConnected) {
            return DataTransferEligibility.Ineligible(
                "El dispositivo '${targetDevice.modelName}' se encuentra desconectado. Activa el Bluetooth y abre Galaxy Wearable."
            )
        }

        if (syncProgress.value.state != SyncState.IDLE && 
            syncProgress.value.state != SyncState.SUCCESS && 
            syncProgress.value.state != SyncState.ERROR) {
            return DataTransferEligibility.Ineligible(
                "Ya hay una transferencia en curso (${syncProgress.value.progressPercent}%)."
            )
        }

        return DataTransferEligibility.Eligible
    }

    /**
     * Ejecuta el envío de datos/esfera hacia el reloj previa verificación obligatoria
     */
    fun sendDataToWatch(
        watchFace: WatchFaceEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        when (val eligibility = checkTransferEligibility()) {
            is DataTransferEligibility.Ineligible -> {
                _userMessage.value = eligibility.message
                onError(eligibility.message)
            }
            is DataTransferEligibility.Eligible -> {
                viewModelScope.launch {
                    syncManager.syncWatchFaceToWatch(watchFace).collect { progress ->
                        when (progress.state) {
                            SyncState.SUCCESS -> {
                                _userMessage.value = "✓ Esfera '${watchFace.title}' enviada con éxito al reloj."
                                onSuccess()
                            }
                            SyncState.ERROR -> {
                                val err = progress.errorMessage ?: "Fallo de conexión al transferir datos."
                                _userMessage.value = err
                                onError(err)
                            }
                            else -> {
                                // Sincronizando
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Limpia mensajes de usuario
     */
    fun clearUserMessage() {
        _userMessage.value = null
    }

    /**
     * Accesos directos a utilidades de conexión
     */
    fun openGalaxyWearable() = syncManager.openGalaxyWearableApp()
    fun openUltraPlugin() = syncManager.openGalaxyWatchUltraPlugin()
    fun openAppSettings() = syncManager.openAppPermissionsSettings()
    fun openBluetoothSettings() = syncManager.openBluetoothSettings()
}
