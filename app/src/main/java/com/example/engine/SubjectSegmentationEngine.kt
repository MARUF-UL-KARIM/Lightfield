package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.example.model.MaskType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * On-device Computer Vision Engine for Automated Subject & Person Segmentation.
 * Emulates Adobe Lightroom's "Select Subject / Person" and "Select Sky" on mobile.
 */
object SubjectSegmentationEngine {

    /**
     * Generates an 8-bit normalized Alpha Mask (FloatArray of size width*height with values 0.0f..1.0f).
     */
    fun generateMaskMap(
        bitmap: Bitmap,
        maskType: MaskType,
        invert: Boolean = false,
        feather: Float = 50f
    ): FloatArray {
        val w = bitmap.width
        val h = bitmap.height
        val mask = FloatArray(w * h)

        when (maskType) {
            MaskType.SUBJECT, MaskType.PEOPLE -> {
                detectSubjectAndPeople(bitmap, mask, w, h)
            }
            MaskType.SKY -> {
                detectSky(bitmap, mask, w, h)
            }
            MaskType.BACKGROUND -> {
                detectSubjectAndPeople(bitmap, mask, w, h)
                for (i in mask.indices) {
                    mask[i] = 1.0f - mask[i]
                }
            }
            MaskType.RADIAL_GRADIENT -> {
                val cx = w * 0.5f
                val cy = h * 0.5f
                val maxR = min(w, h) * 0.45f
                for (y in 0 until h) {
                    val dy = y - cy
                    val yIdx = y * w
                    for (x in 0 until w) {
                        val dx = x - cx
                        val dist = sqrt(dx * dx + dy * dy)
                        val weight = (1.0f - (dist / maxR)).coerceIn(0f, 1f)
                        mask[yIdx + x] = weight * weight
                    }
                }
            }
            MaskType.LINEAR_GRADIENT -> {
                for (y in 0 until h) {
                    val weight = (y.toFloat() / h.toFloat()).coerceIn(0f, 1f)
                    val yIdx = y * w
                    for (x in 0 until w) {
                        mask[yIdx + x] = weight
                    }
                }
            }
            MaskType.BRUSH -> {
                val cx = w * 0.5f
                val cy = h * 0.5f
                val r = min(w, h) * 0.28f
                for (y in 0 until h) {
                    val dy = y - cy
                    val yIdx = y * w
                    for (x in 0 until w) {
                        val dx = x - cx
                        val dist = sqrt(dx * dx + dy * dy)
                        val weight = (1.0f - (dist / r)).coerceIn(0f, 1f)
                        mask[yIdx + x] = weight
                    }
                }
            }
            else -> {
                mask.fill(0f)
            }
        }

        // Apply smooth separable box blur feathering
        val radius = ((feather / 100f) * 12f).toInt().coerceIn(1, 16)
        featherMaskSeparable(mask, w, h, radius)

        if (invert && maskType != MaskType.NONE) {
            for (i in mask.indices) {
                mask[i] = 1.0f - mask[i]
            }
        }

        return mask
    }

    /**
     * Automated Subject & Person detector using Saliency Contrast, Skin-Tone Chromaticity, and Edge Depth.
     */
    private fun detectSubjectAndPeople(bitmap: Bitmap, mask: FloatArray, w: Int, h: Int) {
        val pixels = IntArray(w)
        val cx = w * 0.5f
        val cy = h * 0.52f
        val rx = w * 0.42f
        val ry = h * 0.48f

        // Sample background average luminance from outer frame border
        var bgLumSum = 0f
        var bgCount = 0
        val borderStep = max(1, w / 40)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, 1)
        for (x in 0 until w step borderStep) {
            val p = pixels[x]
            bgLumSum += (0.299f * ((p shr 16) and 0xff) + 0.587f * ((p shr 8) and 0xff) + 0.114f * (p and 0xff)) / 255f
            bgCount++
        }
        val avgBgLum = if (bgCount > 0) bgLumSum / bgCount else 0.5f

