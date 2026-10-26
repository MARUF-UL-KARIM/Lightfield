package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhotoItem
import com.example.ui.components.AdjustmentToolPanel
import com.example.ui.components.HistogramView
import com.example.ui.components.IconToolbar
import com.example.ui.components.SplitComparisonCanvas
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.StudioDarkBackground
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.EditorTab
import com.example.viewmodel.EditorViewModel
import com.example.viewmodel.ExportOptions
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEditorScreen(
    viewModel: EditorViewModel,
    onBackToHome: () -> Unit,
    onImportClick: () -> Unit,
    onPickDngToApply: () -> Unit
) {
    val photos by viewModel.photos.collectAsState()
    val activeAlbumFilter by viewModel.activeAlbumFilter.collectAsState()
    val selectedPhoto by viewModel.selectedPhoto.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val currentEditState by viewModel.currentEditState.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val originalBitmap by viewModel.originalBitmap.collectAsState()
    val clippingZebraBitmap by viewModel.clippingZebraBitmap.collectAsState()
    val maskPreviewOverlay by viewModel.maskPreviewOverlay.collectAsState()
    val histogramData by viewModel.histogramData.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val progress by viewModel.processingProgress.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()
    val batchSelection by viewModel.selectedPhotoIdsForBatch.collectAsState()
    val historyTimeline by viewModel.historyTimeline.collectAsState()
    val cloudInfo by viewModel.cloudInfo.collectAsState()
    val showGrid by viewModel.showRuleOfThirdsGrid.collectAsState()
    val customPresets by viewModel.customPresets.collectAsState()

    // Interactive Pro Controls
    val showSplitComparison by viewModel.showSplitComparison.collectAsState()
    val splitFraction by viewModel.splitFraction.collectAsState()
    val showZebraClipping by viewModel.showZebraClipping.collectAsState()

    var isToolDrawerOpen by remember { mutableStateOf(true) }
    var showHistogram by remember { mutableStateOf(true) }

    var showCopySettingsDialog by remember { mutableStateOf(false) }
    var copyFilter by remember { mutableStateOf(com.example.model.CopySettingsFilter()) }
    var showExifDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    var exportQuality by remember { mutableStateOf(95f) }
    var exportFormat by remember { mutableStateOf("JPEG") }
    var exportDimension by remember { mutableStateOf("Original") }
    var exportWatermark by remember { mutableStateOf(false) }
    var watermarkText by remember { mutableStateOf("Shot on Lightfield") }
    var exportedFileForShare by remember { mutableStateOf<File?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotification()
        }
    }

    val filteredPhotos = remember(photos, activeAlbumFilter) {
        if (activeAlbumFilter == "All Photos") photos
        else photos.filter { it.album.equals(activeAlbumFilter, ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = StudioDarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // TOP BAR: Clean, pro header with direct access to Auto AI, Split View, Zebras & Export
            TopEditorBar(
                photo = selectedPhoto,
                canUndo = canUndo,
                canRedo = canRedo,
                showSplit = showSplitComparison,
                showZebras = showZebraClipping,
                onBack = onBackToHome,
                onApplyDngClick = onPickDngToApply,
                onToggleSplit = { viewModel.toggleSplitComparison() },
                onToggleZebras = { viewModel.toggleZebraClipping() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onHistoryClick = { showHistoryDialog = true },
                onCloudClick = { showCloudDialog = true },
                onSettingsClick = { showSettingsDialog = true },
                onExifClick = { showExifDialog = true },
                onAiEnhance = { viewModel.runAiSmartEnhance() },
                onExportClick = { showExportDialog = true },
                onCopySettings = { showCopySettingsDialog = true },
                onPasteSettings = { viewModel.pasteSettingsToSelected(copyFilter) }
            )

            // FILMSTRIP CAROUSEL WITH ALBUM PILLS (Compact & Sleek)
            CompactFilmstripBar(
                photos = filteredPhotos,
                selectedPhoto = selectedPhoto,
                activeAlbumFilter = activeAlbumFilter,
                batchSelection = batchSelection,
                onFilterSelected = { viewModel.setAlbumFilter(it) },
                onPhotoSelect = { viewModel.selectPhoto(it) },
                onToggleBatch = { viewModel.toggleBatchSelection(it) },
                onImportClick = onImportClick
            )

            // IMAGE CANVAS VIEWPORT (With Interactive Draggable Swipe Split Divider & Zebras)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    if (showSplitComparison && originalBitmap != null) {
                        SplitComparisonCanvas(
                            originalBitmap = originalBitmap!!,
                            editedBitmap = previewBitmap!!,
                            splitFraction = splitFraction,
                            onSplitFractionChange = { viewModel.setSplitFraction(it) },
                            showZebraClipping = showZebraClipping,
                            clippingZebraBitmap = clippingZebraBitmap,
                            maskOverlayBitmap = maskPreviewOverlay,
                            showGrid = showGrid,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Standard Viewport with Ruby-Red Mask Overlay
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = "Active photo viewport",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (maskPreviewOverlay != null) {
                                Image(
                                    bitmap = maskPreviewOverlay!!.asImageBitmap(),
                                    contentDescription = "AI Subject Mask Overlay",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator(color = AccentCyan)
                }

                // Live Histogram overlay (Collapsible)
                if (showHistogram) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .width(115.dp)
                    ) {
                        HistogramView(data = histogramData)
                    }
                }

                // Processing indicator
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(StudioSurface.copy(alpha = 0.85f))
                            .padding(6.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // TOOL DRAWER TOGGLE BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .clickable { isToolDrawerOpen = !isToolDrawerOpen }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeTab.label.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (currentEditState.appliedPresetName != null) {
                        Text(
                            text = "• ${currentEditState.appliedPresetName}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isToolDrawerOpen) "Hide Controls" else "Show Controls",
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                    Icon(
                        imageVector = if (isToolDrawerOpen) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = "Toggle Drawer",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // EXPANDABLE ADJUSTMENT TOOL PANEL (With Sleek Hairline Sliders)
            AnimatedVisibility(
                visible = isToolDrawerOpen,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(StudioDarkBackground)
                ) {
                    AdjustmentToolPanel(
                        tab = activeTab,
                        editState = currentEditState,
                        isProcessing = isProcessing,
                        progress = progress,
                        customPresets = customPresets,
                        onUpdateEdit = { transform -> viewModel.updateEdit("Slider modified", transform) },
                        onRunAiDenoise = { viewModel.runAiDenoise() },
                        onSaveCustomPreset = { name, cat -> viewModel.saveCurrentAsNewPreset(name, cat) }
                    )
                }
            }

            // ICON-FIRST HORIZONTAL TOOLBAR (Ergonomic bottom navigation)
            IconToolbar(
                activeTab = activeTab,
                onTabSelected = {
                    viewModel.setTab(it)
                    isToolDrawerOpen = true
                }
            )
        }
    }

    // LIGHTROOM COPY SETTINGS DIALOG (Selectively copy adjustments & masks)
    if (showCopySettingsDialog) {
        AlertDialog(
            onDismissRequest = { showCopySettingsDialog = false },
            containerColor = StudioSurfaceElevated,
            title = {
                Text(
                    text = "Copy Settings (Lightroom Style)",
                    color = AccentCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Light (Exposure, Contrast, Tone)", color = TextPrimary, fontSize = 12.sp)
                        Switch(checked = copyFilter.copyLight, onCheckedChange = { copyFilter = copyFilter.copy(copyLight = it) }, colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Color & White Balance", color = TextPrimary, fontSize = 12.sp)
                        Switch(checked = copyFilter.copyColor, onCheckedChange = { copyFilter = copyFilter.copy(copyColor = it) }, colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("8-Channel Color Mixer (HSL)", color = TextPrimary, fontSize = 12.sp)
                        Switch(checked = copyFilter.copyHsl, onCheckedChange = { copyFilter = copyFilter.copy(copyHsl = it) }, colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Effects (Texture, Clarity, Grain)", color = TextPrimary, fontSize = 12.sp)
                        Switch(checked = copyFilter.copyEffects, onCheckedChange = { copyFilter = copyFilter.copy(copyEffects = it) }, colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Detail (Sharpening, Denoise)", color = TextPrimary, fontSize = 12.sp)
                        Switch(checked = copyFilter.copyDetail, onCheckedChange = { copyFilter = copyFilter.copy(copyDetail = it) }, colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("DNG Masking (Subject, Sky, Gradients)", color = AccentAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Switch(checked = copyFilter.copyMasking, onCheckedChange = { copyFilter = copyFilter.copy(copyMasking = it) }, colors = SwitchDefaults.colors(checkedThumbColor = AccentAmber))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.copyCurrentSettings(copyFilter)
                        showCopySettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("Copy", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCopySettingsDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // HISTORY DIALOG (MOD-025)
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            containerColor = StudioSurfaceElevated,
            title = {
                Text(
                    text = "Edit History Timeline",
                    color = AccentCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(modifier = Modifier.height(240.dp)) {
                    items(historyTimeline.reversed()) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    viewModel.restoreHistoryItem(item)
                                    showHistoryDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.description, color = TextPrimary, fontSize = 11.sp)
                                    Text("Step ID: ${item.id.takeLast(6)}", color = TextTertiary, fontSize = 9.sp)
                                }
                                Text("Restore", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close", color = AccentCyan)
                }
            }
        )
    }

    // CLOUD & PRO SUBSCRIPTION DIALOG
    if (showCloudDialog) {
        AlertDialog(
            onDismissRequest = { showCloudDialog = false },
            containerColor = StudioSurfaceElevated,
            title = {
                Text("Cloud & Pro Entitlements", color = AccentCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Subscription: PRO CREATOR PLAN (Active)", color = AccentAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Sync Status: ${cloudInfo.status}", color = TextPrimary, fontSize = 11.sp)
                    Text("Storage: ${cloudInfo.cloudStorageUsedMb} MB / ${cloudInfo.cloudStorageTotalMb} MB (6% Used)", color = TextSecondary, fontSize = 11.sp)
                    Text("Features: Unlimited RAW, Hairline Color Science, AI Zebras, Neural Denoise.", color = TextTertiary, fontSize = 10.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showCloudDialog = false }) {
                    Text("Done", color = AccentCyan)
                }
            }
        )
    }

    // SETTINGS DIALOG
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = StudioSurfaceElevated,
            title = {
                Text("Editor Settings", color = AccentCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rule of Thirds Grid", color = TextPrimary, fontSize = 12.sp)
                        Switch(
                            checked = showGrid,
                            onCheckedChange = { viewModel.toggleRuleOfThirdsGrid() },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Histogram Overlay", color = TextPrimary, fontSize = 12.sp)
                        Switch(
                            checked = showHistogram,
                            onCheckedChange = { showHistogram = !showHistogram },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                        )
                    }
                    Text("Phone Display Target: 720×1520 px (Adaptive)", color = TextTertiary, fontSize = 10.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close", color = AccentCyan)
                }
            }
        )
    }

    // EXIF DIALOG
    if (showExifDialog && selectedPhoto != null) {
        val p = selectedPhoto!!
        AlertDialog(
            onDismissRequest = { showExifDialog = false },
            containerColor = StudioSurfaceElevated,
            title = {
                Text("EXIF Metadata", color = AccentCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("File: ${p.title}", color = TextPrimary, fontSize = 11.sp)
                    Text("Camera: ${p.cameraModel}", color = TextSecondary, fontSize = 11.sp)
                    Text("Dimensions: ${p.width} × ${p.height} px", color = TextSecondary, fontSize = 11.sp)
                    Text("Exposure: ${p.shutterSpeed} at ${p.aperture}", color = TextSecondary, fontSize = 11.sp)
                    Text("Focal Length: ${p.focalLength} • ISO ${p.iso}", color = TextSecondary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showExifDialog = false }) {
                    Text("Close", color = AccentCyan)
                }
            }
        )
    }

    // EXPORT DIALOG
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = StudioSurfaceElevated,
            title = {
                Text("Export Photo", color = AccentCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Format", color = TextSecondary, fontSize = 11.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        listOf("JPEG", "PNG", "WEBP").forEach { fmt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = exportFormat == fmt,
                                    onClick = { exportFormat = fmt },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                                )
                                Text(
                                    when (fmt) {
                                        "PNG" -> "PNG (Lossless)"
                                        "WEBP" -> "WebP"
                                        else -> "JPEG"
                                    },
                                    color = TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (exportFormat == "JPEG" || exportFormat == "WEBP") {
                        Text("Quality: ${exportQuality.toInt()}%", color = TextSecondary, fontSize = 11.sp)
                        Slider(
                            value = exportQuality,
                            onValueChange = { exportQuality = it },
                            valueRange = 50f..100f,
                            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                        )
                    }

                    Text("Resolution Preset", color = TextSecondary, fontSize = 11.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        listOf("Original", "2048px (Social)", "1080px (Web)").forEach { dim ->
                            FilterChip(
                                selected = exportDimension == dim,
                                onClick = { exportDimension = dim },
                                label = { Text(dim, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include Studio Watermark", color = TextPrimary, fontSize = 11.sp)
                        Switch(
                            checked = exportWatermark,
                            onCheckedChange = { exportWatermark = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                        )
                    }

                    if (exportWatermark) {
                        OutlinedTextField(
                            value = watermarkText,
                            onValueChange = { watermarkText = it },
                            label = { Text("Watermark Text", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            viewModel.exportPhoto(
                                ExportOptions(
                                    format = exportFormat,
                                    quality = exportQuality.toInt(),
                                    dimensionPreset = exportDimension,
                                    addWatermark = exportWatermark,
                                    watermarkText = watermarkText
                                )
                            ) { file ->
                                exportedFileForShare = file
                                viewModel.shareExportedFile(file)
                            }
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(13.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = Color.Black, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.exportPhoto(
                                ExportOptions(
                                    format = exportFormat,
                                    quality = exportQuality.toInt(),
                                    dimensionPreset = exportDimension,
                                    addWatermark = exportWatermark,
                                    watermarkText = watermarkText
                                )
                            ) {}
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text("Save File", color = Color.Black, fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun TopEditorBar(
    photo: PhotoItem?,
    canUndo: Boolean,
    canRedo: Boolean,
    showSplit: Boolean,
    showZebras: Boolean,
    onBack: () -> Unit,
    onApplyDngClick: () -> Unit,
    onToggleSplit: () -> Unit,
    onToggleZebras: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onHistoryClick: () -> Unit,
    onCloudClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExifClick: () -> Unit,
    onAiEnhance: () -> Unit,
    onExportClick: () -> Unit,
    onCopySettings: () -> Unit,
    onPasteSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(StudioSurface)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "←",
                color = AccentCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 6.dp)
            )
            Text(
                text = "LIGHTFIELD",
                fontWeight = FontWeight.Black,
                color = AccentCyan,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(6.dp))

            // Direct "Apply DNG" chip right in the editor header
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentAmber.copy(alpha = 0.18f))
                    .border(1.dp, AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onApplyDngClick() }
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Apply DNG",
                    color = AccentAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Interactive Swipe Split Comparison toggle
            IconButton(
                onClick = onToggleSplit,
                modifier = Modifier.size(32.dp).testTag("btn_toggle_split")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swipe Split View",
                    tint = if (showSplit) AccentCyan else TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Real-time Clipping Zebras Warning toggle
            IconButton(
                onClick = onToggleZebras,
                modifier = Modifier.size(32.dp).testTag("btn_toggle_zebras")
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Clipping Zebras",
                    tint = if (showZebras) AccentAmber else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // One-tap Auto AI
            IconButton(
                onClick = onAiEnhance,
                modifier = Modifier.size(32.dp).testTag("btn_ai_enhance")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Auto AI Enhance",
                    tint = AccentAmber,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Copy & Paste Settings
            IconButton(onClick = onCopySettings, modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(15.dp))
            }
            IconButton(onClick = onPasteSettings, modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Paste", tint = TextSecondary, modifier = Modifier.size(15.dp))
            }

            // Undo & Redo
            IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo", tint = if (canUndo) TextPrimary else TextTertiary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.Redo, contentDescription = "Redo", tint = if (canRedo) TextPrimary else TextTertiary, modifier = Modifier.size(16.dp))
            }

            // History & Settings
            IconButton(onClick = onHistoryClick, modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }

            // Export
            IconButton(onClick = onExportClick, modifier = Modifier.size(32.dp).testTag("btn_export")) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Export", tint = AccentCyan, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CompactFilmstripBar(
    photos: List<PhotoItem>,
    selectedPhoto: PhotoItem?,
    activeAlbumFilter: String,
    batchSelection: Set<String>,
    onFilterSelected: (String) -> Unit,
    onPhotoSelect: (PhotoItem) -> Unit,
    onToggleBatch: (String) -> Unit,
    onImportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(StudioSurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick Import Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(StudioSurfaceElevated)
                .clickable { onImportClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 16.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Thumbnails
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(photos) { photo ->
                val isSelected = photo.id == selectedPhoto?.id
                val isInBatch = batchSelection.contains(photo.id)

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .border(
                            width = if (isSelected) 2.dp else if (isInBatch) 1.5.dp else 0.5.dp,
                            color = if (isSelected) AccentCyan else if (isInBatch) AccentAmber else BorderSubtle,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .clickable { onPhotoSelect(photo) }
                ) {
                    if (photo.resourceId != null) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = photo.resourceId),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Batch select indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (isInBatch) AccentAmber else Color.Black.copy(alpha = 0.5f))
                            .clickable { onToggleBatch(photo.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isInBatch) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Batch Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }

                    if (photo.isRaw) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 2.dp)
                        ) {
                            Text("RAW", fontSize = 6.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
