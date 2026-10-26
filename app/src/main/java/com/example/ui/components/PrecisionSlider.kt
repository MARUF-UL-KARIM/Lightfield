package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SliderTrack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun PrecisionSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    defaultValue: Float = 0.0f,
    onValueChange: (Float) -> Unit,
    valueFormatter: (Float) -> String = {
        if (it > 0) "+${String.format("%.1f", it)}" else String.format("%.1f", it)
    },
    testTag: String = "slider_$label"
) {
    val isBipolar = valueRange.start < 0f && valueRange.endInclusive > 0f
    val isModified = kotlin.math.abs(value - defaultValue) > 0.01f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isModified) TextPrimary else TextSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = valueFormatter(value),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isModified) AccentCyan else TextTertiary
                )
                if (isModified) {
                    Text(
                        text = "Reset",
                        fontSize = 9.sp,
                        color = TextTertiary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onValueChange(defaultValue)
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Custom Sleek Hairline Track (2dp height) with glowing 12dp micro-thumb
        // and accessible 44dp vertical touch target
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .pointerInput(valueRange) {
                    detectTapGestures { offset ->
                        val width = size.width
                        if (width > 0) {
                            val fraction = (offset.x / width).coerceIn(0f, 1f)
                            val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                            onValueChange(newValue)
                        }
                    }
                }
                .pointerInput(valueRange) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val width = size.width
                        if (width > 0) {
                            val fraction = (change.position.x / width).coerceIn(0f, 1f)
                            val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                            onValueChange(newValue)
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(38.dp)) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                val rangeSpan = valueRange.endInclusive - valueRange.start
                val currentFraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
                val thumbX = currentFraction * w

                // 1. Sleek Inactive Hairline (1.5dp)
                drawLine(
                    color = SliderTrack,
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.75.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 2. Active illuminated segment
                if (isBipolar) {
                    val zeroFraction = ((0f - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
                    val zeroX = zeroFraction * w

                    // Center zero tick
                    drawLine(
                        color = TextTertiary.copy(alpha = 0.8f),
                        start = Offset(zeroX, midY - 4.dp.toPx()),
                        end = Offset(zeroX, midY + 4.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Active bar from center to thumb
                    if (kotlin.math.abs(thumbX - zeroX) > 1f) {
                        drawLine(
                            color = AccentCyan,
                            start = Offset(zeroX, midY),
                            end = Offset(thumbX, midY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    // Unipolar bar (0 to Max)
                    drawLine(
                        color = AccentCyan,
                        start = Offset(0f, midY),
                        end = Offset(thumbX, midY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 3. Compact glowing thumb
                // Soft cyan glow aura
                drawCircle(
                    color = AccentCyan.copy(alpha = 0.22f),
                    radius = 8.5.dp.toPx(),
                    center = Offset(thumbX, midY)
                )
                // Crisp thumb core
                drawCircle(
                    color = AccentCyan,
                    radius = 5.dp.toPx(),
                    center = Offset(thumbX, midY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(thumbX, midY)
                )
            }
        }
    }
}
