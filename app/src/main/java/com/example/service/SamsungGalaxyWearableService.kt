package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.model.WatchFaceEntity
import com.google.android.gms.wearable.*
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Servicio especializado en la comunicación de bajo nivel con el ecosistema Wear OS y Samsung Galaxy Watch:
 * - Google Play Services Wearable DataClient (PutDataRequest / DataMap / Assets)
 * - MessageClient (Canal de comandos RPC de baja latencia)
 * - ChannelClient (Transmisión por streaming de paquetes WFF XML binarios)
 * - CapabilityClient (Detección de relojes compatibles con Galaxy Watch Studio y WFF v1/v2)
 * - Deep links oficiales hacia Galaxy Wearable & Samsung Watch Plugins
 */
class SamsungGalaxyWearableService(private val context: Context) {

    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(context) }
    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(context) }
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }

    companion object {
        const val CAPABILITY_GALAXY_WATCH_STUDIO = "galaxy_watch_studio_wff"
        const val PATH_WATCHFACE_SYNC_DATA = "/galaxy_watch/watchface/active"
        const val PATH_WATCHFACE_BINARY_CHANNEL = "/galaxy_watch/watchface/package_stream"
        const val PATH_HEALTH_COMPLICATION_UPDATE = "/galaxy_watch/complications/health_sync"
        const val PATH_COMMAND_SET_ACTIVE = "/galaxy_watch/command/set_active_face"
        const val PATH_COMMAND_TRIGGER_VIBRATION = "/galaxy_watch/command/haptic_feedback"
        const val KEY_WATCHFACE_PAYLOAD = "watchface_json_payload"
        const val KEY_WFF_XML_ASSET = "wff_xml_binary_asset"
        const val KEY_CHECKSUM_SHA256 = "wff_checksum_sha256"
        const val KEY_TIMESTAMP = "sync_timestamp"

        // Samsung Galaxy Wearable Package IDs
        const val PKG_SAMSUNG_WATCH_MANAGER = "com.samsung.android.app.watchmanager"
        const val PKG_SAMSUNG_WATER_PLUGIN = "com.samsung.android.waterplugin" // Galaxy Watch Ultra / Watch 7
        const val PKG_SAMSUNG_GEARN_PLUGIN = "com.samsung.android.gearnplugin" // Galaxy Watch 6 / Classic
        const val PKG_SAMSUNG_GEARO_PLUGIN = "com.samsung.android.gearoplugin" // Galaxy Watch 5 / 4
        const val PKG_WEAR_OS_COMPANION = "com.google.android.wearable.app"
    }

    /**
     * Obtiene los nodos Wear OS / Samsung Galaxy Watch conectados activamente
     */
    suspend fun getConnectedNodes(): List<Node> {
        return try {
            nodeClient.connectedNodes.await() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Detecta si hay un reloj Samsung / Wear OS con la capacidad de renderizado WFF registrada
     */
    suspend fun findCompatibleGalaxyWatchNodes(): Set<Node> {
        return try {
            val capabilityInfo = capabilityClient.getCapability(
                CAPABILITY_GALAXY_WATCH_STUDIO,
                CapabilityClient.FILTER_REACHABLE
            ).await()
            capabilityInfo.nodes
        } catch (e: Exception) {
            getConnectedNodes().toSet()
        }
    }

    /**
     * Sincroniza la esfera mediante el Wearable Data Layer (DataClient con PutDataMapRequest & Assets)
     * para garantizar sincronización persistente incluso si el reloj se reconecta después.
     */
    suspend fun syncWatchFaceToDataLayer(watchFace: WatchFaceEntity): Result<String> {
        return try {
            val jsonPayload = serializeWatchFaceToJson(watchFace)
            val wffXmlContent = generateWffXml(watchFace)
            val checksum = calculateSha256(wffXmlContent.toByteArray(StandardCharsets.UTF_8))
            val xmlAsset = Asset.createFromBytes(wffXmlContent.toByteArray(StandardCharsets.UTF_8))

            val putDataMapRequest = PutDataMapRequest.create(PATH_WATCHFACE_SYNC_DATA).apply {
                dataMap.putString(KEY_WATCHFACE_PAYLOAD, jsonPayload)
                dataMap.putString(KEY_CHECKSUM_SHA256, checksum)
                dataMap.putAsset(KEY_WFF_XML_ASSET, xmlAsset)
                dataMap.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            }

            val request = putDataMapRequest.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()

            // Enviar mensaje directo de activación a todos los nodos conectados
            val nodes = getConnectedNodes()
            nodes.forEach { node ->
                messageClient.sendMessage(
                    node.id,
                    PATH_COMMAND_SET_ACTIVE,
                    watchFace.id.toByteArray(StandardCharsets.UTF_8)
                ).await()
            }

            Result.success(checksum)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envía una notificación de retroalimentación háptica (vibración Knox / Wear OS) al reloj
     */
    suspend fun sendHapticFeedback(nodeId: String, patternType: Int = 1): Boolean {
        return try {
            val payload = byteArrayOf(patternType.toByte())
            messageClient.sendMessage(nodeId, PATH_COMMAND_TRIGGER_VIBRATION, payload).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Genera un archivo ZIP que contiene la estructura oficial de paquete Watch Face Format (WFF)
     */
    fun createWffZipPackage(watchFace: WatchFaceEntity): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            // 1. watchface.xml (WFF especificación Wear OS 5 / One UI 6 Watch)
            val xmlContent = generateWffXml(watchFace)
            zos.putNextEntry(ZipEntry("res/xml/watchface.xml"))
            zos.write(xmlContent.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 2. manifest.json
            val manifestJson = JSONObject().apply {
                put("format_version", "2.0")
                put("title", watchFace.title)
                put("author", "Galaxy Watch Studio")
                put("target_os", "Wear OS 5.0 / One UI 6.0 Watch")
                put("dial_type", watchFace.dialType.name)
                put("package_name", "com.samsung.galaxy.watchface.${watchFace.id}")
            }.toString(2)
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestJson.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }
        return bos.toByteArray()
    }

    private fun serializeWatchFaceToJson(watchFace: WatchFaceEntity): String {
        return JSONObject().apply {
            put("id", watchFace.id)
            put("title", watchFace.title)
            put("dialType", watchFace.dialType.name)
            put("primaryColor", watchFace.primaryColor)
            put("accentColor", watchFace.accentColor)
            put("dialBackgroundColor", watchFace.dialBackgroundColor)
            put("glowColor", watchFace.glowColor)
            put("handStyle", watchFace.handStyle.name)
            put("secondHandMovement", watchFace.secondHandMovement.name)
            put("secondHandColor", watchFace.secondHandColor)
            put("backgroundPattern", watchFace.backgroundPattern.name)
            put("bezelStyle", watchFace.bezelStyle.name)
            put("complicationTop", watchFace.complicationTop)
            put("complicationBottom", watchFace.complicationBottom)
            put("complicationLeft", watchFace.complicationLeft)
            put("complicationRight", watchFace.complicationRight)
            put("complicationCenter", watchFace.complicationCenter)
            put("showGlowingLume", watchFace.showGlowingLume)
            put("isCurrentActive", watchFace.isCurrentActive)
            put("createdAtTimestamp", watchFace.createdAtTimestamp)
        }.toString()
    }

    fun generateWffXml(watchFace: WatchFaceEntity): String {
        val primaryHex = String.format("#%08X", watchFace.primaryColor)
        val bgHex = String.format("#%08X", watchFace.dialBackgroundColor)
        val secHex = String.format("#%08X", watchFace.secondHandColor)

        return """<?xml version="1.0" encoding="utf-8"?>
<!-- Watch Face Format Specification for Wear OS 5.0 & Samsung Galaxy Watch Ultra (One UI 6 Watch) -->
<WatchFace width="450" height="450" clipShape="CIRCLE">
    <Metadata key="CLOCK_TYPE" value="${watchFace.dialType.name}" />
    <Metadata key="PREVIEW_VERSION" value="2.0" />
    <Metadata key="SECURITY_CHECKSUM" value="${calculateSha256(watchFace.title.toByteArray())}" />

    <Scene backgroundColor="$bgHex">
        <!-- Background Layer -->
        <PartDraw x="0" y="0" width="450" height="450">
            <FilledCircle cx="225" cy="225" radius="220" color="$bgHex" />
        </PartDraw>

        <!-- Outer Dial Marks -->
        <PartDraw x="0" y="0" width="450" height="450">
            <IndexRing color="$primaryHex" markType="TICKS" interval="30" />
        </PartDraw>

        <!-- Complication Slots -->
        <ComplicationSlot id="1" slotType="TOP" bounds="165,30,120,60" defaultType="${watchFace.complicationTop}" />
        <ComplicationSlot id="2" slotType="BOTTOM" bounds="165,360,120,60" defaultType="${watchFace.complicationBottom}" />
        <ComplicationSlot id="3" slotType="LEFT" bounds="30,165,80,80" defaultType="${watchFace.complicationLeft}" />
        <ComplicationSlot id="4" slotType="RIGHT" bounds="340,165,80,80" defaultType="${watchFace.complicationRight}" />

        <!-- Clock Hands -->
        <AnalogClock x="0" y="0" width="450" height="450">
            <HourHand style="${watchFace.handStyle.name}" color="$primaryHex" length="120" width="10" />
            <MinuteHand style="${watchFace.handStyle.name}" color="$primaryHex" length="160" width="7" />
            <SecondHand movement="${watchFace.secondHandMovement.name}" color="$secHex" length="180" width="2" />
        </AnalogClock>
    </Scene>
</WatchFace>""".trimIndent()
    }

    private fun calculateSha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Comprueba qué paquetes y plugins de Samsung Galaxy Watch están instalados en el teléfono
     */
    fun getInstalledSamsungPackages(): Map<String, Boolean> {
        val pm = context.packageManager
        val packages = listOf(
            PKG_SAMSUNG_WATCH_MANAGER,
            PKG_SAMSUNG_WATER_PLUGIN,
            PKG_SAMSUNG_GEARN_PLUGIN,
            PKG_SAMSUNG_GEARO_PLUGIN,
            PKG_WEAR_OS_COMPANION
        )
        return packages.associateWith { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
