package com.example.model

data class DngMaskCorrection(
    val maskType: MaskType,
    val name: String = "Mask",
    val localExposure: Float = 0.0f,
    val localWarmth: Float = 0.0f,
    val localSaturation: Float = 0.0f,
    val localContrast: Float = 0.0f,
    val localHighlights: Float = 0.0f,
    val localShadows: Float = 0.0f,
    val feather: Float = 50.0f,
    val isInverted: Boolean = false
)

data class CopySettingsFilter(
    val copyLight: Boolean = true,
    val copyColor: Boolean = true,
    val copyHsl: Boolean = true,
    val copyCurves: Boolean = true,
    val copyEffects: Boolean = true,
    val copyDetail: Boolean = true,
    val copyOptics: Boolean = true,
    val copyGeometry: Boolean = false,
    val copyMasking: Boolean = true
)
