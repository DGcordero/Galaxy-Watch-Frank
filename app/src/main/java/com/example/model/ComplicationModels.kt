package com.example.model

enum class ComplicationSlot(val displayName: String) {
    TOP("Superior (12h)"),
    BOTTOM("Inferior (6h)"),
    LEFT("Izquierda (9h)"),
    RIGHT("Derecha (3h)"),
    CENTER("Sub-esfera Central")
}

enum class ComplicationType(
    val title: String,
    val iconName: String,
    val defaultUnit: String,
    val category: String
) {
    HEART_RATE("Frecuencia Cardíaca", "favorite", "BPM", "Salud"),
    STEPS("Pasos Diarios", "directions_walk", "pasos", "Salud"),
    CALORIES("Calorías Quemadas", "local_fire_department", "kcal", "Salud"),
    BATTERY_WATCH("Batería Galaxy Watch", "watch", "%", "Sistema"),
    BATTERY_PHONE("Batería S25 Ultra", "smartphone", "%", "Sistema"),
    WEATHER_TEMP("Clima y Temp", "wb_sunny", "°C", "Entorno"),
    UV_INDEX("Índice UV", "wb_iridescent", "UV", "Entorno"),
    SLEEP_SCORE("Puntuación de Sueño", "bedtime", "pts", "Salud"),
    STRESS_LEVEL("Nivel de Estrés", "psychology", "lvl", "Salud"),
    DISTANCE("Distancia Recorrida", "straighten", "km", "Salud"),
    SUNRISE_SUNSET("Amanecer / Ocaso", "wb_twilight", "", "Entorno"),
    NEXT_EVENT("Próximo Evento", "event", "", "Productividad"),
    WORLD_CLOCK("Hora Mundial (UTC)", "public", "", "Tiempo"),
    MOON_PHASE("Fase Lunar", "nightlight_round", "", "Astronomía"),
    BAROMETER("Barómetro", "compress", "hPa", "Sensores"),
    DATE_BADGE("Fecha Actual", "calendar_today", "", "Tiempo"),
    NONE("Ninguno (Vacío)", "block", "", "General")
}

enum class ComplicationDisplayStyle(val title: String) {
    RING_PROGRESS("Anillo de Progreso"),
    VALUE_ONLY("Solo Valor"),
    ICON_AND_VALUE("Icono y Valor"),
    BAR_GAUGE("Barra Gradual"),
    GLOW_BADGE("Insignia Neón")
}

data class ComplicationConfig(
    val type: ComplicationType = ComplicationType.NONE,
    val displayStyle: ComplicationDisplayStyle = ComplicationDisplayStyle.RING_PROGRESS,
    val customColor: Long? = null,
    val label: String = "",
    val goalValue: Float = 100f
)
