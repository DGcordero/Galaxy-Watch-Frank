package com.example.model

enum class TemplateCategory(val displayName: String) {
    ALL("Todos"),
    MODERN_FUTURISTIC("Modernos & Futuristas"),
    CLASSIC_LUXURY("Clásicos & Lujo"),
    METALLIC("Metálicos"),
    SPORTS("Deportivos"),
    DIGITAL("Digitales")
}

data class WatchFaceTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: TemplateCategory,
    val tag: String,
    val previewEntity: WatchFaceEntity
)
