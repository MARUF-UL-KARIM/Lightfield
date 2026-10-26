package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SplitComparisonCanvas(
    originalBitmap: Bitmap,
    editedBitmap: Bitmap,
    splitFraction: Float,
    onSplitFractionChange: (Float) -> Unit,
    showZebraClipping: Boolean,
    clippingZebraBitmap: Bitmap?,
    maskOverlayBitmap: Bitmap? = null,
    showGrid: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()

        val imgW = editedBitmap.width.toFloat()
        val imgH = editedBitmap.height.toFloat()
        val scale = min(totalWidth / imgW, totalHeight / imgH)
        val dstW = imgW * scale
        val dstH = imgH * scale
        val dstLeft = (totalWidth - dstW) / 2f
        val dstTop = (totalHeight - dstH) / 2f

        val splitX = (dstLeft + splitFraction * dstW).coerceIn(dstLeft, dstLeft + dstW)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalWidth, totalHeight, dstW, dstLeft) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        if (dstW > 0) {
                            val newFraction = ((change.position.x - dstLeft) / dstW).coerceIn(0.05f, 0.95f)
                            onSplitFractionChange(newFraction)
                        }
                    }
                }
        ) {
            val dstIntOffset = androidx.compose.ui.unit.IntOffset(dstLeft.toInt(), dstTop.toInt())
            val dstIntSize = IntSize(dstW.toInt(), dstH.toInt())

            // 1. Draw Edited Bitmap (Right or entire photo area)
            drawImage(
                image = editedBitmap.asImageBitmap(),
                dstOffset = dstIntOffset,
                dstSize = dstIntSize
            )

            // 2. Draw Clipping Zebras overlay if enabled (clipped to After side)
            if (showZebraClipping && clippingZebraBitmap != null) {
                val clipAfter = Path().apply {
                    addRect(Rect(splitX, dstTop, dstLeft + dstW, dstTop + dstH))
                }
                clipPath(clipAfter) {
                    drawImage(
                        image = clippingZebraBitmap.asImageBitmap(),
                        dstOffset = dstIntOffset,
                        dstSize = dstIntSize
                    )
                }
            }

            // 3. Clip and Draw Original Bitmap on Left Side (Before negative)
            val clipPathOriginal = Path().apply {
                addRect(Rect(dstLeft, dstTop, splitX, dstTop + dstH))
            }
            clipPath(clipPathOriginal) {
                drawImage(
                    image = originalBitmap.asImageBitmap(),
                    dstOffset = dstIntOffset,
                    dstSize = dstIntSize
                )
            }

            // 4. Draw Vertical Divider Hairline
            drawLine(
                color = Color.White.copy(alpha = 0.95f),
                start = Offset(splitX, dstTop),
                end = Offset(splitX, dstTop + dstH),
                strokeWidth = 2.dp.toPx()
            )

            // 5. Rule of Thirds Grid if enabled
            if (showGrid) {
                val gridColor = Color.White.copy(alpha = 0.25f)
                drawLine(gridColor, Offset(dstLeft + dstW / 3f, dstTop), Offset(dstLeft + dstW / 3f, dstTop + dstH), 1.dp.toPx())
                drawLine(gridColor, Offset(dstLeft + dstW * 2f / 3f, dstTop), Offset(dstLeft + dstW * 2f / 3f, dstTop + dstH), 1.dp.toPx())
                drawLine(gridColor, Offset(dstLeft, dstTop + dstH / 3f), Offset(dstLeft + dstW, dstTop + dstH / 3f), 1.dp.toPx())
                drawLine(gridColor, Offset(dstLeft, dstTop + dstH * 2f / 3f), Offset(dstLeft + dstW, dstTop + dstH * 2f / 3f), 1.dp.toPx())
            }
        }

        // Draggable Floating Pill Handle at center of split line
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(splitX.roundToInt() - 16, 0)
                }
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.85f))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swipe Divider",
                tint = AccentCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        // BEFORE (Negative) Label on Left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text("BEFORE", color = AccentAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        // AFTER (Edited) Label on Right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text("AFTER", color = AccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
