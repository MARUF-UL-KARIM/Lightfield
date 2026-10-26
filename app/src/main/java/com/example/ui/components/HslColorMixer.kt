package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary

enum class HslChannel(val label: String, val color: Color) {
    RED("Red", Color(0xFFE53935)),
    ORANGE("Orange", Color(0xFFFB8C00)),
    YELLOW("Yellow", Color(0xFFFDD835)),
    GREEN("Green", Color(0xFF43A047)),
    AQUA("Aqua", Color(0xFF00ACC1)),
    BLUE("Blue", Color(0xFF1E88E5)),
    PURPLE("Purple", Color(0xFF8E24AA)),
    MAGENTA("Magenta", Color(0xFFD81B60))
}

@Composable
fun HslColorMixer(
    editState: EditState,
    onChannelChange: (HslChannel, hue: Float, sat: Float, lum: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChannel by remember { mutableStateOf(HslChannel.RED) }

    val currentHue = when (selectedChannel) {
        HslChannel.RED -> editState.hslRedHue
        HslChannel.ORANGE -> editState.hslOrangeHue
        HslChannel.YELLOW -> editState.hslYellowHue
        HslChannel.GREEN -> editState.hslGreenHue
        HslChannel.AQUA -> editState.hslAquaHue
        HslChannel.BLUE -> editState.hslBlueHue
        HslChannel.PURPLE -> editState.hslPurpleHue
        HslChannel.MAGENTA -> editState.hslMagentaHue
    }

    val currentSat = when (selectedChannel) {
        HslChannel.RED -> editState.hslRedSat
        HslChannel.ORANGE -> editState.hslOrangeSat
        HslChannel.YELLOW -> editState.hslYellowSat
        HslChannel.GREEN -> editState.hslGreenSat
        HslChannel.AQUA -> editState.hslAquaSat
        HslChannel.BLUE -> editState.hslBlueSat
        HslChannel.PURPLE -> editState.hslPurpleSat
        HslChannel.MAGENTA -> editState.hslMagentaSat
    }

    val currentLum = when (selectedChannel) {
        HslChannel.RED -> editState.hslRedLum
        HslChannel.ORANGE -> editState.hslOrangeLum
        HslChannel.YELLOW -> editState.hslYellowLum
        HslChannel.GREEN -> editState.hslGreenLum
        HslChannel.AQUA -> editState.hslAquaLum
        HslChannel.BLUE -> editState.hslBlueLum
        HslChannel.PURPLE -> editState.hslPurpleLum
        HslChannel.MAGENTA -> editState.hslMagentaLum
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StudioSurfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            text = "8-CHANNEL COLOR MIXER (HSL)",
            fontSize = 11.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 8 Channel Swatches
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HslChannel.values().forEach { channel ->
                val isSelected = channel == selectedChannel
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedChannel = channel }
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(channel.color)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = channel.label.take(3),
                        fontSize = 9.sp,
                        color = if (isSelected) AccentCyan else TextTertiary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sliders for Selected Channel
        PrecisionSlider(
            label = "${selectedChannel.label} Hue",
            value = currentHue,
            valueRange = -100f..100f,
            onValueChange = { onChannelChange(selectedChannel, it, currentSat, currentLum) }
        )
        PrecisionSlider(
            label = "${selectedChannel.label} Saturation",
            value = currentSat,
            valueRange = -100f..100f,
            onValueChange = { onChannelChange(selectedChannel, currentHue, it, currentLum) }
        )
        PrecisionSlider(
            label = "${selectedChannel.label} Luminance",
            value = currentLum,
            valueRange = -100f..100f,
            onValueChange = { onChannelChange(selectedChannel, currentHue, currentSat, it) }
        )
    }
}
