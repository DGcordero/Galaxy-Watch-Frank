package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WatchDialType(val displayName: String, val category: String) {
    ANALOG_CHRONO("Cronógrafo Pro", "Analógico"),
    ANALOG_DIVER("Buceo 300M", "Analógico"),
    ANALOG_MINIMAL("Minimalista Bauhaus", "Analógico"),
    ANALOG_LUXURY("Alta Relojería Skeleton", "Lujo"),
    DIGITAL_CYBER("Cyberpunk HUD", "Digital"),
    DIGITAL_RETRO("Retro Digital 80s", "Digital"),
    HYBRID_ULTRA("Ultra Táctico S25", "Híbrido"),
    FITNESS_QUAD("Fitness & Salud Pro", "Deporte")
}

enum class WatchBackgroundPattern(val displayName: String) {
    AMOLED_BLACK("Negro Puro AMOLED"),
    TITANIUM_BRUSHED("Titanio Cepillado"),
    CARBON_FIBER("Fibra de Carbono"),
    RADIAL_SUNBURST("Rayo de Sol Radial"),
    CYBER_GRID("Cuadrícula Neón"),
    CONSTELLATION_NIGHT("Noche Estrellada"),
    DEEP_OCEAN_GRADIENT("Océano Profundo"),
    EMERALD_SPORTS_MESH("Malla Deportiva Esmeralda")
}

enum class WatchHandStyle(val displayName: String, val description: String) {
    SPORT_ARROW("Flechas Deportivas", "Puntas de flecha anchas con lumen"),
    CLASSIC_SWORD("Espadas de Acero", "Diseño cónico estilizado y afilado"),
    DIVER_LUMINOUS("Buceador Super-LumiNova", "Cápsulas geométricas ultrabrillantes"),
    CHRONO_NEEDLE("Agujas de Precisión", "Finas y calibradas al milímetro"),
    MINIMAL_BAR("Barras Minimalistas", "Líneas puras y balanceadas"),
    SKELETON_LUXURY("Esqueleto Calado", "Ventanas recortadas con marco metálico"),
    NEON_BEAM("Láser Neón Luminiscente", "Rayos brillantes con halo de luz"),
    DIGITAL_SEGMENT_ARCS("Arcos Digitales", "Sin manecillas, arcos de segmento")
}

enum class HourMarkerStyle(val displayName: String) {
    NUMBERS_1_12("Números Clásicos (1-12)"),
    PILOT_3_6_9_12("Marcadores Piloto (12, 3, 6, 9)"),
    ROMAN_NUMERALS("Números Romanos (I-XII)"),
    BOLD_INDEX_BARS("Barras de Índice Gruesas"),
    DIVER_GEOMETRIC("Geometría de Buceo (Puntos y Triángulos)"),
    MINIMAL_DOTS("Puntos Sutiles"),
    NONE("Sin Marcadores (Limpio)")
}

enum class BezelStyle(val displayName: String) {
    TACHYMETER("Taquímetro 400-60 km/h"),
    DIVER_60MIN("Bisel de Inmersión 60 Min"),
    COMPASS_ROSE("Brújula Táctica (N-E-S-W)"),
    WORLD_TIME_CITIES("Ciudades Horarias"),
    INNER_SECONDS_TRACK("Pista de 60 Segundos"),
    MINIMAL_RING("Anillo Bisel Delgado"),
    NONE("Bisel Plano Limpio")
}

enum class WatchFontFamily(val displayName: String) {
    GALAXY_SANS("Galaxy One UI Sans"),
    ORBITRON("Orbitron Futurista"),
    ROBOTO_MONO("Roboto Mono Tech"),
    BEBAS_NEUE("Bebas Neue Display"),
    MONTSERRAT("Montserrat Modern"),
    CYBER_LED("Digital LED Segment"),
    PLAYFAIR_SERIF("Classic Luxury Serif")
}

enum class SecondHandMovement(val displayName: String) {
    SWEEP_60FPS("Barrido Continuo Suave (60 FPS)"),
    TICK_1HZ("Salto Mecánico (1 Segundo)"),
    HIDDEN("Ocultar Segundero")
}

@Entity(tableName = "watch_faces")
data class WatchFaceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val author: String = "Galaxy Studio Community",
    val category: String = "Híbrido",
    val dialType: WatchDialType = WatchDialType.HYBRID_ULTRA,
    val backgroundPattern: WatchBackgroundPattern = WatchBackgroundPattern.AMOLED_BLACK,
    
    // Core Colors (ARGB Longs)
    val primaryColor: Long = 0xFF00D2FF, // Cyan
    val accentColor: Long = 0xFFFF7A00,  // Ultra Orange
    val dialBackgroundColor: Long = 0xFF0B0F17, // Deep dark
    val handsColor: Long = 0xFFFFFFFF,
    val secondHandColor: Long = 0xFFFF7A00,
    val subdialColor: Long = 0xFF1E2638,
    val glowColor: Long = 0xFF00D2FF,

    // Hand & Typography configuration
    val handStyle: WatchHandStyle = WatchHandStyle.SPORT_ARROW,
    val secondHandMovement: SecondHandMovement = SecondHandMovement.SWEEP_60FPS,
    val hourMarkerStyle: HourMarkerStyle = HourMarkerStyle.PILOT_3_6_9_12,
    val bezelStyle: BezelStyle = BezelStyle.TACHYMETER,
    val fontFamily: WatchFontFamily = WatchFontFamily.GALAXY_SANS,
    
    // Feature toggles
    val showDateBadge: Boolean = true,
    val showGlowingLume: Boolean = true,
    val aodOptimized: Boolean = true,
    val aodPixelPercentage: Int = 8, // Sub 10% burn-in safe
    
    // Complication Slots (Types stored as strings for ease)
    val complicationTop: String = ComplicationType.BATTERY_WATCH.name,
    val complicationBottom: String = ComplicationType.STEPS.name,
    val complicationLeft: String = ComplicationType.HEART_RATE.name,
    val complicationRight: String = ComplicationType.WEATHER_TEMP.name,
    val complicationCenter: String = ComplicationType.CALORIES.name,
    
    // Meta properties
    val isFavorite: Boolean = false,
    val isCurrentActive: Boolean = false,
    val isCustomUserCreated: Boolean = false,
    val downloadCount: Int = 1240,
    val rating: Float = 4.9f,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
