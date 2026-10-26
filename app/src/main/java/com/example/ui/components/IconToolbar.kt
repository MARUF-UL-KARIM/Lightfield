package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.EditorTab

data class ToolBarItem(
    val tab: EditorTab,
    val icon: ImageVector,
    val label: String
)

@Composable
fun IconToolbar(
    activeTab: EditorTab,
    onTabSelected: (EditorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        ToolBarItem(EditorTab.LIGHT, Icons.Default.LightMode, "Light"),
        ToolBarItem(EditorTab.COLOR, Icons.Default.ColorLens, "Color"),
        ToolBarItem(EditorTab.CURVES, Icons.Default.ShowChart, "Curves"),
        ToolBarItem(EditorTab.EFFECTS, Icons.Default.Grain, "Effects"),
        ToolBarItem(EditorTab.DETAIL, Icons.Default.Details, "Detail"),
        ToolBarItem(EditorTab.OPTICS, Icons.Default.Camera, "Optics"),
        ToolBarItem(EditorTab.CROP, Icons.Default.Crop, "Crop"),
        ToolBarItem(EditorTab.MASKING, Icons.Default.AutoFixHigh, "Masks"),
        ToolBarItem(EditorTab.HEALING, Icons.Default.Healing, "Heal"),
        ToolBarItem(EditorTab.PORTRAIT, Icons.Default.Face, "Portrait"),
        ToolBarItem(EditorTab.PRESETS, Icons.Default.Style, "Presets")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(StudioSurface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = activeTab == item.tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) StudioSurfaceElevated else Color.Transparent)
                    .clickable { onTabSelected(item.tab) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) AccentCyan else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = item.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) AccentCyan else TextTertiary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
