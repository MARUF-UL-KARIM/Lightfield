package com.example

import com.example.engine.DngXmpParser
import com.example.model.CopySettingsFilter
import com.example.model.EditState
import com.example.model.MaskType
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class DngXmpParserTest {

    @Test
    fun testParseDngXmpCrsMetadata() {
        val sampleXmpXml = """
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
             <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
              <rdf:Description rdf:about=""
                xmlns:crs="http://ns.adobe.com/camera-raw-settings/1.0/"
                crs:Exposure2012="+0.75"
                crs:Contrast2012="+25"
                crs:Highlights2012="-40"
                crs:Shadows2012="+30"
                crs:Temperature="6500"
                crs:Tint="+10"
                crs:Vibrance="+20"
                crs:Saturation="-5"
                crs:HueAdjustmentRed="+15"
                crs:SaturationAdjustmentRed="+20"
                crs:Texture="+15"
                crs:Clarity2012="+10"
                crs:Dehaze="+5"
                crs:Sharpness="40">
                <crs:MaskGroupBasedCorrections>
                  <rdf:Seq>
                    <rdf:li>
                      <rdf:Description crs:Correction="Subject" crs:LocalExposure2012="+0.4" />
                    </rdf:li>
                  </rdf:Seq>
                </crs:MaskGroupBasedCorrections>
              </rdf:Description>
             </rdf:RDF>
            </x:xmpmeta>
        """.trimIndent()

        val stream = ByteArrayInputStream(sampleXmpXml.toByteArray())
        val parsed = DngXmpParser.parseDngStream(stream)

        assertNotNull(parsed)
        assertEquals(0.75f, parsed!!.exposure, 0.01f)
        assertEquals(25f, parsed.contrast, 0.01f)
        assertEquals(-40f, parsed.highlights, 0.01f)
        assertEquals(30f, parsed.shadows, 0.01f)
        assertEquals(15f, parsed.hslRedHue, 0.01f)
        assertEquals(40f, parsed.sharpening, 0.01f)
        assertTrue(parsed.dngMasks.isNotEmpty())
        assertEquals(MaskType.SUBJECT, parsed.dngMasks.first().maskType)
    }

    @Test
    fun testCopySettingsFilterPreservesSelectiveValues() {
        val filter = CopySettingsFilter(
            copyLight = true,
            copyColor = false,
            copyMasking = true
        )

        assertTrue(filter.copyLight)
        assertFalse(filter.copyColor)
        assertTrue(filter.copyMasking)
    }
}
