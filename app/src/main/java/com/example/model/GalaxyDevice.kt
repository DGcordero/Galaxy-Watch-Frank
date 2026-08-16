package com.example.model

enum class ConnectionStatus {
    CONNECTED,
    SYNCING,
    DISCONNECTED
}

data class GalaxyWearableDevice(
    val id: String,
    val modelName: String,
    val edition: String,
    val isConnected: Boolean,
    val connectionType: String = "Bluetooth 5.3 (BLE Ultra-low latency)",
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val freeStorageGb: Float = 22.4f,
    val totalStorageGb: Float = 32.0f,
    val wearOsVersion: String = "Wear OS 5.0",
    val oneUiVersion: String = "One UI 6 Watch",
    val activeWatchFaceId: String = "preset_ultra_tactical"
)

enum class SyncState {
    IDLE,
    PREPARING_PACKAGE,
    OPTIMIZING_COMPLICATIONS,
    TRANSFERRING_BLE,
    COMPILING_WATCH,
    SUCCESS,
    ERROR
}

data class SyncProgress(
    val state: SyncState = SyncState.IDLE,
    val progressPercent: Int = 0,
    val statusMessage: String = "",
    val errorMessage: String? = null
)

data class SecurityDiagnostics(
    val bleEncryption: String = "AES-128 CCM (Hardware Secured)",
    val handshakeStatus: String = "Autenticado (Samsung Knox & Wear OS)",
    val dataIntegrityProtocol: String = "SHA-256 Checksum Verified",
    val signalRssi: Int = -42, // dBm
    val latencyMs: Int = 11,
    val wffCompatibility: String = "Wear OS 5 / One UI 6 (100% Nativo)",
    val bioactiveSensorAccess: Boolean = true,
    val batteryImpactEstimate: String = "< 0.8% / 24h (OPR < 10% AOD)",
    val isDiagnosticRunning: Boolean = false,
    val lastDiagnosticTimestamp: Long = System.currentTimeMillis()
)

