package com.devson.nvplayerlite.model

data class TrackInfo(
    val index: Int,
    val label: String,
    val language: String?,
    val selected: Boolean = false,
    val codec: String? = null,
    val isExternal: Boolean = false
) {
    val id: Int get() = index
    val name: String get() = label
}
