package com.example.model

data class ColorThemePreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val primaryColor: Long,
    val accentColor: Long,
    val dialBackgroundColor: Long,
    val handsColor: Long,
    val secondHandColor: Long,
    val subdialColor: Long,
    val glowColor: Long
)

object ColorThemePresetsCatalog {
    val presets = listOf(
        ColorThemePreset(
            id = "galaxy_cyber_neon",
            name = "Galaxy Cyber Neon",
            subtitle = "Cian eléctrico, magenta neón y fondo abisal",
            primaryColor = 0xFF00F0FF,
            accentColor = 0xFFFF0055,
            dialBackgroundColor = 0xFF060913,
            handsColor = 0xFF00F0FF,
            secondHandColor = 0xFFFFE600,
            subdialColor = 0xFF101A2D,
            glowColor = 0xFF00F0FF
        ),
        ColorThemePreset(
            id = "ultra_titanium_orange",
            name = "Ultra Titanium S25",
            subtitle = "Acento naranja Ultra con titanio cepillado",
            primaryColor = 0xFFFF7A00,
            accentColor = 0xFF00D2FF,
            dialBackgroundColor = 0xFF0C1017,
            handsColor = 0xFFE6EDF5,
            secondHandColor = 0xFFFF7A00,
            subdialColor = 0xFF192231,
            glowColor = 0xFFFF7A00
        ),
        ColorThemePreset(
            id = "emerald_stealth_tactical",
            name = "Emerald Tactical Sport",
            subtitle = "Verde esmeralda militar y negro puro AMOLED",
            primaryColor = 0xFF00E676,
            accentColor = 0xFFFFAB00,
            dialBackgroundColor = 0xFF061009,
            handsColor = 0xFFFFFFFF,
            secondHandColor = 0xFF00E676,
            subdialColor = 0xFF112316,
            glowColor = 0xFF00E676
        ),
        ColorThemePreset(
            id = "royal_gold_heritage",
            name = "Royal 18K Gold Luxury",
            subtitle = "Oro pulido, acentos dorados y negro noche",
            primaryColor = 0xFFFFD700,
            accentColor = 0xFFE5A93C,
            dialBackgroundColor = 0xFF08090D,
            handsColor = 0xFFFFEAA7,
            secondHandColor = 0xFF54A0FF,
            subdialColor = 0xFF181C26,
            glowColor = 0xFFFFD700
        ),
        ColorThemePreset(
            id = "deep_ocean_aqua",
            name = "Deep Ocean Diver 300M",
            subtitle = "Azul zafiro marino con aguamarina luminiscente",
            primaryColor = 0xFF00E5FF,
            accentColor = 0xFF18FFFF,
            dialBackgroundColor = 0xFF04111E,
            handsColor = 0xFFF0FBFF,
            secondHandColor = 0xFFFF5252,
            subdialColor = 0xFF0B253D,
            glowColor = 0xFF00E5FF
        ),
        ColorThemePreset(
            id = "crimson_speed_gt",
            name = "Crimson Speed GT",
            subtitle = "Rojo carmesí de carreras y fibra de carbono",
            primaryColor = 0xFFFF1744,
            accentColor = 0xFFFFC400,
            dialBackgroundColor = 0xFF0B0B0D,
            handsColor = 0xFFFFFFFF,
            secondHandColor = 0xFFFF1744,
            subdialColor = 0xFF1A1A22,
            glowColor = 0xFFFF1744
        ),
        ColorThemePreset(
            id = "plasma_violet_nebula",
            name = "Plasma Violet Nebula",
            subtitle = "Violeta interestelar y orquídea brillante",
            primaryColor = 0xFFB026FF,
            accentColor = 0xFF00FFFF,
            dialBackgroundColor = 0xFF0D061A,
            handsColor = 0xFFE1BEE7,
            secondHandColor = 0xFF00E5FF,
            subdialColor = 0xFF21133B,
            glowColor = 0xFFB026FF
        ),
        ColorThemePreset(
            id = "oneui_clean_minimal",
            name = "One UI 7 Monochrome",
            subtitle = "Blanco puro, gris platino y bajo consumo de energía",
            primaryColor = 0xFF8AB4F8,
            accentColor = 0xFFFF8A65,
            dialBackgroundColor = 0xFF000000,
            handsColor = 0xFFF1F3F4,
            secondHandColor = 0xFFFF5252,
            subdialColor = 0xFF14171E,
            glowColor = 0xFF8AB4F8
        )
    )
}
