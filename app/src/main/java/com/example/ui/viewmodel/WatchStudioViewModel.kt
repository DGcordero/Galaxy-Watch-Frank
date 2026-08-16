package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WatchFaceDatabase
import com.example.data.WatchFaceRepository
import com.example.model.*
import com.example.service.GalaxySyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class WatchStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WatchFaceDatabase.getDatabase(application, viewModelScope)
    val repository = WatchFaceRepository(db.watchFaceDao())
    val syncManager = GalaxySyncManager(application)

    // Watch faces list from DB
    val allWatchFaces: StateFlow<List<WatchFaceEntity>> = repository.allWatchFaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteWatchFaces: StateFlow<List<WatchFaceEntity>> = repository.favoriteWatchFaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCreatedWatchFaces: StateFlow<List<WatchFaceEntity>> = repository.userCreatedWatchFaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently edited watch face in Studio
    private val _editingWatchFace = MutableStateFlow<WatchFaceEntity?>(null)
    val editingWatchFace: StateFlow<WatchFaceEntity?> = _editingWatchFace.asStateFlow()

    // Live Health Snapshot for interactive complication simulation
    private val _healthSnapshot = MutableStateFlow(GalaxyHealthSnapshot())
    val healthSnapshot: StateFlow<GalaxyHealthSnapshot> = _healthSnapshot.asStateFlow()

    // UI View mode (Active, AOD, Night red shift)
    private val _viewMode = MutableStateFlow(com.example.ui.components.WatchViewMode.ACTIVE)
    val viewMode: StateFlow<com.example.ui.components.WatchViewMode> = _viewMode.asStateFlow()

    // Active Tab in Editor (0: Base/Estilo, 1: Manecillas, 2: Complicaciones, 3: Colores & Fuentes, 4: Efectos & AOD)
    private val _selectedEditorTab = MutableStateFlow(0)
    val selectedEditorTab: StateFlow<Int> = _selectedEditorTab.asStateFlow()

    // Status snackbar / toast message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Wearable Data Layer connection state
    val connectionStatus: StateFlow<ConnectionStatus> = syncManager.connectionStatus
    val isWearableConnected: StateFlow<Boolean> = syncManager.isWearableConnected

    init {
        viewModelScope.launch {
            allWatchFaces.collect { list ->
                if (_editingWatchFace.value == null && list.isNotEmpty()) {
                    // Pick active or first item
                    _editingWatchFace.value = list.find { it.isCurrentActive } ?: list.first()
                }
            }
        }
    }

    fun setEditingWatchFace(watchFace: WatchFaceEntity) {
        _editingWatchFace.value = watchFace
    }

    fun setEditorTab(tabIndex: Int) {
        _selectedEditorTab.value = tabIndex
    }

    fun setViewMode(mode: com.example.ui.components.WatchViewMode) {
        _viewMode.value = mode
    }

    fun updateEditedWatchFace(transform: (WatchFaceEntity) -> WatchFaceEntity) {
        val current = _editingWatchFace.value ?: return
        val updated = transform(current).copy(
            isCustomUserCreated = true
        )
        _editingWatchFace.value = updated
        viewModelScope.launch {
            repository.saveWatchFace(updated)
        }
    }

    fun createNewWatchFace() {
        val newFace = WatchFaceEntity(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            title = "Nueva Esfera Galaxy S25",
            description = "Diseño creado desde cero con Galaxy Watch Studio.",
            author = "Mi Perfil Galaxy",
            category = "Personalizado",
            dialType = WatchDialType.HYBRID_ULTRA,
            backgroundPattern = WatchBackgroundPattern.AMOLED_BLACK,
            primaryColor = 0xFF00D2FF,
            accentColor = 0xFFFF7A00,
            dialBackgroundColor = 0xFF0B0F17,
            handsColor = 0xFFFFFFFF,
            secondHandColor = 0xFFFF7A00,
            subdialColor = 0xFF1E2638,
            glowColor = 0xFF00D2FF,
            handStyle = WatchHandStyle.SPORT_ARROW,
            secondHandMovement = SecondHandMovement.SWEEP_60FPS,
            hourMarkerStyle = HourMarkerStyle.PILOT_3_6_9_12,
            bezelStyle = BezelStyle.TACHYMETER,
            fontFamily = WatchFontFamily.GALAXY_SANS,
            showDateBadge = true,
            showGlowingLume = true,
            isCustomUserCreated = true,
            createdAtTimestamp = System.currentTimeMillis()
        )
        _editingWatchFace.value = newFace
        viewModelScope.launch {
            repository.saveWatchFace(newFace)
            _userMessage.value = "¡Nueva esfera creada! Personalízala a tu gusto."
        }
    }

    fun duplicateWatchFace(watchFace: WatchFaceEntity) {
        viewModelScope.launch {
            val duplicate = repository.duplicateWatchFace(watchFace)
            _editingWatchFace.value = duplicate
            _userMessage.value = "Esfera duplicada como '${duplicate.title}'"
        }
    }

    fun deleteWatchFace(watchFace: WatchFaceEntity) {
        viewModelScope.launch {
            repository.deleteWatchFace(watchFace)
            val remaining = allWatchFaces.value.filter { it.id != watchFace.id }
            _editingWatchFace.value = remaining.firstOrNull()
            _userMessage.value = "Esfera eliminada"
        }
    }

    fun toggleFavorite(watchFace: WatchFaceEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(watchFace.id, watchFace.isFavorite)
        }
    }

    fun checkWatchConnectionEligibility(): Boolean {
        val currentDevices = syncManager.devices.value
        val selectedId = syncManager.selectedDeviceId.value
        if (currentDevices.isEmpty()) {
            _userMessage.value = "No hay ningún reloj vinculado. Empareja tu Samsung Galaxy Watch en Galaxy Wearable."
            return false
        }
        val target = currentDevices.find { it.id == selectedId } ?: currentDevices.first()
        if (!target.isConnected) {
            _userMessage.value = "El reloj '${target.modelName}' no está conectado. Activa Bluetooth y abre Galaxy Wearable."
            return false
        }
        return true
    }

    fun applyAndSyncActiveWatchFace(watchFace: WatchFaceEntity) {
        if (!checkWatchConnectionEligibility()) return

        viewModelScope.launch {
            repository.setActiveWatchFace(watchFace.id)
            _editingWatchFace.value = watchFace.copy(isCurrentActive = true)
            syncManager.syncWatchFaceToWatch(watchFace).collect { progress ->
                if (progress.state == SyncState.SUCCESS) {
                    _userMessage.value = "✓ Esfera '${watchFace.title}' sincronizada en tu Galaxy Watch"
                } else if (progress.state == SyncState.ERROR) {
                    _userMessage.value = progress.errorMessage ?: "Error al sincronizar"
                }
            }
        }
    }

    fun importFromJson(jsonString: String) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonString)
            if (result.isSuccess) {
                val imported = result.getOrNull()
                if (imported != null) {
                    _editingWatchFace.value = imported
                    _userMessage.value = "¡Esfera '${imported.title}' importada con éxito!"
                }
            } else {
                _userMessage.value = "Error al importar el archivo JSON"
            }
        }
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun updateHealthMetric(type: ComplicationType, delta: Int) {
        _healthSnapshot.update { current ->
            when (type) {
                ComplicationType.HEART_RATE -> current.copy(heartRateBpm = (current.heartRateBpm + delta).coerceIn(45, 190))
                ComplicationType.STEPS -> current.copy(dailySteps = (current.dailySteps + delta * 250).coerceIn(0, 30000))
                ComplicationType.CALORIES -> current.copy(activeCalories = (current.activeCalories + delta * 50).coerceIn(0, 2000))
                ComplicationType.BATTERY_WATCH -> current.copy(watchBatteryLevel = (current.watchBatteryLevel + delta).coerceIn(5, 100))
                ComplicationType.WEATHER_TEMP -> current.copy(temperatureCelsius = (current.temperatureCelsius + delta).coerceIn(-10, 45))
                ComplicationType.STRESS_LEVEL -> current.copy(stressLevel = (current.stressLevel + delta).coerceIn(0, 100))
                ComplicationType.SLEEP_SCORE -> current.copy(sleepScore = (current.sleepScore + delta).coerceIn(20, 100))
                else -> current
            }
        }
    }
}
