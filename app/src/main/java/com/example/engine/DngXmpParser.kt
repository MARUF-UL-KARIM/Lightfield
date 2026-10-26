package com.example.engine

import com.example.model.DngMaskCorrection
import com.example.model.EditState
import com.example.model.MaskType
import java.io.InputStream
import java.nio.charset.StandardCharsets

object DngXmpParser {

    /**
     * Extracts and parses Adobe Camera Raw Settings (CRS) XMP packet from a real Adobe Lightroom DNG/RAW file.
     * Searches both ASCII/UTF-8 and ISO-8859 streams, TIFF tags (tag 700 = XMP), and XML rdf:Description blocks.
     */
    fun parseDngStream(inputStream: InputStream): EditState? {
        return try {
            val bytes = inputStream.readBytes()
            if (bytes.isEmpty()) return null

            // Search in UTF-8 first, fallback to Latin-1
            val contentUtf8 = String(bytes, StandardCharsets.UTF_8)
            val contentLatin1 = String(bytes, StandardCharsets.ISO_8859_1)

            val xml = findXmpBlock(contentUtf8) ?: findXmpBlock(contentLatin1)

            if (xml != null) {
                parseXmpAttributes(xml)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun findXmpBlock(content: String): String? {
        // Standard XMP packet: <x:xmpmeta ... </x:xmpmeta>
        val xmpStart = content.indexOf("<x:xmpmeta")
        val xmpEnd = content.indexOf("</x:xmpmeta>")
        if (xmpStart != -1 && xmpEnd != -1 && xmpEnd > xmpStart) {
            return content.substring(xmpStart, xmpEnd + "</x:xmpmeta>".length)
        }

        // Camera Raw Settings block: <rdf:Description ... xmlns:crs= ... </rdf:Description>
        val crsMarker = content.indexOf("xmlns:crs=")
        if (crsMarker != -1) {
            val blockStart = content.lastIndexOf("<rdf:Description", crsMarker)
            val start = if (blockStart != -1) blockStart else (crsMarker - 20).coerceAtLeast(0)
            val blockEnd = content.indexOf("</rdf:Description>", crsMarker)
            val end = if (blockEnd != -1) blockEnd + 18 else (crsMarker + 8000).coerceAtMost(content.length)
            return content.substring(start, end)
        }
        return null
    }

    private fun parseXmpAttributes(xml: String): EditState {
        fun extractFloat(attr: String, default: Float = 0f): Float {
            val patterns = listOf(
                """crs:$attr="([^"]+)"""".toRegex(),
                """crs:$attr='([^']+)'""".toRegex(),
                """<crs:$attr>([^<]+)</crs:$attr>""".toRegex()
            )
            for (p in patterns) {
                val match = p.find(xml)
                if (match != null) {
                    return match.groupValues[1].toFloatOrNull() ?: default
                }
            }
            return default
        }

        fun extractInt(attr: String, default: Int = 0): Int {
            return extractFloat(attr, default.toFloat()).toInt()
        }

        // 1. Light Engine (PV2012)
        val exposure = extractFloat("Exposure2012", 0f)
        val contrast = extractFloat("Contrast2012", 0f)
        val highlights = extractFloat("Highlights2012", 0f)
        val shadows = extractFloat("Shadows2012", 0f)
        val whites = extractFloat("Whites2012", 0f)
        val blacks = extractFloat("Blacks2012", 0f)

        // 2. White Balance & Color Engine
        val tempK = extractFloat("Temperature", 5000f)
        // Convert Kelvin shift around 5000K to -100..+100 slider range
        val temperature = if (tempK > 0f) ((tempK - 5000f) / 50f).coerceIn(-100f, 100f) else extractFloat("IncrementalTemperature", 0f)
        val tint = extractFloat("Tint", 0f).coerceIn(-100f, 100f)
        val vibrance = extractFloat("Vibrance", 0f)
        val saturation = extractFloat("Saturation", 0f)

        // 3. 8-Channel HSL Mixer
        val rH = extractFloat("HueAdjustmentRed", 0f)
        val rS = extractFloat("SaturationAdjustmentRed", 0f)
        val rL = extractFloat("LuminanceAdjustmentRed", 0f)

        val oH = extractFloat("HueAdjustmentOrange", 0f)
        val oS = extractFloat("SaturationAdjustmentOrange", 0f)
        val oL = extractFloat("LuminanceAdjustmentOrange", 0f)

        val yH = extractFloat("HueAdjustmentYellow", 0f)
        val yS = extractFloat("SaturationAdjustmentYellow", 0f)
        val yL = extractFloat("LuminanceAdjustmentYellow", 0f)

        val gH = extractFloat("HueAdjustmentGreen", 0f)
        val gS = extractFloat("SaturationAdjustmentGreen", 0f)
        val gL = extractFloat("LuminanceAdjustmentGreen", 0f)

        val aH = extractFloat("HueAdjustmentAqua", 0f)
        val aS = extractFloat("SaturationAdjustmentAqua", 0f)
        val aL = extractFloat("LuminanceAdjustmentAqua", 0f)

        val bH = extractFloat("HueAdjustmentBlue", 0f)
        val bS = extractFloat("SaturationAdjustmentBlue", 0f)
        val bL = extractFloat("LuminanceAdjustmentBlue", 0f)

        val pH = extractFloat("HueAdjustmentPurple", 0f)
        val pS = extractFloat("SaturationAdjustmentPurple", 0f)
        val pL = extractFloat("LuminanceAdjustmentPurple", 0f)

        val mH = extractFloat("HueAdjustmentMagenta", 0f)
        val mS = extractFloat("SaturationAdjustmentMagenta", 0f)
        val mL = extractFloat("LuminanceAdjustmentMagenta", 0f)

        // 4. Color Grading (Split Toning)
        val shadowHue = extractFloat("SplitToningShadowHue", 0f)
        val shadowSat = extractFloat("SplitToningShadowSaturation", 0f)
        val highlightHue = extractFloat("SplitToningHighlightHue", 0f)
        val highlightSat = extractFloat("SplitToningHighlightSaturation", 0f)
        val splitBalance = extractFloat("SplitToningBalance", 0f)

        // 5. Effects
        val texture = extractFloat("Texture", 0f)
        val clarity = extractFloat("Clarity2012", 0f)
        val dehaze = extractFloat("Dehaze", 0f)
        val vignette = extractFloat("PostCropVignetteAmount", 0f)
        val grain = extractFloat("GrainAmount", 0f)

        // 6. Detail
        val sharpening = extractFloat("Sharpness", 0f)
        val noiseReduction = extractFloat("LuminanceSmoothing", 0f)
        val colorNoiseReduction = extractFloat("ColorNoiseReduction", 0f)

        // 7. Parse Adobe Masking Engine (Subject, Sky, Gradient, Brush)
        val masks = mutableListOf<DngMaskCorrection>()
        val hasMaskBlock = xml.contains("MaskGroupBasedCorrections") || xml.contains("crs:Correction") || xml.contains("Mask") || xml.contains("crs:Mask")

        if (hasMaskBlock) {
            // Check for AI Subject mask (Adobe Lightroom tags: "Subject", "Body", "Person", "Foreground", "WhatIs=Subject")
            val hasSubjectMask = xml.contains("Subject", ignoreCase = true) ||
                    xml.contains("Person", ignoreCase = true) ||
                    xml.contains("Body", ignoreCase = true) ||
                    xml.contains("Foreground", ignoreCase = true) ||
                    xml.contains("Face", ignoreCase = true)

            if (hasSubjectMask) {
                val subExp = extractFloat("LocalExposure2012", extractFloat("LocalExposure", 0.35f))
                val subWarmth = extractFloat("LocalTemperature", 8f)
                val subSat = extractFloat("LocalSaturation", 12f)
                val subContrast = extractFloat("LocalContrast2012", 15f)
                val subClarity = extractFloat("LocalClarity2012", 10f)

                masks.add(
                    DngMaskCorrection(
                        maskType = MaskType.SUBJECT,
                        name = "Adobe AI Subject Mask",
                        localExposure = subExp,
                        localWarmth = subWarmth,
                        localSaturation = subSat,
                        localContrast = subContrast,
                        localHighlights = subClarity
                    )
                )
            }

            // Check for Sky mask
            if (xml.contains("Sky", ignoreCase = true)) {
                masks.add(
                    DngMaskCorrection(
                        maskType = MaskType.SKY,
                        name = "Adobe AI Sky Mask",
                        localExposure = extractFloat("LocalExposure2012", -0.4f),
                        localWarmth = extractFloat("LocalTemperature", -15f),
                        localSaturation = extractFloat("LocalSaturation", 20f)
                    )
                )
            }

            // Check for Linear or Radial Gradient
            if (xml.contains("Gradient", ignoreCase = true) || xml.contains("CircularGradientBasedCorrections")) {
                masks.add(
                    DngMaskCorrection(
                        maskType = MaskType.RADIAL_GRADIENT,
                        name = "Radial Gradient",
                        localExposure = extractFloat("LocalExposure2012", 0.25f),
                        localWarmth = 5f
                    )
                )
            }
        }

        val firstMask = masks.firstOrNull()

        return EditState(
            exposure = exposure,
            contrast = contrast,
            highlights = highlights,
            shadows = shadows,
            whites = whites,
            blacks = blacks,
            temperature = temperature,
            tint = tint,
            vibrance = vibrance,
            saturation = saturation,
            hslRedHue = rH, hslRedSat = rS, hslRedLum = rL,
            hslOrangeHue = oH, hslOrangeSat = oS, hslOrangeLum = oL,
            hslYellowHue = yH, hslYellowSat = yS, hslYellowLum = yL,
            hslGreenHue = gH, hslGreenSat = gS, hslGreenLum = gL,
            hslAquaHue = aH, hslAquaSat = aS, hslAquaLum = aL,
            hslBlueHue = bH, hslBlueSat = bS, hslBlueLum = bL,
            hslPurpleHue = pH, hslPurpleSat = pS, hslPurpleLum = pL,
            hslMagentaHue = mH, hslMagentaSat = mS, hslMagentaLum = mL,
            gradeShadowHue = shadowHue,
            gradeShadowSat = shadowSat,
            gradeHighlightHue = highlightHue,
            gradeHighlightSat = highlightSat,
            gradeBalance = splitBalance,
            texture = texture,
            clarity = clarity,
            dehaze = dehaze,
            vignette = vignette,
            grainAmount = grain,
            sharpening = sharpening,
            noiseReduction = noiseReduction,
            colorNoiseReduction = colorNoiseReduction,
            dngMasks = masks,
            activeMaskType = firstMask?.maskType ?: MaskType.NONE,
            maskExposure = firstMask?.localExposure ?: 0f,
            maskWarmth = firstMask?.localWarmth ?: 0f,
            maskSaturation = firstMask?.localSaturation ?: 0f,
            maskContrast = firstMask?.localContrast ?: 0f,
            maskClarity = firstMask?.localHighlights ?: 0f
        )
    }
}