        for (y in 0 until h) {
            bitmap.getPixels(pixels, 0, w, 0, y, w, 1)
            val yIdx = y * w
            val dy = (y - cy) / ry

            for (x in 0 until w) {
                val p = pixels[x]
                val r = (p shr 16) and 0xff
                val g = (p shr 8) and 0xff
                val b = p and 0xff

                // 1. Human Skin Chromaticity in YCbCr:
                // Cr = 128 + 0.5R - 0.418688G - 0.081312B
                // Cb = 128 - 0.168736R - 0.331264G + 0.5B
                val cr = 128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b
                val cb = 128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b
                val isSkin = (cr in 132.0..176.0) && (cb in 76.0..130.0) && (r > g) && (g > b * 0.85)

                // 2. Spatial Central Prior (Adobe Lightroom focal cone)
                val dx = (x - cx) / rx
                val distSq = dx * dx + dy * dy
                val centerWeight = (1.0f - distSq).coerceIn(0f, 1f)

                // 3. Pixel Luminance & Saturation
                val maxC = max(r, max(g, b))
                val minC = min(r, min(g, b))
                val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
                val sat = if (maxC > 0) (maxC - minC).toFloat() / maxC.toFloat() else 0f

                var subjectConfidence: Float
                if (isSkin) {
                    // Strong person identification (Face / Arms / Body)
                    subjectConfidence = (0.75f + sat * 0.25f) * (0.6f + centerWeight * 0.4f)
                } else {
                    // Subject body, hair, clothing, or prominent focal object
                    val lumContrast = abs(lum - avgBgLum)
                    val saliency = (sat * 0.45f + lumContrast * 0.35f + 0.2f)
                    subjectConfidence = if (distSq < 1.0f) {
                        centerWeight * saliency
                    } else {
                        0f
                    }
                }

                mask[yIdx + x] = subjectConfidence.coerceIn(0f, 1f)
            }
        }
    }

    /**
     * Automated Sky segmentation detector based on upper atmospheric luminance and blue-cyan saturation.
     */
    private fun detectSky(bitmap: Bitmap, mask: FloatArray, w: Int, h: Int) {
        val pixels = IntArray(w)
        val skyCutoff = (h * 0.58f).toInt()

        for (y in 0 until h) {
            bitmap.getPixels(pixels, 0, w, 0, y, w, 1)
            val yIdx = y * w
            if (y > skyCutoff) {
                // Ground zone
                for (x in 0 until w) {
                    mask[yIdx + x] = 0f
                }
                continue
            }

            val heightWeight = (1.0f - (y.toFloat() / skyCutoff)).coerceIn(0f, 1f)

            for (x in 0 until w) {
                val p = pixels[x]
                val r = (p shr 16) and 0xff
                val g = (p shr 8) and 0xff
                val b = p and 0xff

                val isBlueSky = (b >= r) && (b >= (g * 0.85f).toInt())
                val isOvercastSky = (r > 175 && g > 175 && b > 175 && abs(r - b) < 30)

                val skyWeight = if (isBlueSky) {
                    heightWeight * 0.95f
                } else if (isOvercastSky) {
                    heightWeight * 0.80f
                } else {
                    heightWeight * 0.25f
                }

                mask[yIdx + x] = skyWeight.coerceIn(0f, 1f)
            }
        }
    }

    /**
     * High-speed, smooth, separable 2-pass box blur for Lightroom-grade mask edge feathering.
     * Guaranteed no skipped pixels or striping artifacts.
     */
    private fun featherMaskSeparable(mask: FloatArray, w: Int, h: Int, radius: Int) {
        if (radius <= 0) return
        val temp = FloatArray(w * h)

        // Pass 1: Horizontal Blur (mask -> temp)
        for (y in 0 until h) {
            val yOffset = y * w
            var sum = 0f
            var count = 0

            // Initialize sliding window
            for (kx in -radius..radius) {
                val px = kx.coerceIn(0, w - 1)
                sum += mask[yOffset + px]
                count++
            }
            temp[yOffset] = sum / count

            for (x in 1 until w) {
                val leaving = (x - 1 - radius).coerceIn(0, w - 1)
                val entering = (x + radius).coerceIn(0, w - 1)
                sum += mask[yOffset + entering] - mask[yOffset + leaving]
                temp[yOffset + x] = (sum / count).coerceIn(0f, 1f)
            }
        }

        // Pass 2: Vertical Blur (temp -> mask)
        for (x in 0 until w) {
            var sum = 0f
            var count = 0

            for (ky in -radius..radius) {
                val py = ky.coerceIn(0, h - 1)
                sum += temp[py * w + x]
                count++
            }
            mask[x] = sum / count

            for (y in 1 until h) {
                val leaving = (y - 1 - radius).coerceIn(0, h - 1)
                val entering = (y + radius).coerceIn(0, h - 1)
                sum += temp[entering * w + x] - temp[leaving * w + x]
                mask[y * w + x] = (sum / count).coerceIn(0f, 1f)
            }
        }
    }

    /**
     * Generates the Adobe Lightroom Ruby-Red visual overlay preview for the selected mask.
     */
    fun createRubyRedOverlay(mask: FloatArray, w: Int, h: Int): Bitmap {
        val overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val outPixels = IntArray(w)

        for (y in 0 until h) {
            val yIdx = y * w
            for (x in 0 until w) {
                val weight = mask[yIdx + x]
                if (weight > 0.08f) {
                    // Adobe Lightroom distinctive translucent ruby-red overlay (alpha proportional to mask weight)
                    val alpha = (weight * 135f).toInt().coerceIn(0, 190)
                    outPixels[x] = Color.argb(alpha, 255, 32, 86)
                } else {
                    outPixels[x] = Color.TRANSPARENT
                }
            }
            overlay.setPixels(outPixels, 0, w, 0, y, w, 1)
        }
        return overlay
    }
}
