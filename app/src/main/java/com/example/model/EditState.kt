package com.example.model

enum class CurveChannel(val label: String) {
    RGB("RGB"),
    RED("Red"),
    GREEN("Green"),
    BLUE("Blue")
}

enum class BokehShape(val label: String) {
    CIRCULAR("Circular"),
    HEXAGONAL("Hexagonal"),
    HEART("Heart")
}

enum class HealingMode(val label: String) {
    SPOT_HEAL("Spot Heal"),
    CLONE("Clone Stamp"),
    GENERATIVE_REMOVE("AI Generative Remove")
}

data class HistoryItem(
    val id: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val state: EditState
)

data class EditState(
    val photoId: String = "",
    val version: Int = 1,
    
    // MOD-005 Light Engine
    val exposure: Float = 0.0f,     // -5.0 to +5.0 EV
    val contrast: Float = 0.0f,     // -100 to +100
    val highlights: Float = 0.0f,   // -100 to +100
    val shadows: Float = 0.0f,      // -100 to +100
    val whites: Float = 0.0f,       // -100 to +100
    val blacks: Float = 0.0f,       // -100 to +100
    val isHdrToneMapped: Boolean = false, // MOD-018 HDR Engine
    
    // MOD-006 Color Engine: WB & Global
    val temperature: Float = 0.0f,  // -100 (Cool) to +100 (Warm)
    val tint: Float = 0.0f,         // -100 (Green) to +100 (Magenta)
    val vibrance: Float = 0.0f,     // -100 to +100
    val saturation: Float = 0.0f,   // -100 to +100
    
    // HSL 8 Channels: Hue, Saturation, Luminance shifts (-100 to +100)
    val hslRedHue: Float = 0f, val hslRedSat: Float = 0f, val hslRedLum: Float = 0f,
    val hslOrangeHue: Float = 0f, val hslOrangeSat: Float = 0f, val hslOrangeLum: Float = 0f,
    val hslYellowHue: Float = 0f, val hslYellowSat: Float = 0f, val hslYellowLum: Float = 0f,
    val hslGreenHue: Float = 0f, val hslGreenSat: Float = 0f, val hslGreenLum: Float = 0f,
    val hslAquaHue: Float = 0f, val hslAquaSat: Float = 0f, val hslAquaLum: Float = 0f,
    val hslBlueHue: Float = 0f, val hslBlueSat: Float = 0f, val hslBlueLum: Float = 0f,
    val hslPurpleHue: Float = 0f, val hslPurpleSat: Float = 0f, val hslPurpleLum: Float = 0f,
    val hslMagentaHue: Float = 0f, val hslMagentaSat: Float = 0f, val hslMagentaLum: Float = 0f,
    
    // Color Grading (Split Toning): Shadows, Midtones, Highlights
    val gradeShadowHue: Float = 0f,     // 0..360
    val gradeShadowSat: Float = 0f,     // 0..100
    val gradeMidtoneHue: Float = 0f,    // 0..360
    val gradeMidtoneSat: Float = 0f,    // 0..100
    val gradeHighlightHue: Float = 0f,  // 0..360
    val gradeHighlightSat: Float = 0f,  // 0..100
    val gradeBlending: Float = 50f,     // 0..100
    val gradeBalance: Float = 0f,       // -100 to +100
    
    // MOD-007 Tone Curves (0.0 to 1.0 points: blacks, shadows, mid, highlights, whites)
    val activeCurveChannel: CurveChannel = CurveChannel.RGB,
    val curveBlackPoint: Float = 0.0f,
    val curveShadowPoint: Float = 0.25f,
    val curveMidPoint: Float = 0.5f,
    val curveHighlightPoint: Float = 0.75f,
    val curveWhitePoint: Float = 1.0f,
    // Red Channel curve
    val curveRedBlackPoint: Float = 0.0f,
    val curveRedShadowPoint: Float = 0.25f,
    val curveRedMidPoint: Float = 0.5f,
    val curveRedHighlightPoint: Float = 0.75f,
    val curveRedWhitePoint: Float = 1.0f,
    // Green Channel curve
    val curveGreenBlackPoint: Float = 0.0f,
    val curveGreenShadowPoint: Float = 0.25f,
    val curveGreenMidPoint: Float = 0.5f,
    val curveGreenHighlightPoint: Float = 0.75f,
    val curveGreenWhitePoint: Float = 1.0f,
    // Blue Channel curve
    val curveBlueBlackPoint: Float = 0.0f,
    val curveBlueShadowPoint: Float = 0.25f,
    val curveBlueMidPoint: Float = 0.5f,
    val curveBlueHighlightPoint: Float = 0.75f,
    val curveBlueWhitePoint: Float = 1.0f,
    
    // MOD-008 Effects Engine
    val texture: Float = 0.0f,          // -100 to +100
    val clarity: Float = 0.0f,          // -100 to +100
    val dehaze: Float = 0.0f,           // -100 to +100
    val vignette: Float = 0.0f,         // -100 (Dark) to +100 (Light)
    val vignetteMidpoint: Float = 50f,  // 0 to 100
    val vignetteFeather: Float = 50f,   // 0 to 100
    val grainAmount: Float = 0.0f,      // 0 to 100
    val grainSize: Float = 25.0f,       // 0 to 100
    val grainRoughness: Float = 50.0f,  // 0 to 100
    
    // MOD-009 Detail Engine
    val sharpening: Float = 0.0f,       // 0 to 100
    val sharpeningRadius: Float = 1.0f, // 0.5 to 3.0
    val sharpeningDetail: Float = 25f,  // 0 to 100
    val sharpeningMasking: Float = 0f,  // 0 to 100
    val noiseReduction: Float = 0.0f,   // 0 to 100
    val colorNoiseReduction: Float = 0.0f, // 0 to 100
    val isAiDenoiseApplied: Boolean = false,
    
    // MOD-010 Optics Engine
    val distortion: Float = 0.0f,       // -100 to +100
    val chromaticAberration: Boolean = false,
    val lensVignetteCorrection: Float = 0.0f, // 0 to 100
    
    // MOD-011 Crop & Geometry
    val rotationDegrees: Int = 0,       // 0, 90, 180, 270
    val straightenAngle: Float = 0.0f,  // -45 to +45 deg
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val perspectiveVertical: Float = 0.0f,   // -100 to +100
    val perspectiveHorizontal: Float = 0.0f, // -100 to +100
    val aspectRatio: AspectRatioType = AspectRatioType.ORIGINAL,
    
    // MOD-012 Masking Engine
    val activeMaskType: MaskType = MaskType.NONE,
    val maskBrushRadius: Float = 30.0f,
    val maskExposure: Float = 0.0f,
    val maskContrast: Float = 0.0f,
    val maskSaturation: Float = 0.0f,
    val maskWarmth: Float = 0.0f,
    val maskClarity: Float = 0.0f,
    val maskFeather: Float = 50.0f,
    val maskOpacity: Float = 100.0f,
    val isMaskInverted: Boolean = false,
    val dngMasks: List<DngMaskCorrection> = emptyList(),
    
    // MOD-013 Healing Engine
    val healingMode: HealingMode = HealingMode.SPOT_HEAL,
    val healingBrushSize: Float = 25f,
    val healingApplied: Boolean = false,
    
    // MOD-015 Portrait Retouching
    val skinSmooth: Float = 0.0f,
    val eyeBrighten: Float = 0.0f,
    val teethWhiten: Float = 0.0f,
    val faceLighting: Float = 0.0f,     // 0 to 100
    val lipTint: Float = 0.0f,          // 0 to 100
    
    // MOD-017 Lens Blur Engine
    val lensBlurAmount: Float = 0.0f,   // 0 to 100
    val bokehShape: BokehShape = BokehShape.CIRCULAR,
    val bokehIntensity: Float = 50.0f,
    
    // MOD-016 Active Preset
    val appliedPresetName: String? = null,
    val presetAmount: Float = 100.0f    // 0 to 200%
) {
    fun isModified(): Boolean {
        return exposure != 0f || contrast != 0f || highlights != 0f || shadows != 0f ||
                whites != 0f || blacks != 0f || temperature != 0f || tint != 0f ||
                vibrance != 0f || saturation != 0f || texture != 0f || clarity != 0f ||
                dehaze != 0f || vignette != 0f || grainAmount != 0f || sharpening != 0f ||
                noiseReduction != 0f || rotationDegrees != 0 || straightenAngle != 0f ||
                flipHorizontal || flipVertical || perspectiveVertical != 0f || perspectiveHorizontal != 0f ||
                skinSmooth != 0f || faceLighting != 0f || lipTint != 0f || lensBlurAmount != 0f ||
                activeMaskType != MaskType.NONE || gradeShadowSat != 0f || gradeHighlightSat != 0f ||
                isHdrToneMapped || distortion != 0f
    }
}

enum class AspectRatioType(val label: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    FREE("Free", null),
    SQUARE_1_1("1:1", 1.0f),
    PORTRAIT_4_5("4:5", 0.8f),
    PORTRAIT_3_4("3:4", 0.75f),
    CLASSIC_3_2("3:2", 1.5f),
    WIDE_16_9("16:9", 1.777f),
    STORY_9_16("9:16", 0.5625f)
}

enum class MaskType(val label: String) {
    NONE("None"),
    SUBJECT("AI Subject"),
    SKY("AI Sky"),
    BACKGROUND("AI Background"),
    PEOPLE("AI People"),
    BRUSH("Custom Brush"),
    LINEAR_GRADIENT("Linear Gradient"),
    RADIAL_GRADIENT("Radial Gradient")
}
