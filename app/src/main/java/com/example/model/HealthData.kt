package com.example.model

data class GalaxyHealthSnapshot(
    val heartRateBpm: Int = 74,
    val heartRateStatus: String = "Normal (En reposo)",
    val dailySteps: Int = 8420,
    val stepGoal: Int = 10000,
    val activeCalories: Int = 540,
    val calorieGoal: Int = 650,
    val watchBatteryLevel: Int = 88,
    val phoneBatteryLevel: Int = 92,
    val temperatureCelsius: Int = 23,
    val weatherCondition: String = "Parcialmente Soleado",
    val uvIndex: Int = 4,
    val sleepScore: Int = 88,
    val sleepDurationHours: Float = 7.5f,
    val stressLevel: Int = 28, // 0-100
    val distanceKm: Float = 6.2f,
    val sunsetTime: String = "19:48",
    val sunriseTime: String = "06:32",
    val barometerHpa: Int = 1013,
    val nextEventTitle: String = "Reunión de Diseño",
    val nextEventTime: String = "15:30",
    val worldTimeCity: String = "Tokio",
    val worldTimeDiff: String = "+9h",
    val moonPhaseName: String = "Luna Creciente"
) {
    val stepProgress: Float get() = (dailySteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f)
    val calorieProgress: Float get() = (activeCalories.toFloat() / calorieGoal.toFloat()).coerceIn(0f, 1f)
    val watchBatteryProgress: Float get() = (watchBatteryLevel.toFloat() / 100f).coerceIn(0f, 1f)
    val phoneBatteryProgress: Float get() = (phoneBatteryLevel.toFloat() / 100f).coerceIn(0f, 1f)
    val uvProgress: Float get() = (uvIndex.toFloat() / 11f).coerceIn(0f, 1f)
    val sleepProgress: Float get() = (sleepScore.toFloat() / 100f).coerceIn(0f, 1f)
    val stressProgress: Float get() = (stressLevel.toFloat() / 100f).coerceIn(0f, 1f)
}
