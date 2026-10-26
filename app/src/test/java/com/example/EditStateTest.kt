package com.example

import android.graphics.Bitmap
import com.example.engine.DngXmpParser
import com.example.engine.ImageProcessingEngine
import com.example.model.AspectRatioType
import com.example.model.DngMaskCorrection
import com.example.model.EditState
import com.example.model.MaskType
import com.example.viewmodel.ExportOptions
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class EditStateTest {

    @Test
    fun testDefaultEditStateIsNotModified() {
        val state = EditState(photoId = "test_01")
        assertFalse(state.isModified())
        assertEquals(0.0f, state.exposure, 0.001f)
        assertEquals(0.0f, state.contrast, 0.001f)
        assertEquals(AspectRatioType.ORIGINAL, state.aspectRatio)
        assertEquals(MaskType.NONE, state.activeMaskType)
        assertEquals(0.0f, state.maskContrast, 0.001f)
        assertEquals(0.0f, state.maskClarity, 0.001f)
    }

    @Test
    fun testModifiedStateDetection() {
        val state = EditState(photoId = "test_02", exposure = 1.5f)
        assertTrue(state.isModified())
    }

    @Test
    fun testNonDestructiveCopy() {
        val original = EditState(photoId = "test_03", exposure = 0.5f, contrast = 10f)
        val modified = original.copy(highlights = -20f)
        
        assertEquals(0.5f, modified.exposure, 0.001f)
        assertEquals(10f, modified.contrast, 0.001f)
        assertEquals(-20f, modified.highlights, 0.001f)
        assertEquals(0.0f, original.highlights, 0.001f)
    }

    @Test
    fun testMaskAdjustmentsAndDngMasks() {
        val dngMask = DngMaskCorrection(
            maskType = MaskType.SUBJECT,
            name = "Adobe AI Subject Mask",
            localExposure = 0.45f,
            localWarmth = 15f,
            localSaturation = 20f,
            localContrast = 10f,
            localHighlights = 12f
        )
        val state = EditState(
            photoId = "test_04",
            activeMaskType = MaskType.SUBJECT,
            maskExposure = dngMask.localExposure,
            maskWarmth = dngMask.localWarmth,
            maskSaturation = dngMask.localSaturation,
            maskContrast = dngMask.localContrast,
            maskClarity = dngMask.localHighlights,
            dngMasks = listOf(dngMask)
        )

        assertTrue(state.isModified())
        assertEquals(MaskType.SUBJECT, state.activeMaskType)
        assertEquals(0.45f, state.maskExposure, 0.001f)
        assertEquals(15f, state.maskWarmth, 0.001f)
        assertEquals(20f, state.maskSaturation, 0.001f)
        assertEquals(10f, state.maskContrast, 0.001f)
        assertEquals(12f, state.maskClarity, 0.001f)
        assertEquals(1, state.dngMasks.size)
    }

    @Test
    fun testDngXmpParserWithSubjectMask() {
        val sampleXmp = """
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
             <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description xmlns:crs="http://ns.adobe.com/camera-raw-settings/1.0/"
               crs:Exposure2012="+0.30"
               crs:Contrast2012="+15"
               crs:Highlights2012="-25"
               crs:Shadows2012="+20"
               crs:MaskGroupBasedCorrections="Subject"
               crs:LocalExposure2012="+0.50"
               crs:LocalTemperature="+10.0"
               crs:LocalSaturation="+15.0"
               crs:LocalContrast2012="+20.0"
               crs:LocalClarity2012="+12.0">
              </rdf:Description>
             </rdf:RDF>
            </x:xmpmeta>
        """.trimIndent()

        val parsed = DngXmpParser.parseDngStream(ByteArrayInputStream(sampleXmp.toByteArray()))
        assertNotNull(parsed)
        assertEquals(0.30f, parsed!!.exposure, 0.01f)
        assertEquals(15f, parsed.contrast, 0.01f)
        assertEquals(-25f, parsed.highlights, 0.01f)
        assertEquals(20f, parsed.shadows, 0.01f)

        // Verify active mask was automatically populated with DNG values
        assertEquals(MaskType.SUBJECT, parsed.activeMaskType)
        assertEquals(0.50f, parsed.maskExposure, 0.01f)
        assertEquals(10.0f, parsed.maskWarmth, 0.01f)
        assertEquals(15.0f, parsed.maskSaturation, 0.01f)
        assertEquals(20.0f, parsed.maskContrast, 0.01f)
        assertEquals(12.0f, parsed.maskClarity, 0.01f)
        assertEquals(1, parsed.dngMasks.size)
    }

    @Test
    fun testExportOptionsConfiguration() {
        val jpegOptions = ExportOptions(format = "JPEG", quality = 90, dimensionPreset = "2048px (Social)")
        assertEquals("JPEG", jpegOptions.format)
        assertEquals(90, jpegOptions.quality)
        assertEquals("2048px (Social)", jpegOptions.dimensionPreset)

        val webpOptions = ExportOptions(format = "WEBP", quality = 85, addWatermark = true, watermarkText = "Studio")
        assertEquals("WEBP", webpOptions.format)
        assertTrue(webpOptions.addWatermark)
        assertEquals("Studio", webpOptions.watermarkText)
    }
}
