package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SamplePhotoRepository
import com.example.model.AspectRatioType
import com.example.model.BokehShape
import com.example.model.CurveChannel
import com.example.model.EditState
import com.example.model.HealingMode
import com.example.model.MaskType
import com.example.model.PresetItem
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.EditorTab

@Composable
fun AdjustmentToolPanel(
    tab: EditorTab,
    editState: EditState,
    isProcessing: Boolean,
    progress: Float,
    customPresets: List<PresetItem>,
    onUpdateEdit: ((EditState) -> EditState) -> Unit,
    onRunAiDenoise: () -> Unit,
    onSaveCustomPreset: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        when (tab) {
            EditorTab.LIGHT -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("HDR Tonemapping (Local Tone Map)", color = TextSecondary, fontSize = 11.sp)
                    Switch(
                        checked = editState.isHdrToneMapped,
                        onCheckedChange = { onUpdateEdit { s -> s.copy(isHdrToneMapped = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                    )
                }

                PrecisionSlider(
                    label = "Exposure",
                    value = editState.exposure,
                    valueRange = -5f..5f,
                    onValueChange = { v -> onUpdateEdit { it.copy(exposure = v) } }
                )
                PrecisionSlider(
                    label = "Contrast",
                    value = editState.contrast,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(contrast = v) } }
                )
                PrecisionSlider(
                    label = "Highlights",
                    value = editState.highlights,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(highlights = v) } }
                )
                PrecisionSlider(
                    label = "Shadows",
                    value = editState.shadows,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(shadows = v) } }
                )
                PrecisionSlider(
                    label = "Whites",
                    value = editState.whites,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(whites = v) } }
                )
                PrecisionSlider(
                    label = "Blacks",
                    value = editState.blacks,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(blacks = v) } }
                )
            }

            EditorTab.COLOR -> {
                PrecisionSlider(
                    label = "Temp (Warmth)",
                    value = editState.temperature,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(temperature = v) } }
                )
                PrecisionSlider(
                    label = "Tint (Green/Magenta)",
                    value = editState.tint,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(tint = v) } }
                )
                PrecisionSlider(
                    label = "Vibrance",
                    value = editState.vibrance,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(vibrance = v) } }
                )
                PrecisionSlider(
                    label = "Saturation",
                    value = editState.saturation,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(saturation = v) } }
                )

                Spacer(modifier = Modifier.height(10.dp))
                // 3-Way Split Toning / Color Grading
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("3-WAY COLOR GRADING (SPLIT TONING)", color = AccentCyan, fontSize = 11.sp)
                        PrecisionSlider(
                            label = "Shadows Hue",
                            value = editState.gradeShadowHue,
                            valueRange = 0f..360f,
                            onValueChange = { v -> onUpdateEdit { it.copy(gradeShadowHue = v) } }
                        )
                        PrecisionSlider(
                            label = "Shadows Saturation",
                            value = editState.gradeShadowSat,
                            valueRange = 0f..100f,
                            onValueChange = { v -> onUpdateEdit { it.copy(gradeShadowSat = v) } }
                        )
                        PrecisionSlider(
                            label = "Highlights Hue",
                            value = editState.gradeHighlightHue,
                            valueRange = 0f..360f,
                            onValueChange = { v -> onUpdateEdit { it.copy(gradeHighlightHue = v) } }
                        )
                        PrecisionSlider(
                            label = "Highlights Saturation",
                            value = editState.gradeHighlightSat,
                            valueRange = 0f..100f,
                            onValueChange = { v -> onUpdateEdit { it.copy(gradeHighlightSat = v) } }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HslColorMixer(
                    editState = editState,
                    onChannelChange = { channel, hue, sat, lum ->
                        onUpdateEdit { state ->
                            when (channel) {
                                HslChannel.RED -> state.copy(hslRedHue = hue, hslRedSat = sat, hslRedLum = lum)
                                HslChannel.ORANGE -> state.copy(hslOrangeHue = hue, hslOrangeSat = sat, hslOrangeLum = lum)
                                HslChannel.YELLOW -> state.copy(hslYellowHue = hue, hslYellowSat = sat, hslYellowLum = lum)
                                HslChannel.GREEN -> state.copy(hslGreenHue = hue, hslGreenSat = sat, hslGreenLum = lum)
                                HslChannel.AQUA -> state.copy(hslAquaHue = hue, hslAquaSat = sat, hslAquaLum = lum)
                                HslChannel.BLUE -> state.copy(hslBlueHue = hue, hslBlueSat = sat, hslBlueLum = lum)
                                HslChannel.PURPLE -> state.copy(hslPurpleHue = hue, hslPurpleSat = sat, hslPurpleLum = lum)
                                HslChannel.MAGENTA -> state.copy(hslMagentaHue = hue, hslMagentaSat = sat, hslMagentaLum = lum)
                            }
                        }
                    }
                )
            }

            EditorTab.CURVES -> {
                ToneCurveEditor(
                    editState = editState,
                    onCurveChanged = { channel, b, s, m, h, w ->
                        onUpdateEdit {
                            when (channel) {
                                CurveChannel.RGB -> it.copy(curveBlackPoint = b, curveShadowPoint = s, curveMidPoint = m, curveHighlightPoint = h, curveWhitePoint = w)
                                CurveChannel.RED -> it.copy(curveRedBlackPoint = b, curveRedShadowPoint = s, curveRedMidPoint = m, curveRedHighlightPoint = h, curveRedWhitePoint = w)
                                CurveChannel.GREEN -> it.copy(curveGreenBlackPoint = b, curveGreenShadowPoint = s, curveGreenMidPoint = m, curveGreenHighlightPoint = h, curveGreenWhitePoint = w)
                                CurveChannel.BLUE -> it.copy(curveBlueBlackPoint = b, curveBlueShadowPoint = s, curveBlueMidPoint = m, curveBlueHighlightPoint = h, curveBlueWhitePoint = w)
                            }
                        }
                    }
                )
            }

            EditorTab.EFFECTS -> {
                PrecisionSlider(
                    label = "Texture",
                    value = editState.texture,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(texture = v) } }
                )
                PrecisionSlider(
                    label = "Clarity",
                    value = editState.clarity,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(clarity = v) } }
                )
                PrecisionSlider(
                    label = "Dehaze",
                    value = editState.dehaze,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(dehaze = v) } }
                )
                PrecisionSlider(
                    label = "Vignette Amount",
                    value = editState.vignette,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(vignette = v) } }
                )
                PrecisionSlider(
                    label = "Vignette Midpoint",
                    value = editState.vignetteMidpoint,
                    valueRange = 0f..100f,
                    defaultValue = 50f,
                    onValueChange = { v -> onUpdateEdit { it.copy(vignetteMidpoint = v) } }
                )
                PrecisionSlider(
                    label = "Film Grain Amount",
                    value = editState.grainAmount,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(grainAmount = v) } }
                )
                PrecisionSlider(
                    label = "Film Grain Size",
                    value = editState.grainSize,
                    valueRange = 0f..100f,
                    defaultValue = 25f,
                    onValueChange = { v -> onUpdateEdit { it.copy(grainSize = v) } }
                )
            }

            EditorTab.DETAIL -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AI Denoise (Neural Noise Extraction)", color = AccentCyan, fontSize = 11.sp)
                            Button(
                                onClick = onRunAiDenoise,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                            ) {
                                Text(if (editState.isAiDenoiseApplied) "Applied" else "Run AI Denoise", color = Color.Black, fontSize = 10.sp)
                            }
                        }
                        if (isProcessing && progress > 0f) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                color = AccentCyan,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                PrecisionSlider(
                    label = "Sharpening",
                    value = editState.sharpening,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(sharpening = v) } }
                )
                PrecisionSlider(
                    label = "Sharpening Radius",
                    value = editState.sharpeningRadius,
                    valueRange = 0.5f..3.0f,
                    defaultValue = 1.0f,
                    onValueChange = { v -> onUpdateEdit { it.copy(sharpeningRadius = v) } }
                )
                PrecisionSlider(
                    label = "Noise Reduction",
                    value = editState.noiseReduction,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(noiseReduction = v) } }
                )
                PrecisionSlider(
                    label = "Color Noise Reduction",
                    value = editState.colorNoiseReduction,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(colorNoiseReduction = v) } }
                )
            }

            EditorTab.OPTICS -> {
                Text("Lens & Optics Correction Engine", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remove Chromatic Aberration", color = TextPrimary, fontSize = 12.sp)
                    Switch(
                        checked = editState.chromaticAberration,
                        onCheckedChange = { onUpdateEdit { s -> s.copy(chromaticAberration = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                    )
                }
                PrecisionSlider(
                    label = "Lens Distortion (Barrel/Pincushion)",
                    value = editState.distortion,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(distortion = v) } }
                )
                PrecisionSlider(
                    label = "Lens Vignetting Defringe",
                    value = editState.lensVignetteCorrection,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(lensVignetteCorrection = v) } }
                )
            }

            EditorTab.CROP -> {
                Text("Aspect Ratio", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioType.values().forEach { ar ->
                        FilterChip(
                            selected = editState.aspectRatio == ar,
                            onClick = { onUpdateEdit { it.copy(aspectRatio = ar) } },
                            label = { Text(ar.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onUpdateEdit { it.copy(rotationDegrees = (it.rotationDegrees + 90) % 360) } },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rotate 90°", color = TextPrimary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onUpdateEdit { it.copy(flipHorizontal = !it.flipHorizontal) } },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Flip H", color = TextPrimary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onUpdateEdit { it.copy(flipVertical = !it.flipVertical) } },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Flip V", color = TextPrimary, fontSize = 11.sp)
                    }
                }

                PrecisionSlider(
                    label = "Straighten Angle",
                    value = editState.straightenAngle,
                    valueRange = -45f..45f,
                    onValueChange = { v -> onUpdateEdit { it.copy(straightenAngle = v) } }
                )
                PrecisionSlider(
                    label = "Perspective Vertical Skew",
                    value = editState.perspectiveVertical,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(perspectiveVertical = v) } }
                )
                PrecisionSlider(
                    label = "Perspective Horizontal Skew",
                    value = editState.perspectiveHorizontal,
                    valueRange = -100f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(perspectiveHorizontal = v) } }
                )
            }

            EditorTab.MASKING -> {
                Text("Selective AI & Geometry Masking", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Imported DNG Masks Section (if DNG recipe has masks)
                if (editState.dngMasks.isNotEmpty()) {
                    Text(
                        "Imported Adobe DNG Masks (${editState.dngMasks.size})",
                        color = AccentAmber,
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        editState.dngMasks.forEachIndexed { _, dngMask ->
                            val isSelected = editState.activeMaskType == dngMask.maskType
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onUpdateEdit {
                                        it.copy(
                                            activeMaskType = dngMask.maskType,
                                            maskExposure = dngMask.localExposure,
                                            maskWarmth = dngMask.localWarmth,
                                            maskSaturation = dngMask.localSaturation,
                                            maskContrast = dngMask.localContrast,
                                            maskClarity = dngMask.localHighlights
                                        )
                                    }
                                },
                                label = { Text(dngMask.name, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentAmber,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaskType.values().forEach { mask ->
                        FilterChip(
                            selected = editState.activeMaskType == mask,
                            onClick = { onUpdateEdit { it.copy(activeMaskType = mask) } },
                            label = { Text(mask.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                if (editState.activeMaskType != MaskType.NONE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invert Mask Selection", color = TextPrimary, fontSize = 12.sp)
                                Switch(
                                    checked = editState.isMaskInverted,
                                    onCheckedChange = { onUpdateEdit { s -> s.copy(isMaskInverted = it) } },
                                    colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                                )
                            }
                            PrecisionSlider(
                                label = "Mask Feathering",
                                value = editState.maskFeather,
                                valueRange = 0f..100f,
                                defaultValue = 50f,
                                onValueChange = { v -> onUpdateEdit { it.copy(maskFeather = v) } }
                            )
                            PrecisionSlider(
                                label = "Mask Exposure",
                                value = editState.maskExposure,
                                valueRange = -3f..3f,
                                onValueChange = { v -> onUpdateEdit { it.copy(maskExposure = v) } }
                            )
                            PrecisionSlider(
                                label = "Mask Contrast",
                                value = editState.maskContrast,
                                valueRange = -100f..100f,
                                onValueChange = { v -> onUpdateEdit { it.copy(maskContrast = v) } }
                            )
                            PrecisionSlider(
                                label = "Mask Warmth",
                                value = editState.maskWarmth,
                                valueRange = -100f..100f,
                                onValueChange = { v -> onUpdateEdit { it.copy(maskWarmth = v) } }
                            )
                            PrecisionSlider(
                                label = "Mask Saturation",
                                value = editState.maskSaturation,
                                valueRange = -100f..100f,
                                onValueChange = { v -> onUpdateEdit { it.copy(maskSaturation = v) } }
                            )
                            PrecisionSlider(
                                label = "Mask Clarity",
                                value = editState.maskClarity,
                                valueRange = -100f..100f,
                                onValueChange = { v -> onUpdateEdit { it.copy(maskClarity = v) } }
                            )
                        }
                    }
                }
            }

            EditorTab.HEALING -> {
                Text("Healing & Inpainting Engine", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HealingMode.values().forEach { mode ->
                        FilterChip(
                            selected = editState.healingMode == mode,
                            onClick = { onUpdateEdit { it.copy(healingMode = mode) } },
                            label = { Text(mode.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                PrecisionSlider(
                    label = "Healing Brush Size",
                    value = editState.healingBrushSize,
                    valueRange = 5f..100f,
                    defaultValue = 25f,
                    onValueChange = { v -> onUpdateEdit { it.copy(healingBrushSize = v) } }
                )
                Button(
                    onClick = { onUpdateEdit { it.copy(healingApplied = true) } },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Execute Inpainting on Selection", color = Color.Black, fontSize = 11.sp)
                }
            }

            EditorTab.PORTRAIT -> {
                Text("Portrait & Face Retouching Engine", color = TextSecondary, fontSize = 12.sp)
                PrecisionSlider(
                    label = "Skin Smoothing",
                    value = editState.skinSmooth,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(skinSmooth = v) } }
                )
                PrecisionSlider(
                    label = "Face Lighting",
                    value = editState.faceLighting,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(faceLighting = v) } }
                )
                PrecisionSlider(
                    label = "Lip Tint Enhancer",
                    value = editState.lipTint,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(lipTint = v) } }
                )
                PrecisionSlider(
                    label = "Eye Brighten & Sclera",
                    value = editState.eyeBrighten,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(eyeBrighten = v) } }
                )
                PrecisionSlider(
                    label = "Teeth Whitening",
                    value = editState.teethWhiten,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(teethWhiten = v) } }
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Lens Blur & Bokeh Simulation", color = TextSecondary, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BokehShape.values().forEach { shape ->
                        FilterChip(
                            selected = editState.bokehShape == shape,
                            onClick = { onUpdateEdit { it.copy(bokehShape = shape) } },
                            label = { Text(shape.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                PrecisionSlider(
                    label = "Depth Blur Radius",
                    value = editState.lensBlurAmount,
                    valueRange = 0f..100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(lensBlurAmount = v) } }
                )
            }

            EditorTab.PRESETS -> {
                var newPresetName by remember { mutableStateOf("") }
                var showCreatePreset by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Signature Preset Engine", color = TextSecondary, fontSize = 12.sp)
                    TextButton(onClick = { showCreatePreset = !showCreatePreset }) {
                        Text("+ New Preset", color = AccentCyan, fontSize = 11.sp)
                    }
                }

                if (showCreatePreset) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            OutlinedTextField(
                                value = newPresetName,
                                onValueChange = { newPresetName = it },
                                label = { Text("Preset Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (newPresetName.isNotBlank()) {
                                        onSaveCustomPreset(newPresetName, "Custom")
                                        newPresetName = ""
                                        showCreatePreset = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Save Current Style", color = Color.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }

                PrecisionSlider(
                    label = "Preset Strength Amount",
                    value = editState.presetAmount,
                    valueRange = 0f..200f,
                    defaultValue = 100f,
                    onValueChange = { v -> onUpdateEdit { it.copy(presetAmount = v) } }
                )

                Spacer(modifier = Modifier.height(8.dp))
                val allPresets = SamplePhotoRepository.samplePresets + customPresets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allPresets.forEach { preset ->
                        val isApplied = editState.appliedPresetName == preset.name
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isApplied) StudioSurfaceElevated else StudioSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.name, color = if (isApplied) AccentCyan else TextPrimary, fontSize = 12.sp)
                                    Text(preset.category, color = TextTertiary, fontSize = 10.sp)
                                    Text(preset.description, color = TextSecondary, fontSize = 10.sp, maxLines = 1)
                                }
                                Button(
                                    onClick = {
                                        onUpdateEdit { current ->
                                            preset.editState.copy(
                                                photoId = current.photoId,
                                                appliedPresetName = preset.name,
                                                presetAmount = 100f
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isApplied) AccentCyan else StudioSurfaceElevated
                                    )
                                ) {
                                    Text(if (isApplied) "Applied" else "Apply", color = if (isApplied) Color.Black else TextPrimary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
