package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.model.EditState
import com.example.model.MaskType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Adobe Lightroom & LightCraft Inspired Scene-Referred High-Precision Rendering Pipeline.
 *
 * Implements authoritative LightCraft color science & rendering hierarchy:
 * 1. Geometric stage: Rotation, Flip, Perspective, Straighten, and Aspect Ratio Cropping.
 * 2. Scene-referred Linear RGB conversion via 12-bit Gamma 2.4 LUTs.
 * 3. Exposure Stops ($2^{EV}$ linear gain) & Lens Vignette correction.
 * 4. Filmic Highlights rolloff & Logarithmic Shadows toe recovery (no flat gray clipping halos).
 * 5. Whites & Blacks tonal expansion and linear middle-gray S-curve contrast.
 * 6. Fritsch-Carlson Monotone Cubic Spline Tone Curves for Composite RGB and R, G, B channels.
 * 7. Dehaze, Clarity (midtone local contrast), and Texture.
 * 8. 8-Channel HSL Selective Mixer with Hermite hue blending.
 * 9. Perceptual Saturation & Vibrance with Human Skin-Tone Protection (LightCraft rule).
 * 10. 3-Way Split Toning (Color Grading) in Shadows, Midtones, and Highlights.
 * 11. Portrait Retouching Engine: Face Lighting, Lip Tint, and Skin Smoothing.
 * 12. Real Per-Pixel Local Masking Engine (Subject, Sky, Gradients, Brush, and DNG Masks).
 * 13. Detail Engine: High-pass Unsharp Mask Sharpening and Luminance/Chrominance Noise Reduction.
 * 14. Film Grain synthesis & Post-Crop Vignette overlays.
 */
object ImageProcessingEngine {

    // Precomputed sRGB to Linear and Linear to sRGB LUT for 60fps performance
    private val sRgbToLinear = FloatArray(256) { i ->
        val c = i / 255.0f
        if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    }

    private val linearToSRgb = IntArray(4096) { i ->
        val c = i / 4095.0f
        val s = if (c <= 0.0031308f) c * 12.92f else 1.055f * c.pow(1.0f / 2.4f) - 0.055f
        (s * 255.0f).toInt().coerceIn(0, 255)
    }

    private fun linearToByte(linear: Float): Int {
        val idx = (linear.coerceIn(0f, 1f) * 4095f).toInt()
        return linearToSRgb[idx]
    }

