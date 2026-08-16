package com.example.data

import androidx.room.TypeConverter
import com.example.model.*

class Converters {
    @TypeConverter
    fun fromDialType(value: WatchDialType): String = value.name

    @TypeConverter
    fun toDialType(value: String): WatchDialType = try {
        WatchDialType.valueOf(value)
    } catch (e: Exception) {
        WatchDialType.HYBRID_ULTRA
    }

    @TypeConverter
    fun fromBackgroundPattern(value: WatchBackgroundPattern): String = value.name

    @TypeConverter
    fun toBackgroundPattern(value: String): WatchBackgroundPattern = try {
        WatchBackgroundPattern.valueOf(value)
    } catch (e: Exception) {
        WatchBackgroundPattern.AMOLED_BLACK
    }

    @TypeConverter
    fun fromHandStyle(value: WatchHandStyle): String = value.name

    @TypeConverter
    fun toHandStyle(value: String): WatchHandStyle = try {
        WatchHandStyle.valueOf(value)
    } catch (e: Exception) {
        WatchHandStyle.SPORT_ARROW
    }

    @TypeConverter
    fun fromSecondHandMovement(value: SecondHandMovement): String = value.name

    @TypeConverter
    fun toSecondHandMovement(value: String): SecondHandMovement = try {
        SecondHandMovement.valueOf(value)
    } catch (e: Exception) {
        SecondHandMovement.SWEEP_60FPS
    }

    @TypeConverter
    fun fromHourMarkerStyle(value: HourMarkerStyle): String = value.name

    @TypeConverter
    fun toHourMarkerStyle(value: String): HourMarkerStyle = try {
        HourMarkerStyle.valueOf(value)
    } catch (e: Exception) {
        HourMarkerStyle.PILOT_3_6_9_12
    }

    @TypeConverter
    fun fromBezelStyle(value: BezelStyle): String = value.name

    @TypeConverter
    fun toBezelStyle(value: String): BezelStyle = try {
        BezelStyle.valueOf(value)
    } catch (e: Exception) {
        BezelStyle.TACHYMETER
    }

    @TypeConverter
    fun fromFontFamily(value: WatchFontFamily): String = value.name

    @TypeConverter
    fun toFontFamily(value: String): WatchFontFamily = try {
        WatchFontFamily.valueOf(value)
    } catch (e: Exception) {
        WatchFontFamily.GALAXY_SANS
    }
}
