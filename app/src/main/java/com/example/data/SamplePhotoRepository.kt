package com.example.data

import com.example.R
import com.example.model.AspectRatioType
import com.example.model.EditState
import com.example.model.PhotoItem
import com.example.model.PresetItem

object SamplePhotoRepository {

    val samplePresets = listOf(
        PresetItem(
            id = "preset_cinematic_teal",
            name = "Teal & Orange",
            category = "Cinematic",
            description = "Iconic Hollywood blockbuster color grading with warm highlights and teal shadows.",
            editState = EditState(
                exposure = 0.2f,
                contrast = 18f,
                highlights = -20f,
                shadows = 15f,
                temperature = 12f,
                vibrance = 25f,
                hslRedSat = 15f,
                hslOrangeSat = 20f,
                hslAquaSat = -10f,
                hslBlueSat = 35f,
                gradeShadowHue = 200f,
                gradeShadowSat = 35f,
                gradeHighlightHue = 35f,
                gradeHighlightSat = 40f,
                vignette = -18f,
                appliedPresetName = "Teal & Orange"
            )
        ),
        PresetItem(
            id = "preset_mood_bw",
            name = "Moody Noir B&W",
            category = "Black & White",
            description = "Deep contrast black and white with crushed blacks and dramatic micro-contrast.",
            editState = EditState(
                contrast = 35f,
                highlights = -15f,
                shadows = -25f,
                saturation = -100f,
                clarity = 25f,
                texture = 20f,
                grainAmount = 30f,
                vignette = -30f,
                appliedPresetName = "Moody Noir B&W"
            )
        ),
        PresetItem(
            id = "preset_vintage_film",
            name = "Vintage 35mm Film",
            category = "Vintage",
            description = "Warm organic film emulation with lifted blacks, muted greens, and nostalgic film grain.",
            editState = EditState(
                exposure = 0.1f,
                contrast = -10f,
                highlights = -30f,
                shadows = 30f,
                blacks = 15f,
                temperature = 18f,
                tint = 6f,
                saturation = -12f,
                grainAmount = 45f,
                vignette = -12f,
                appliedPresetName = "Vintage 35mm Film"
            )
        ),
        PresetItem(
            id = "preset_golden_landscape",
            name = "Golden Vista",
            category = "Landscape",
            description = "Rich golden hour sunrise/sunset boost with vibrant greens, warm skies, and dehazed mountains.",
            editState = EditState(
                exposure = 0.15f,
                contrast = 12f,
                highlights = -35f,
                shadows = 20f,
                temperature = 22f,
                tint = 2f,
                vibrance = 30f,
                dehaze = 15f,
                texture = 15f,
                sharpening = 25f,
                appliedPresetName = "Golden Vista"
            )
        ),
        PresetItem(
            id = "preset_cyberpunk",
            name = "Neon Cyberpunk",
            category = "Street",
            description = "Vibrant electric magenta, cyan, and deep saturated night street aesthetic.",
            editState = EditState(
                exposure = -0.2f,
                contrast = 28f,
                shadows = -10f,
                temperature = -15f,
                tint = 25f,
                vibrance = 45f,
                clarity = 20f,
                vignette = -25f,
                appliedPresetName = "Neon Cyberpunk"
            )
        ),
        PresetItem(
            id = "preset_clean_portrait",
            name = "Soft Portrait Pro",
            category = "Portrait",
            description = "Gentle highlight rolloff, skin smoothing warmth, and crisp eye definition.",
            editState = EditState(
                exposure = 0.25f,
                contrast = -5f,
                highlights = -20f,
                shadows = 15f,
                temperature = 8f,
                texture = -10f,
                clarity = 5f,
                skinSmooth = 35f,
                eyeBrighten = 25f,
                appliedPresetName = "Soft Portrait Pro"
            )
        )
    )

    fun getInitialPhotos(): List<PhotoItem> {
        return listOf(
            PhotoItem(
                id = "photo_landscape_01",
                resourceId = R.drawable.sample_landscape,
                title = "Alpine Horizon.DNG",
                iso = 64,
                shutterSpeed = "1/500s",
                aperture = "f/8.0",
                focalLength = "24mm",
                cameraModel = "Hasselblad X2D 100C",
                width = 11656,
                height = 8742,
                rating = 5,
                isFlagged = true,
                album = "Landscape",
                isRaw = true,
                editState = EditState(
                    photoId = "photo_landscape_01",
                    exposure = 0.2f,
                    temperature = 10f,
                    vibrance = 15f
                )
            ),
            PhotoItem(
                id = "photo_portrait_02",
                resourceId = R.drawable.sample_portrait,
                title = "Studio Radiance.RAW",
                iso = 100,
                shutterSpeed = "1/160s",
                aperture = "f/1.4",
                focalLength = "85mm",
                cameraModel = "Sony Alpha 1",
                width = 8640,
                height = 5760,
                rating = 4,
                isFlagged = true,
                album = "Portrait",
                isRaw = true,
                editState = EditState(
                    photoId = "photo_portrait_02",
                    skinSmooth = 20f,
                    temperature = 5f
                )
            ),
            PhotoItem(
                id = "photo_street_03",
                resourceId = R.drawable.sample_street,
                title = "Shibuya Rain.DNG",
                iso = 1600,
                shutterSpeed = "1/125s",
                aperture = "f/2.0",
                focalLength = "35mm",
                cameraModel = "Leica M11",
                width = 9528,
                height = 6328,
                rating = 5,
                isFlagged = false,
                album = "Street",
                isRaw = true,
                editState = EditState(
                    photoId = "photo_street_03",
                    clarity = 15f,
                    vignette = -20f
                )
            ),
            PhotoItem(
                id = "photo_dng_preset_04",
                resourceId = R.drawable.sample_portrait,
                title = "Adobe_Editorial_Subject.DNG",
                iso = 100,
                shutterSpeed = "1/250s",
                aperture = "f/1.8",
                focalLength = "50mm",
                cameraModel = "Adobe DNG Preset (CRS XMP)",
                width = 8640,
                height = 5760,
                rating = 5,
                isFlagged = true,
                album = "DNG Presets",
                isRaw = true,
                editState = EditState(
                    photoId = "photo_dng_preset_04",
                    exposure = 0.15f,
                    contrast = 12f,
                    highlights = -20f,
                    shadows = 20f,
                    temperature = 6f,
                    vibrance = 20f,
                    activeMaskType = com.example.model.MaskType.SUBJECT,
                    maskExposure = 0.40f,
                    maskWarmth = 12f,
                    maskSaturation = 18f,
                    maskContrast = 15f,
                    maskClarity = 15f,
                    dngMasks = listOf(
                        com.example.model.DngMaskCorrection(
                            maskType = com.example.model.MaskType.SUBJECT,
                            name = "Adobe AI Subject Mask",
                            localExposure = 0.40f,
                            localWarmth = 12f,
                            localSaturation = 18f,
                            localContrast = 15f,
                            localHighlights = 15f
                        ),
                        com.example.model.DngMaskCorrection(
                            maskType = com.example.model.MaskType.SKY,
                            name = "Adobe AI Sky Mask",
                            localExposure = -0.35f,
                            localWarmth = -15f,
                            localSaturation = 25f
                        )
                    ),
                    appliedPresetName = "Editorial Subject & Sky DNG"
                )
            )
        )
    }
}