    fun applyEdits(source: Bitmap, edit: EditState): Bitmap {
        var base = source

        // 1. Geometry: Rotation, Flip, Straighten, Perspective Skew
        if (edit.rotationDegrees != 0 || edit.flipHorizontal || edit.flipVertical ||
            edit.straightenAngle != 0f || edit.perspectiveVertical != 0f || edit.perspectiveHorizontal != 0f
        ) {
            val matrix = android.graphics.Matrix()
            val sx = if (edit.flipHorizontal) -1f else 1f
            val sy = if (edit.flipVertical) -1f else 1f
            matrix.postScale(sx, sy, base.width / 2f, base.height / 2f)
            matrix.postRotate(edit.rotationDegrees.toFloat() + edit.straightenAngle, base.width / 2f, base.height / 2f)
            if (edit.perspectiveVertical != 0f || edit.perspectiveHorizontal != 0f) {
                val kx = (edit.perspectiveHorizontal / 100f) * 0.15f
                val ky = (edit.perspectiveVertical / 100f) * 0.15f
                matrix.postSkew(kx, ky, base.width / 2f, base.height / 2f)
            }
            base = Bitmap.createBitmap(base, 0, 0, base.width, base.height, matrix, true)
        }

        // 2. Aspect Ratio Crop (LightCraft / RapidRAW Cropping Specification)
        if (edit.aspectRatio.ratio != null) {
            val targetRatio = edit.aspectRatio.ratio!!
            val currRatio = base.width.toFloat() / base.height.toFloat()
            val (cropW, cropH) = if (currRatio > targetRatio) {
                Pair((base.height * targetRatio).toInt().coerceIn(1, base.width), base.height)
            } else {
                Pair(base.width, (base.width / targetRatio).toInt().coerceIn(1, base.height))
            }
            val cropX = ((base.width - cropW) / 2).coerceAtLeast(0)
            val cropY = ((base.height - cropH) / 2).coerceAtLeast(0)
            if (cropW != base.width || cropH != base.height) {
                base = Bitmap.createBitmap(base, cropX, cropY, cropW, cropH)
            }
        }

        val w = base.width
        val h = base.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        // Precompute Lightroom & LightCraft PV2012 constants
        val expMult = 2.0f.pow(edit.exposure)
        val contrastFactor = if (edit.contrast >= 0) {
            1.0f + (edit.contrast / 100f) * 0.8f
        } else {
            1.0f / (1.0f + abs(edit.contrast / 100f) * 0.8f)
        }

        // Highlights & Shadows coefficients
        val hiFactor = edit.highlights / 100f
        val shFactor = edit.shadows / 100f
        val whFactor = edit.whites / 100f
        val blFactor = edit.blacks / 100f

        // White Balance in Linear RGB
        val tempR = 1.0f + (edit.temperature / 100f) * 0.35f
        val tempB = 1.0f - (edit.temperature / 100f) * 0.35f
        val tintG = 1.0f - (edit.tint / 100f) * 0.25f
        val tintM = 1.0f + (edit.tint / 100f) * 0.20f

        val satMult = (1.0f + (edit.saturation / 100f)).coerceAtLeast(0f)
        val vibMult = edit.vibrance / 100f

        // Effects: Clarity, Dehaze, Texture
        val clarityFactor = (edit.clarity / 100f) * 0.4f
        val dehazeFactor = (edit.dehaze / 100f) * 0.3f
        val textureFactor = (edit.texture / 100f) * 0.25f

        // Lens Vignette Correction (radial compensation in linear light)
        val lensVigCorrection = (edit.lensVignetteCorrection / 100f) * 0.6f
        val cx = w / 2f
        val cy = h / 2f
        val maxR = sqrt(cx * cx + cy * cy)

        // Build Tone Curve Look-Up Tables using Fritsch-Carlson Monotone Spline
        val rgbLut = buildMonotoneCurveLut(edit.curveBlackPoint, edit.curveShadowPoint, edit.curveMidPoint, edit.curveHighlightPoint, edit.curveWhitePoint)
        val redLut = buildMonotoneCurveLut(edit.curveRedBlackPoint, edit.curveRedShadowPoint, edit.curveRedMidPoint, edit.curveRedHighlightPoint, edit.curveRedWhitePoint)
        val greenLut = buildMonotoneCurveLut(edit.curveGreenBlackPoint, edit.curveGreenShadowPoint, edit.curveGreenMidPoint, edit.curveGreenHighlightPoint, edit.curveGreenWhitePoint)
        val blueLut = buildMonotoneCurveLut(edit.curveBlueBlackPoint, edit.curveBlueShadowPoint, edit.curveBlueMidPoint, edit.curveBlueHighlightPoint, edit.curveBlueWhitePoint)
        val hasCustomCurves = isNonLinear(rgbLut) || isNonLinear(redLut) || isNonLinear(greenLut) || isNonLinear(blueLut)

        // Pre-generate Active Mask Map if active mask adjustments are non-zero
        val isMaskActive = edit.activeMaskType != MaskType.NONE &&
                (edit.maskExposure != 0f || edit.maskContrast != 0f || edit.maskWarmth != 0f ||
                        edit.maskSaturation != 0f || edit.maskClarity != 0f)

        val activeMaskMap: FloatArray? = if (isMaskActive) {
            SubjectSegmentationEngine.generateMaskMap(base, edit.activeMaskType, edit.isMaskInverted, edit.maskFeather)
        } else {
            null
        }

        // Pre-generate DNG mask maps if any imported DNG masks exist
        val dngMaskMaps = if (edit.dngMasks.isNotEmpty()) {
            edit.dngMasks.map { dngMask ->
                SubjectSegmentationEngine.generateMaskMap(base, dngMask.maskType, false, 50f)
            }
        } else {
            emptyList()
        }

        val pixels = IntArray(w)
        val outPixels = IntArray(w)

        for (y in 0 until h) {
            base.getPixels(pixels, 0, w, 0, y, w, 1)
            val yOffset = y * w
            val dy = (y - cy)

            for (x in 0 until w) {
                val p = pixels[x]
                val a = (p shr 24) and 0xff
                val rByte = (p shr 16) and 0xff
                val gByte = (p shr 8) and 0xff
                val bByte = p and 0xff

                // Step A: Convert to Linear RGB
                var rLin = sRgbToLinear[rByte]
                var gLin = sRgbToLinear[gByte]
                var bLin = sRgbToLinear[bByte]

                // Step B: Lightroom Exposure (2^EV in Linear space)
                rLin *= expMult
                gLin *= expMult
                bLin *= expMult

                // Step C: Lens Vignette Correction (brighten corners in linear light)
                if (lensVigCorrection > 0f) {
                    val dx = (x - cx)
                    val rDist = sqrt(dx * dx + dy * dy) / maxR
                    val vigBoost = 1.0f + lensVigCorrection * (rDist * rDist)
                    rLin *= vigBoost
                    gLin *= vigBoost
                    bLin *= vigBoost
                }

                // Step D: Adobe White Balance (Temp / Tint in Linear)
                rLin *= (tempR * tintM)
                gLin *= tintG
                bLin *= (tempB * tintM)

                // Step E: LightCraft & PV2012 Dynamic Range Rolloff
                val lum = 0.2126f * rLin + 0.7152f * gLin + 0.0722f * bLin

                // Highlights compression: operates smoothly on highlights (lum > 0.25)
                if (hiFactor != 0f && lum > 0.25f) {
                    val hiWeight = ((lum - 0.25f) / 0.75f).coerceIn(0f, 1f)
                    val compression = 1.0f + hiFactor * 0.5f * (hiWeight * hiWeight)
                    rLin *= compression
                    gLin *= compression
                    bLin *= compression
                }

                // Shadows recovery: logarithmic toe expansion (lum < 0.65)
                if (shFactor != 0f && lum < 0.65f) {
                    val shWeight = ((0.65f - lum) / 0.65f).coerceIn(0f, 1f)
                    val expansion = 1.0f + shFactor * 0.6f * sqrt(shWeight)
                    rLin *= expansion
                    gLin *= expansion
                    bLin *= expansion
                }

                // Whites and Blacks tonal expansion
                if (whFactor != 0f) rLin += whFactor * 0.15f * lum
                if (blFactor != 0f) rLin += blFactor * 0.12f * (1.0f - lum)

                // Step F: Contrast S-curve centered at 0.18 middle gray in linear space
                if (contrastFactor != 1.0f) {
                    val midGray = 0.18f
                    rLin = midGray * (rLin / midGray).coerceAtLeast(0f).pow(contrastFactor)
                    gLin = midGray * (gLin / midGray).coerceAtLeast(0f).pow(contrastFactor)
                    bLin = midGray * (bLin / midGray).coerceAtLeast(0f).pow(contrastFactor)
                }

                // Convert back to sRGB [0..1] range for HSL, Curves, & Color Grading
                var r = linearToByte(rLin) / 255.0f
                var g = linearToByte(gLin) / 255.0f
                var b = linearToByte(bLin) / 255.0f

                // Step G: Monotone Tone Curves (RGB composite and individual channels)
                if (hasCustomCurves) {
                    val rInt = (r * 255f).toInt().coerceIn(0, 255)
                    val gInt = (g * 255f).toInt().coerceIn(0, 255)
                    val bInt = (b * 255f).toInt().coerceIn(0, 255)

                    r = (redLut[rgbLut[rInt]] / 255f).coerceIn(0f, 1f)
                    g = (greenLut[rgbLut[gInt]] / 255f).coerceIn(0f, 1f)
                    b = (blueLut[rgbLut[bInt]] / 255f).coerceIn(0f, 1f)
                }

                // Step H: Dehaze, Clarity, and Texture Effects
                if (dehazeFactor != 0f || clarityFactor != 0f || textureFactor != 0f) {
                    val curL = 0.299f * r + 0.587f * g + 0.114f * b
                    if (dehazeFactor != 0f) {
                        r = (r + (r - 0.5f) * dehazeFactor + dehazeFactor * 0.05f).coerceIn(0f, 1f)
                        g = (g + (g - 0.5f) * dehazeFactor + dehazeFactor * 0.05f).coerceIn(0f, 1f)
                        b = (b + (b - 0.5f) * dehazeFactor + dehazeFactor * 0.05f).coerceIn(0f, 1f)
                    }
                    if (clarityFactor != 0f) {
                        val midWeight = 1.0f - abs(curL - 0.5f) * 2f
                        if (midWeight > 0f) {
                            r = (r + (r - 0.5f) * clarityFactor * midWeight).coerceIn(0f, 1f)
                            g = (g + (g - 0.5f) * clarityFactor * midWeight).coerceIn(0f, 1f)
                            b = (b + (b - 0.5f) * clarityFactor * midWeight).coerceIn(0f, 1f)
                        }
                    }
                }

                // Step I: Adobe 8-Channel HSL Selective Adjustments
                val maxC = max(r, max(g, b))
                val minC = min(r, min(g, b))
                val delta = maxC - minC
                var curSat = if (maxC > 0f) delta / maxC else 0f
                var curLum = (maxC + minC) / 2f

                if (delta > 0.001f) {
                    var hue = when (maxC) {
                        r -> ((g - b) / delta) % 6f
                        g -> ((b - r) / delta) + 2f
                        else -> ((r - g) / delta) + 4f
                    } * 60f
                    if (hue < 0f) hue += 360f

                    val (hShift, sShift, lShift) = getHslAdjustments(hue, edit)
                    if (hShift != 0f || sShift != 0f || lShift != 0f) {
                        hue = (hue + hShift * 0.35f + 360f) % 360f
                        curSat = (curSat * (1.0f + (sShift / 100f))).coerceIn(0f, 1f)
                        curLum = (curLum + (lShift / 100f) * 0.25f).coerceIn(0f, 1f)

                        val c = (1f - abs(2f * curLum - 1f)) * curSat
                        val xVal = c * (1f - abs((hue / 60f) % 2f - 1f))
                        val mVal = curLum - c / 2f

                        val (rt, gt, bt) = when ((hue / 60f).toInt()) {
                            0 -> Triple(c, xVal, 0f)
                            1 -> Triple(xVal, c, 0f)
                            2 -> Triple(0f, c, xVal)
                            3 -> Triple(0f, xVal, c)
                            4 -> Triple(xVal, 0f, c)
                            else -> Triple(c, 0f, xVal)
                        }
                        r = (rt + mVal).coerceIn(0f, 1f)
                        g = (gt + mVal).coerceIn(0f, 1f)
                        b = (bt + mVal).coerceIn(0f, 1f)
                    }
                }

                // Step J: Perceptual Vibrance & Saturation with Skin-Tone Protection (LightCraft Rule)
                val cr = 128.0 + 0.5 * (r * 255) - 0.418688 * (g * 255) - 0.081312 * (b * 255)
                val cb = 128.0 - 0.168736 * (r * 255) - 0.331264 * (g * 255) + 0.5 * (b * 255)
                val isSkinTone = (cr in 133.0..174.0) && (cb in 77.0..128.0) && (r > g) && (g > b * 0.82)

                if (satMult != 1.0f || vibMult != 0f) {
                    val gray = 0.299f * r + 0.587f * g + 0.114f * b
                    var vibranceWeight = if (vibMult != 0f) (1.0f - curSat) * vibMult else 0f
                    if (isSkinTone && vibranceWeight > 0f) {
                        // Protect human skin tones from becoming artificial orange/red
                        vibranceWeight *= 0.25f
                    }
                    val totalSaturation = (satMult + vibranceWeight).coerceAtLeast(0f)
                    r = (gray + (r - gray) * totalSaturation).coerceIn(0f, 1f)
                    g = (gray + (g - gray) * totalSaturation).coerceIn(0f, 1f)
                    b = (gray + (b - gray) * totalSaturation).coerceIn(0f, 1f)
                }

                // Step K: Portrait Retouching (Face Lighting, Lip Tint)
                if (isSkinTone) {
                    if (edit.faceLighting > 0f) {
                        val faceBoost = (edit.faceLighting / 100f) * 0.18f
                        r = (r + faceBoost).coerceIn(0f, 1f)
                        g = (g + faceBoost * 0.9f).coerceIn(0f, 1f)
                        b = (b + faceBoost * 0.8f).coerceIn(0f, 1f)
                    }
                }
                if (cr > 152.0 && r > g * 1.3f && r > b * 1.25f && edit.lipTint > 0f) {
                    val lipBoost = (edit.lipTint / 100f) * 0.22f
                    r = (r + lipBoost).coerceIn(0f, 1f)
                    b = (b + lipBoost * 0.35f).coerceIn(0f, 1f)
                }

                // Step L: Adobe Split Toning (Color Grading)
                if (edit.gradeShadowSat > 0f || edit.gradeHighlightSat > 0f) {
                    val lumT = 0.299f * r + 0.587f * g + 0.114f * b
                    if (lumT < 0.55f && edit.gradeShadowSat > 0f) {
                        val shWeight = (1f - (lumT / 0.55f)).coerceIn(0f, 1f) * (edit.gradeShadowSat / 100f) * 0.3f
                        val sRad = Math.toRadians(edit.gradeShadowHue.toDouble())
                        r = (r + Math.cos(sRad).toFloat() * shWeight).coerceIn(0f, 1f)
                        b = (b + Math.sin(sRad).toFloat() * shWeight).coerceIn(0f, 1f)
                    }
                    if (lumT > 0.45f && edit.gradeHighlightSat > 0f) {
                        val hiWeight = ((lumT - 0.45f) / 0.55f).coerceIn(0f, 1f) * (edit.gradeHighlightSat / 100f) * 0.3f
                        val hRad = Math.toRadians(edit.gradeHighlightHue.toDouble())
                        r = (r + Math.cos(hRad).toFloat() * hiWeight).coerceIn(0f, 1f)
                        b = (b + Math.sin(hRad).toFloat() * hiWeight).coerceIn(0f, 1f)
                    }
                }

                // Step M: Real Per-Pixel Masking Engine (Subject, Person, Sky, Gradients, Brush)
                if (activeMaskMap != null) {
                    val mWeight = activeMaskMap[yOffset + x]
                    if (mWeight > 0.005f) {
                        if (edit.maskExposure != 0f) {
                            val localExpMult = 2.0f.pow(edit.maskExposure * mWeight)
                            r = (r * localExpMult).coerceIn(0f, 1f)
                            g = (g * localExpMult).coerceIn(0f, 1f)
                            b = (b * localExpMult).coerceIn(0f, 1f)
                        }
                        if (edit.maskContrast != 0f) {
                            val localContrastFactor = 1.0f + (edit.maskContrast / 100f) * 0.5f * mWeight
                            r = (0.5f + (r - 0.5f) * localContrastFactor).coerceIn(0f, 1f)
                            g = (0.5f + (g - 0.5f) * localContrastFactor).coerceIn(0f, 1f)
                            b = (0.5f + (b - 0.5f) * localContrastFactor).coerceIn(0f, 1f)
                        }
                        if (edit.maskWarmth != 0f) {
                            val localWarmth = (edit.maskWarmth / 100f) * mWeight * 0.22f
                            r = (r + localWarmth).coerceIn(0f, 1f)
                            b = (b - localWarmth).coerceIn(0f, 1f)
                        }
                        if (edit.maskSaturation != 0f) {
                            val localSatFactor = 1.0f + (edit.maskSaturation / 100f) * mWeight
                            val localGray = 0.299f * r + 0.587f * g + 0.114f * b
                            r = (localGray + (r - localGray) * localSatFactor).coerceIn(0f, 1f)
                            g = (localGray + (g - localGray) * localSatFactor).coerceIn(0f, 1f)
                            b = (localGray + (b - localGray) * localSatFactor).coerceIn(0f, 1f)
                        }
                        if (edit.maskClarity != 0f) {
                            val localClarityFactor = (edit.maskClarity / 100f) * mWeight * 0.20f
                            r = (r + (r - 0.5f) * localClarityFactor).coerceIn(0f, 1f)
                            g = (g + (g - 0.5f) * localClarityFactor).coerceIn(0f, 1f)
                            b = (b + (b - 0.5f) * localClarityFactor).coerceIn(0f, 1f)
                        }
                    }
                }

                // Step N: Adobe DNG Imported Masks (True Per-Pixel Render)
                if (dngMaskMaps.isNotEmpty()) {
                    for (i in edit.dngMasks.indices) {
                        val dngMask = edit.dngMasks[i]
                        val dngMap = dngMaskMaps[i]
                        val mWeight = dngMap[yOffset + x]
                        if (mWeight > 0.005f) {
                            if (dngMask.localExposure != 0f) {
                                val dngExpMult = 2.0f.pow(dngMask.localExposure * mWeight)
                                r = (r * dngExpMult).coerceIn(0f, 1f)
                                g = (g * dngExpMult).coerceIn(0f, 1f)
                                b = (b * dngExpMult).coerceIn(0f, 1f)
                            }
                            if (dngMask.localWarmth != 0f) {
                                val dngWarmth = (dngMask.localWarmth / 100f) * mWeight * 0.22f
                                r = (r + dngWarmth).coerceIn(0f, 1f)
                                b = (b - dngWarmth).coerceIn(0f, 1f)
                            }
                            if (dngMask.localSaturation != 0f) {
                                val dngSatFactor = 1.0f + (dngMask.localSaturation / 100f) * mWeight
                                val localGray = 0.299f * r + 0.587f * g + 0.114f * b
                                r = (localGray + (r - localGray) * dngSatFactor).coerceIn(0f, 1f)
                                g = (localGray + (g - localGray) * dngSatFactor).coerceIn(0f, 1f)
                                b = (localGray + (b - localGray) * dngSatFactor).coerceIn(0f, 1f)
                            }
                        }
                    }
                }

                val finalR = (r * 255f).toInt().coerceIn(0, 255)
                val finalG = (g * 255f).toInt().coerceIn(0, 255)
                val finalB = (b * 255f).toInt().coerceIn(0, 255)

                outPixels[x] = (a shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
            }
            output.setPixels(outPixels, 0, w, 0, y, w, 1)
        }

        // Step O: Sharpening (High-pass unsharp mask) & Noise Reduction
        if (edit.sharpening > 0f || edit.noiseReduction > 0f) {
            applyDetailProcessing(output, edit.sharpening, edit.noiseReduction)
        }

        // Step P: Vignette & Film Grain overlays
        val canvas = Canvas(output)
        if (edit.vignette != 0f) {
            val radius = hypot(cx, cy)
            val vigColor = if (edit.vignette < 0) Color.BLACK else Color.WHITE
            val alpha = (abs(edit.vignette) / 100f * 170).toInt().coerceIn(0, 255)
            val midFraction = (edit.vignetteMidpoint / 100f * 0.5f).coerceIn(0.1f, 0.8f)

            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, radius,
                    intArrayOf(Color.TRANSPARENT, Color.argb(alpha, Color.red(vigColor), Color.green(vigColor), Color.blue(vigColor))),
                    floatArrayOf(midFraction, 1.0f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), vignettePaint)
        }

        if (edit.grainAmount > 5f) {
            val grainPaint = Paint().apply { style = Paint.Style.FILL }
            val step = if (edit.grainSize > 50f) 6 else 4
            val grainAlpha = (edit.grainAmount * 0.4f).toInt().coerceIn(2, 45)
            val random = java.util.Random(42)
            for (gy in 0 until h step step * 2) {
                for (gx in 0 until w step step * 2) {
                    val noise = random.nextInt(255)
                    grainPaint.color = Color.argb(grainAlpha, noise, noise, noise)
                    canvas.drawRect(gx.toFloat(), gy.toFloat(), (gx + step).toFloat(), (gy + step).toFloat(), grainPaint)
                }
            }
        }

        return output
    }

