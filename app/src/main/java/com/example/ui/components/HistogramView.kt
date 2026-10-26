package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextTertiary

@Composable
fun HistogramView(
    data: FloatArray,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudioSurfaceVariant.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (data.isEmpty()) return@Canvas
            val width = size.width
            val height = size.height

            // Background grid lines
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(width * 0.25f, 0f),
                end = Offset(width * 0.25f, height)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(width * 0.5f, 0f),
                end = Offset(width * 0.5f, height)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(width * 0.75f, 0f),
                end = Offset(width * 0.75f, height)
            )

            val path = Path()
            path.moveTo(0f, height)

            for (i in data.indices) {
                val x = (i / 255f) * width
                val y = height - (data[i] * height * 0.9f)
                path.lineTo(x, y)
            }
            path.lineTo(width, height)
            path.close()

            // Draw filled area with cyan gradient tone
            drawPath(
                path = path,
                color = AccentCyan.copy(alpha = 0.25f)
            )
            drawPath(
                path = path,
                color = AccentCyan.copy(alpha = 0.8f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Zone labels (Blacks, Shadows, Exposure/Midtones, Highlights, Whites)
        Text(
            text = "RGB LUMINANCE HISTOGRAM",
            color = TextTertiary,
            fontSize = 9.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
