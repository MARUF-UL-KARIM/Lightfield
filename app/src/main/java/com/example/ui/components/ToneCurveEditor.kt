package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurveChannel
import com.example.model.EditState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ToneCurveEditor(
    editState: EditState,
    onCurveChanged: (channel: CurveChannel, Float, Float, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChannel by remember { mutableStateOf(CurveChannel.RGB) }

    val (black, shadow, mid, highlight, white) = when (selectedChannel) {
        CurveChannel.RGB -> listOf(editState.curveBlackPoint, editState.curveShadowPoint, editState.curveMidPoint, editState.curveHighlightPoint, editState.curveWhitePoint)
        CurveChannel.RED -> listOf(editState.curveRedBlackPoint, editState.curveRedShadowPoint, editState.curveRedMidPoint, editState.curveRedHighlightPoint, editState.curveRedWhitePoint)
        CurveChannel.GREEN -> listOf(editState.curveGreenBlackPoint, editState.curveGreenShadowPoint, editState.curveGreenMidPoint, editState.curveGreenHighlightPoint, editState.curveGreenWhitePoint)
        CurveChannel.BLUE -> listOf(editState.curveBlueBlackPoint, editState.curveBlueShadowPoint, editState.curveBlueMidPoint, editState.curveBlueHighlightPoint, editState.curveBlueWhitePoint)
    }

    val channelColor = when (selectedChannel) {
        CurveChannel.RGB -> AccentCyan
        CurveChannel.RED -> Color(0xFFEF5350)
        CurveChannel.GREEN -> Color(0xFF66BB6A)
        CurveChannel.BLUE -> Color(0xFF42A5F5)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StudioSurfaceVariant)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PARAMETRIC & POINT TONE CURVE",
                fontSize = 11.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    onCurveChanged(selectedChannel, 0f, 0.25f, 0.5f, 0.75f, 1.0f)
                }
            ) {
                Text("Linear", fontSize = 11.sp, color = AccentCyan)
            }
        }

        // Channel Selectors: RGB, Red, Green, Blue
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            CurveChannel.values().forEach { ch ->
                FilterChip(
                    selected = selectedChannel == ch,
                    onClick = { selectedChannel = ch },
                    label = { Text(ch.label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when(ch) {
                            CurveChannel.RGB -> AccentCyan
                            CurveChannel.RED -> Color(0xFFEF5350)
                            CurveChannel.GREEN -> Color(0xFF66BB6A)
                            CurveChannel.BLUE -> Color(0xFF42A5F5)
                        },
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(StudioSurfaceElevated)
                .pointerInput(selectedChannel) {
                    detectDragGestures { change, _ ->
                        val w = size.width
                        val h = size.height
                        val normX = (change.position.x / w).coerceIn(0f, 1f)
                        val normY = (1f - (change.position.y / h)).coerceIn(0f, 1f)

                        var b = black
                        var s = shadow
                        var m = mid
                        var hi = highlight
                        var wh = white

                        when {
                            normX < 0.2f -> b = normY
                            normX < 0.4f -> s = normY
                            normX < 0.6f -> m = normY
                            normX < 0.8f -> hi = normY
                            else -> wh = normY
                        }
                        onCurveChanged(selectedChannel, b, s, m, hi, wh)
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Draw 4x4 Grid
                for (i in 1..3) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(w * (i / 4f), 0f),
                        end = Offset(w * (i / 4f), h)
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, h * (i / 4f)),
                        end = Offset(w, h * (i / 4f))
                    )
                }

                // Linear Reference
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, h),
                    end = Offset(w, 0f),
                    strokeWidth = 1.dp.toPx()
                )

                // Spline Path
                val path = Path()
                val p0 = Offset(0f, h * (1f - black))
                val p1 = Offset(w * 0.25f, h * (1f - shadow))
                val p2 = Offset(w * 0.50f, h * (1f - mid))
                val p3 = Offset(w * 0.75f, h * (1f - highlight))
                val p4 = Offset(w, h * (1f - white))

                path.moveTo(p0.x, p0.y)
                path.cubicTo(p1.x, p1.y, p1.x, p1.y, p2.x, p2.y)
                path.cubicTo(p3.x, p3.y, p3.x, p3.y, p4.x, p4.y)

                drawPath(
                    path = path,
                    color = channelColor,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Control points
                listOf(p0, p1, p2, p3, p4).forEach { pt ->
                    drawCircle(color = channelColor, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
                }
            }
        }
        Text(
            text = "Channel: ${selectedChannel.label} • Blacks • Shadows • Midtones • Highlights • Whites",
            fontSize = 9.sp,
            color = TextTertiary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