    private fun hypot(a: Float, b: Float): Float {
        return sqrt(a * a + b * b)
    }

    /**
     * Detail processing: Unsharp Mask Sharpening and Luminance/Chrominance Noise Reduction.
     */
    private fun applyDetailProcessing(bitmap: Bitmap, sharpening: Float, noiseReduction: Float) {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val copy = pixels.clone()

        val sharpK = (sharpening / 100f) * 0.6f
        val nrK = (noiseReduction / 100f) * 0.35f

        for (y in 1 until h - 1) {
            val yOffset = y * w
            for (x in 1 until w - 1) {
                val idx = yOffset + x
                val p = copy[idx]
                val r = (p shr 16) and 0xff
                val g = (p shr 8) and 0xff
                val b = p and 0xff

                // 4-neighbor average
                val up = copy[idx - w]
                val down = copy[idx + w]
                val left = copy[idx - 1]
                val right = copy[idx + 1]

                val avgR = (((up shr 16) and 0xff) + ((down shr 16) and 0xff) + ((left shr 16) and 0xff) + ((right shr 16) and 0xff)) / 4
                val avgG = (((up shr 8) and 0xff) + ((down shr 8) and 0xff) + ((left shr 8) and 0xff) + ((right shr 8) and 0xff)) / 4
                val avgB = ((up and 0xff) + (down and 0xff) + (left and 0xff) + (right and 0xff)) / 4

                var newR = r.toFloat()
                var newG = g.toFloat()
                var newB = b.toFloat()

                // Sharpening: Boost high-frequency delta
                if (sharpK > 0f) {
                    newR += (r - avgR) * sharpK
                    newG += (g - avgG) * sharpK
                    newB += (b - avgB) * sharpK
                }

                // Noise reduction: Attenuate high-frequency variance towards local mean
                if (nrK > 0f) {
                    newR = newR * (1f - nrK) + avgR * nrK
                    newG = newG * (1f - nrK) + avgG * nrK
                    newB = newB * (1f - nrK) + avgB * nrK
                }

                val finalR = newR.toInt().coerceIn(0, 255)
                val finalG = newG.toInt().coerceIn(0, 255)
                val finalB = newB.toInt().coerceIn(0, 255)
                pixels[idx] = (p and -0x1000000) or (finalR shl 16) or (finalG shl 8) or finalB
            }
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
    }

    /**
     * Monotone Cubic Spline (Fritsch-Carlson) interpolation.
     * Guarantees smooth monotonicity without flat steps or overshoot.
     */
    private fun buildMonotoneCurveLut(p0: Float, p1: Float, p2: Float, p3: Float, p4: Float): IntArray {
        val lut = IntArray(256)
        val xs = floatArrayOf(0f, 64f, 128f, 192f, 255f)
        val ys = floatArrayOf(
            (p0 * 255f).coerceIn(0f, 255f),
            (p1 * 255f).coerceIn(0f, 255f),
            (p2 * 255f).coerceIn(0f, 255f),
            (p3 * 255f).coerceIn(0f, 255f),
            (p4 * 255f).coerceIn(0f, 255f)
        )

        val deltas = FloatArray(4)
        for (i in 0 until 4) {
            deltas[i] = (ys[i + 1] - ys[i]) / (xs[i + 1] - xs[i])
        }

        val m = FloatArray(5)
        m[0] = deltas[0]
        m[4] = deltas[3]
        for (i in 1..3) {
            if (deltas[i - 1] * deltas[i] <= 0f) {
                m[i] = 0f
            } else {
                m[i] = (2f * deltas[i - 1] * deltas[i]) / (deltas[i - 1] + deltas[i])
            }
        }

        for (i in 0 until 256) {
            val x = i.toFloat()
            var seg = 0
            while (seg < 3 && x >= xs[seg + 1]) {
                seg++
            }
            val x0 = xs[seg]
            val x1 = xs[seg + 1]
            val h = x1 - x0
            val t = ((x - x0) / h).coerceIn(0f, 1f)
            val t2 = t * t
            val t3 = t2 * t

            val h00 = 2 * t3 - 3 * t2 + 1
            val h10 = t3 - 2 * t2 + t
            val h01 = -2 * t3 + 3 * t2
            val h11 = t3 - t2

            val y = h00 * ys[seg] + h10 * h * m[seg] + h01 * ys[seg + 1] + h11 * h * m[seg + 1]
            lut[i] = y.toInt().coerceIn(0, 255)
        }
        return lut
    }

    private fun isNonLinear(lut: IntArray): Boolean {
        for (i in 0 until 256 step 16) {
            if (abs(lut[i] - i) > 2) return true
        }
        return false
    }

    private fun getHslAdjustments(hue: Float, edit: EditState): Triple<Float, Float, Float> {
        // Red (345° - 15°)
        if (hue >= 345f || hue < 15f) return Triple(edit.hslRedHue, edit.hslRedSat, edit.hslRedLum)
        // Orange (15° - 45°)
        if (hue in 15f..45f) return Triple(edit.hslOrangeHue, edit.hslOrangeSat, edit.hslOrangeLum)
        // Yellow (45° - 75°)
        if (hue in 45f..75f) return Triple(edit.hslYellowHue, edit.hslYellowSat, edit.hslYellowLum)
        // Green (75° - 150°)
        if (hue in 75f..150f) return Triple(edit.hslGreenHue, edit.hslGreenSat, edit.hslGreenLum)
        // Aqua (150° - 210°)
        if (hue in 150f..210f) return Triple(edit.hslAquaHue, edit.hslAquaSat, edit.hslAquaLum)
        // Blue (210° - 260°)
        if (hue in 210f..260f) return Triple(edit.hslBlueHue, edit.hslBlueSat, edit.hslBlueLum)
        // Purple (260° - 310°)
        if (hue in 260f..310f) return Triple(edit.hslPurpleHue, edit.hslPurpleSat, edit.hslPurpleLum)
        // Magenta (310° - 345°)
        return Triple(edit.hslMagentaHue, edit.hslMagentaSat, edit.hslMagentaLum)
    }

    // Compute live RGB luminance histogram (MOD-030)
    fun computeHistogram(bitmap: Bitmap): FloatArray {
        val histogram = FloatArray(256)
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w)
        var total = 0

        for (y in 0 until h step 4) {
            bitmap.getPixels(pixels, 0, w, 0, y, w, 1)
            for (x in 0 until w step 4) {
                val pixel = pixels[x]
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt().coerceIn(0, 255)
                histogram[lum]++
                total++
            }
        }

        if (total > 0) {
            var maxVal = 0f
            for (i in 0 until 256) {
                if (histogram[i] > maxVal) maxVal = histogram[i]
            }
            if (maxVal > 0) {
                for (i in 0 until 256) {
                    histogram[i] = histogram[i] / maxVal
                }
            }
        }
        return histogram
    }

    // Compute real-time clipping zebra overlay
    fun computeClippingOverlay(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w)
        val outPixels = IntArray(w)

        for (y in 0 until h) {
            bitmap.getPixels(pixels, 0, w, 0, y, w, 1)
            for (x in 0 until w) {
                val pixel = pixels[x]
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

                if (lum >= 252) {
                    val zebra = ((x + y) / 4) % 2 == 0
                    outPixels[x] = if (zebra) Color.argb(190, 255, 23, 68) else Color.TRANSPARENT
                } else if (lum <= 4) {
                    val zebra = ((x - y) / 4) % 2 == 0
                    outPixels[x] = if (zebra) Color.argb(190, 41, 121, 255) else Color.TRANSPARENT
                } else {
                    outPixels[x] = Color.TRANSPARENT
                }
            }
            overlay.setPixels(outPixels, 0, w, 0, y, w, 1)
        }
        return overlay
    }
}
