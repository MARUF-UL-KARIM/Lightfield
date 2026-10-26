package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SamplePhotoRepository
import com.example.engine.DngXmpParser
import com.example.engine.ImageProcessingEngine
import com.example.model.AspectRatioType
import com.example.model.CurveChannel
import com.example.model.EditState
import com.example.model.HistoryItem
import com.example.model.MaskType
import com.example.model.PhotoItem
import com.example.model.PresetItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class EditorTab(val label: String) {
    LIGHT("Light"),
    COLOR("Color"),
    CURVES("Curves"),
    EFFECTS("Effects"),
    DETAIL("Detail"),
    OPTICS("Optics"),
    CROP("Crop"),
    MASKING("Masking"),
    HEALING("Healing"),
    PORTRAIT("Portrait"),
    PRESETS("Presets")
}

data class ExportOptions(
    val format: String = "JPEG", // JPEG or PNG
    val quality: Int = 95,
    val dimensionPreset: String = "Original", // Original, 2048px (Social), 1080px (Web)
    val includeExif: Boolean = true,
    val addWatermark: Boolean = false,
    val watermarkText: String = "Shot on Lightfield",
    val colorSpace: String = "sRGB"
)

data class CloudSyncInfo(
    val status: String = "All edits synced",
    val pendingChanges: Int = 0,
    val cloudStorageUsedMb: Int = 1240,
    val cloudStorageTotalMb: Int = 20480,
    val isProSubscribed: Boolean = true
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _photos = MutableStateFlow<List<PhotoItem>>(SamplePhotoRepository.getInitialPhotos())
    val photos: StateFlow<List<PhotoItem>> = _photos.asStateFlow()

    private val _activeAlbumFilter = MutableStateFlow("All Photos")
    val activeAlbumFilter: StateFlow<String> = _activeAlbumFilter.asStateFlow()

    private val _selectedPhoto = MutableStateFlow<PhotoItem?>(null)
    val selectedPhoto: StateFlow<PhotoItem?> = _selectedPhoto.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorTab.LIGHT)
    val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

    private val _currentEditState = MutableStateFlow(EditState())
    val currentEditState: StateFlow<EditState> = _currentEditState.asStateFlow()

    // Undo / Redo history stacks & named history entries (MOD-025)
    private val undoStack = mutableListOf<EditState>()
    private val redoStack = mutableListOf<EditState>()
    private val _historyTimeline = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyTimeline: StateFlow<List<HistoryItem>> = _historyTimeline.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Rendered preview bitmap & histogram
    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap: StateFlow<Bitmap?> = _originalBitmap.asStateFlow()

    private val _clippingZebraBitmap = MutableStateFlow<Bitmap?>(null)
    val clippingZebraBitmap: StateFlow<Bitmap?> = _clippingZebraBitmap.asStateFlow()

    // Adobe Lightroom Ruby-Red Mask Selection Overlay
    private val _maskPreviewOverlay = MutableStateFlow<Bitmap?>(null)
    val maskPreviewOverlay: StateFlow<Bitmap?> = _maskPreviewOverlay.asStateFlow()

    private val _histogramData = MutableStateFlow(FloatArray(256))
    val histogramData: StateFlow<FloatArray> = _histogramData.asStateFlow()

    private val _isComparingBeforeAfter = MutableStateFlow(false)
    val isComparingBeforeAfter: StateFlow<Boolean> = _isComparingBeforeAfter.asStateFlow()

    // Interactive Swipe Split Comparison & Pro Zebras
    private val _showSplitComparison = MutableStateFlow(false)
    val showSplitComparison: StateFlow<Boolean> = _showSplitComparison.asStateFlow()

    private val _splitFraction = MutableStateFlow(0.5f)
    val splitFraction: StateFlow<Float> = _splitFraction.asStateFlow()

    private val _showZebraClipping = MutableStateFlow(false)
    val showZebraClipping: StateFlow<Boolean> = _showZebraClipping.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingProgress = MutableStateFlow(0f)
    val processingProgress: StateFlow<Float> = _processingProgress.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    // Multi-selection for Batch Editing
    private val _selectedPhotoIdsForBatch = MutableStateFlow<Set<String>>(emptySet())
    val selectedPhotoIdsForBatch: StateFlow<Set<String>> = _selectedPhotoIdsForBatch.asStateFlow()

    // Clipboard for copy/paste settings
    private var clipboardEditState: EditState? = null

    // Cloud and Subscription state (MOD-022, MOD-027)
    private val _cloudInfo = MutableStateFlow(CloudSyncInfo())
    val cloudInfo: StateFlow<CloudSyncInfo> = _cloudInfo.asStateFlow()

    // Settings (MOD-028)
    private val _showRuleOfThirdsGrid = MutableStateFlow(false)
    val showRuleOfThirdsGrid: StateFlow<Boolean> = _showRuleOfThirdsGrid.asStateFlow()

    private val _highQualityPreview = MutableStateFlow(true)
    val highQualityPreview: StateFlow<Boolean> = _highQualityPreview.asStateFlow()

    // Base loaded source bitmap and fast preview bitmap
    private var baseSourceBitmap: Bitmap? = null
    private var previewSourceBitmap: Bitmap? = null
    private var renderJob: kotlinx.coroutines.Job? = null
    private var lastUndoRecordTime = 0L

    // Presets catalog (MOD-016)
    private val _customPresets = MutableStateFlow<List<PresetItem>>(emptyList())
    val customPresets: StateFlow<List<PresetItem>> = _customPresets.asStateFlow()

    init {
        if (_photos.value.isNotEmpty()) {
            selectPhoto(_photos.value.first())
        }
    }

    fun selectPhoto(photo: PhotoItem) {
        _selectedPhoto.value = photo
        _currentEditState.value = photo.editState
        undoStack.clear()
        redoStack.clear()
        _historyTimeline.value = listOf(
            HistoryItem(
                id = "hist_init",
                description = "Imported / Negative Loaded",
                state = photo.editState
            )
        )
        updateUndoRedoStates()
        loadBaseBitmap(photo)
    }

    private fun loadBaseBitmap(photo: PhotoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            try {
                val context = getApplication<Application>()
                val bmp = if (photo.resourceId != null) {
                    BitmapFactory.decodeResource(context.resources, photo.resourceId)
                } else if (photo.uri != null) {
                    context.contentResolver.openInputStream(photo.uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } else null

                baseSourceBitmap = bmp
                if (bmp != null) {
                    val maxDim = maxOf(bmp.width, bmp.height)
                    previewSourceBitmap = if (maxDim > 1280) {
                        val scale = 1280f / maxDim
                        Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true)
                    } else {
                        bmp
                    }
                } else {
                    previewSourceBitmap = null
                }
                _originalBitmap.value = previewSourceBitmap
                renderPreview()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun setSplitFraction(fraction: Float) {
        _splitFraction.value = fraction
    }

    fun toggleSplitComparison() {
        _showSplitComparison.value = !_showSplitComparison.value
    }

    fun toggleZebraClipping() {
        _showZebraClipping.value = !_showZebraClipping.value
        if (_showZebraClipping.value) {
            renderPreview()
        }
    }

    fun setTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    fun setAlbumFilter(album: String) {
        _activeAlbumFilter.value = album
    }

    fun toggleRuleOfThirdsGrid() {
        _showRuleOfThirdsGrid.value = !_showRuleOfThirdsGrid.value
    }

    fun toggleHighQualityPreview() {
        _highQualityPreview.value = !_highQualityPreview.value
        renderPreview()
    }

    fun updateEdit(description: String = "Adjustment updated", transform: (EditState) -> EditState) {
        val current = _currentEditState.value
        val now = System.currentTimeMillis()
        if (now - lastUndoRecordTime > 700L) {
            undoStack.add(current)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
            updateUndoRedoStates()
            lastUndoRecordTime = now
            _historyTimeline.value = _historyTimeline.value + HistoryItem(
                id = "hist_${now}",
                description = description,
                state = current
            )
        }

        val next = transform(current)
        _currentEditState.value = next
        renderPreview()
    }

    fun selectDngMask(index: Int) {
        val current = _currentEditState.value
        if (index in current.dngMasks.indices) {
            val mask = current.dngMasks[index]
            updateEdit("Selected DNG Mask: ${mask.name}") {
                it.copy(
                    activeMaskType = mask.maskType,
                    maskExposure = mask.localExposure,
                    maskWarmth = mask.localWarmth,
                    maskSaturation = mask.localSaturation,
                    maskContrast = mask.localContrast,
                    maskClarity = mask.localHighlights
                )
            }
        }
    }

    fun restoreHistoryItem(item: HistoryItem) {
        undoStack.add(_currentEditState.value)
        _currentEditState.value = item.state
        updateUndoRedoStates()
        renderPreview()
        _notificationMessage.value = "Restored to: ${item.description}"
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val last = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_currentEditState.value)
            _currentEditState.value = last
            updateUndoRedoStates()
            renderPreview()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_currentEditState.value)
            _currentEditState.value = next
            updateUndoRedoStates()
            renderPreview()
        }
    }

    fun resetAllEdits() {
        val photo = _selectedPhoto.value ?: return
        undoStack.add(_currentEditState.value)
        redoStack.clear()
        updateUndoRedoStates()
        _currentEditState.value = EditState(photoId = photo.id)
        _historyTimeline.value = _historyTimeline.value + HistoryItem(
            id = "hist_reset_${System.currentTimeMillis()}",
            description = "Reset All Adjustments",
            state = _currentEditState.value
        )
        renderPreview()
        _notificationMessage.value = "All adjustments reset to initial state"
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun setBeforeAfter(comparing: Boolean) {
        _isComparingBeforeAfter.value = comparing
        renderPreview()
    }

    private fun renderPreview() {
        val base = previewSourceBitmap ?: baseSourceBitmap ?: return
        renderJob?.cancel()
        renderJob = viewModelScope.launch(Dispatchers.Default) {
            val rendered = if (_isComparingBeforeAfter.value) {
                base
            } else {
                ImageProcessingEngine.applyEdits(base, _currentEditState.value)
            }
            val histo = ImageProcessingEngine.computeHistogram(rendered)
            val zebras = if (_showZebraClipping.value) {
                ImageProcessingEngine.computeClippingOverlay(rendered)
            } else null

            // Generate Adobe Lightroom Ruby-Red Overlay when mask is selected and user hasn't adjusted exposure yet
            val activeMask = _currentEditState.value.activeMaskType
            val maskOverlay = if (activeMask != com.example.model.MaskType.NONE &&
                _currentEditState.value.maskExposure == 0f &&
                _currentEditState.value.maskContrast == 0f &&
                _currentEditState.value.maskWarmth == 0f &&
                _currentEditState.value.maskSaturation == 0f &&
                _currentEditState.value.maskClarity == 0f) {
                val maskMap = com.example.engine.SubjectSegmentationEngine.generateMaskMap(
                    base,
                    activeMask,
                    _currentEditState.value.isMaskInverted,
                    _currentEditState.value.maskFeather
                )
                com.example.engine.SubjectSegmentationEngine.createRubyRedOverlay(maskMap, base.width, base.height)
            } else {
                null
            }

            _previewBitmap.value = rendered
            _maskPreviewOverlay.value = maskOverlay
            _clippingZebraBitmap.value = zebras
            _histogramData.value = histo
        }
    }

    // Preset application with amount scaling (MOD-016)
    fun applyPreset(preset: PresetItem, amountPct: Float = 100f) {
        updateEdit("Applied Preset: ${preset.name} (${amountPct.toInt()}%)") { current ->
            preset.editState.copy(
                photoId = current.photoId,
                rotationDegrees = current.rotationDegrees,
                appliedPresetName = preset.name,
                presetAmount = amountPct
            )
        }
        _notificationMessage.value = "Preset applied: ${preset.name}"
    }

    fun saveCurrentAsNewPreset(name: String, category: String) {
        val newPreset = PresetItem(
            id = "custom_preset_${System.currentTimeMillis()}",
            name = name,
            category = category,
            description = "Custom User Created Style",
            editState = _currentEditState.value
        )
        _customPresets.value = _customPresets.value + newPreset
        _notificationMessage.value = "Saved preset '$name' under $category"
    }

    // AI Denoise execution simulation (MOD-009, MOD-014)
    fun runAiDenoise() {
        _isProcessing.value = true
        _processingProgress.value = 0f
        viewModelScope.launch(Dispatchers.Default) {
            for (i in 1..10) {
                kotlinx.coroutines.delay(100)
                _processingProgress.value = i * 0.1f
            }
            withContext(Dispatchers.Main) {
                updateEdit("AI Denoise neural noise extraction") {
                    it.copy(
                        isAiDenoiseApplied = true,
                        noiseReduction = 60f,
                        colorNoiseReduction = 50f
                    )
                }
                _isProcessing.value = false
                _notificationMessage.value = "AI Neural Denoise Complete"
            }
        }
    }

    // AI Auto Smart Enhance (MOD-014)
    fun runAiSmartEnhance() {
        _isProcessing.value = true
        viewModelScope.launch(Dispatchers.Default) {
            kotlinx.coroutines.delay(350)
            withContext(Dispatchers.Main) {
                updateEdit("AI Auto-Adjustment") {
                    it.copy(
                        exposure = 0.25f,
                        contrast = 15f,
                        highlights = -25f,
                        shadows = 25f,
                        temperature = 5f,
                        vibrance = 20f,
                        clarity = 12f,
                        dehaze = 10f
                    )
                }
                _isProcessing.value = false
                _notificationMessage.value = "AI Auto-Adjustment Applied: Balanced Tone & Clarity"
            }
        }
    }

    // Directly Apply Adobe Lightroom DNG Preset onto Current Active Photo
    fun applyDngPresetDirectly(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            try {
                val context = getApplication<Application>()
                var parsedState: EditState? = null
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    parsedState = DngXmpParser.parseDngStream(stream)
                }

                withContext(Dispatchers.Main) {
                    if (parsedState != null) {
                        val current = _currentEditState.value
                        val merged = parsedState!!.copy(
                            photoId = current.photoId,
                            rotationDegrees = current.rotationDegrees,
                            flipHorizontal = current.flipHorizontal,
                            flipVertical = current.flipVertical,
                            aspectRatio = current.aspectRatio,
                            appliedPresetName = "Adobe DNG Preset"
                        )
                        updateEdit("Applied Adobe Lightroom DNG Preset") { merged }
                        val maskCount = merged.dngMasks.size
                        _notificationMessage.value = "Lightroom DNG Applied! Color, Tone & $maskCount Masks Active."
                    } else {
                        _notificationMessage.value = "Could not parse Adobe CRS XMP from this DNG file."
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _notificationMessage.value = "Error reading DNG file: ${e.localizedMessage}"
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // Photo import with DNG XMP CRS Auto-Detection (MOD-002, MOD-020)
    fun importPhotoFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            var parsedDngState: EditState? = null
            var isDngFile = false

            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    parsedDngState = DngXmpParser.parseDngStream(stream)
                    if (parsedDngState != null) isDngFile = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val finalState = parsedDngState ?: EditState()
            val newPhoto = PhotoItem(
                id = "photo_user_${System.currentTimeMillis()}",
                uri = uri,
                title = if (isDngFile) "DNG_Preset_${System.currentTimeMillis() % 10000}.dng" else "Import_${System.currentTimeMillis() % 10000}.jpg",
                cameraModel = if (isDngFile) "Adobe DNG Preset (CRS XMP)" else "Imported Mobile Media",
                album = if (isDngFile) "DNG Presets" else "Imports",
                isRaw = isDngFile,
                editState = finalState
            )

            withContext(Dispatchers.Main) {
                _photos.value = listOf(newPhoto) + _photos.value
                selectPhoto(newPhoto)
                if (isDngFile) {
                    val maskCount = finalState.dngMasks.size
                    _notificationMessage.value = "Adobe DNG Preset Detected! XMP Settings & $maskCount Masks Imported."
                } else {
                    _notificationMessage.value = "Imported new photo to library"
                }
            }
        }
    }

    // Granular Copy Settings (Lightroom Style)
    fun copyCurrentSettings(filter: com.example.model.CopySettingsFilter = com.example.model.CopySettingsFilter()) {
        clipboardEditState = _currentEditState.value
        _notificationMessage.value = "Copied Lightroom adjustments & masks to clipboard"
    }

    fun pasteSettingsToSelected(filter: com.example.model.CopySettingsFilter = com.example.model.CopySettingsFilter()) {
        val clip = clipboardEditState ?: run {
            _notificationMessage.value = "No settings in clipboard. Open a DNG photo and tap Copy Settings."
            return
        }

        fun mergeFilteredState(target: EditState): EditState {
            return target.copy(
                // Light
                exposure = if (filter.copyLight) clip.exposure else target.exposure,
                contrast = if (filter.copyLight) clip.contrast else target.contrast,
                highlights = if (filter.copyLight) clip.highlights else target.highlights,
                shadows = if (filter.copyLight) clip.shadows else target.shadows,
                whites = if (filter.copyLight) clip.whites else target.whites,
                blacks = if (filter.copyLight) clip.blacks else target.blacks,
                // Color & HSL
                temperature = if (filter.copyColor) clip.temperature else target.temperature,
                tint = if (filter.copyColor) clip.tint else target.tint,
                vibrance = if (filter.copyColor) clip.vibrance else target.vibrance,
                saturation = if (filter.copyColor) clip.saturation else target.saturation,
                hslRedHue = if (filter.copyHsl) clip.hslRedHue else target.hslRedHue,
                hslRedSat = if (filter.copyHsl) clip.hslRedSat else target.hslRedSat,
                hslRedLum = if (filter.copyHsl) clip.hslRedLum else target.hslRedLum,
                hslOrangeHue = if (filter.copyHsl) clip.hslOrangeHue else target.hslOrangeHue,
                hslOrangeSat = if (filter.copyHsl) clip.hslOrangeSat else target.hslOrangeSat,
                hslOrangeLum = if (filter.copyHsl) clip.hslOrangeLum else target.hslOrangeLum,
                hslYellowHue = if (filter.copyHsl) clip.hslYellowHue else target.hslYellowHue,
                hslYellowSat = if (filter.copyHsl) clip.hslYellowSat else target.hslYellowSat,
                hslYellowLum = if (filter.copyHsl) clip.hslYellowLum else target.hslYellowLum,
                hslGreenHue = if (filter.copyHsl) clip.hslGreenHue else target.hslGreenHue,
                hslGreenSat = if (filter.copyHsl) clip.hslGreenSat else target.hslGreenSat,
                hslGreenLum = if (filter.copyHsl) clip.hslGreenLum else target.hslGreenLum,
                hslAquaHue = if (filter.copyHsl) clip.hslAquaHue else target.hslAquaHue,
                hslAquaSat = if (filter.copyHsl) clip.hslAquaSat else target.hslAquaSat,
                hslAquaLum = if (filter.copyHsl) clip.hslAquaLum else target.hslAquaLum,
                hslBlueHue = if (filter.copyHsl) clip.hslBlueHue else target.hslBlueHue,
                hslBlueSat = if (filter.copyHsl) clip.hslBlueSat else target.hslBlueSat,
                hslBlueLum = if (filter.copyHsl) clip.hslBlueLum else target.hslBlueLum,
                hslPurpleHue = if (filter.copyHsl) clip.hslPurpleHue else target.hslPurpleHue,
                hslPurpleSat = if (filter.copyHsl) clip.hslPurpleSat else target.hslPurpleSat,
                hslPurpleLum = if (filter.copyHsl) clip.hslPurpleLum else target.hslPurpleLum,
                hslMagentaHue = if (filter.copyHsl) clip.hslMagentaHue else target.hslMagentaHue,
                hslMagentaSat = if (filter.copyHsl) clip.hslMagentaSat else target.hslMagentaSat,
                hslMagentaLum = if (filter.copyHsl) clip.hslMagentaLum else target.hslMagentaLum,
                // Split Toning
                gradeShadowHue = if (filter.copyColor) clip.gradeShadowHue else target.gradeShadowHue,
                gradeShadowSat = if (filter.copyColor) clip.gradeShadowSat else target.gradeShadowSat,
                gradeHighlightHue = if (filter.copyColor) clip.gradeHighlightHue else target.gradeHighlightHue,
                gradeHighlightSat = if (filter.copyColor) clip.gradeHighlightSat else target.gradeHighlightSat,
                // Effects
                texture = if (filter.copyEffects) clip.texture else target.texture,
                clarity = if (filter.copyEffects) clip.clarity else target.clarity,
                dehaze = if (filter.copyEffects) clip.dehaze else target.dehaze,
                vignette = if (filter.copyEffects) clip.vignette else target.vignette,
                grainAmount = if (filter.copyEffects) clip.grainAmount else target.grainAmount,
                // Detail
                sharpening = if (filter.copyDetail) clip.sharpening else target.sharpening,
                noiseReduction = if (filter.copyDetail) clip.noiseReduction else target.noiseReduction,
                colorNoiseReduction = if (filter.copyDetail) clip.colorNoiseReduction else target.colorNoiseReduction,
                // Masking
                dngMasks = if (filter.copyMasking) clip.dngMasks else target.dngMasks,
                activeMaskType = if (filter.copyMasking) clip.activeMaskType else target.activeMaskType
            )
        }

        val targets = _selectedPhotoIdsForBatch.value
        if (targets.isEmpty()) {
            val photo = _selectedPhoto.value ?: return
            updateEdit("Pasted DNG Lightroom recipe") { mergeFilteredState(it) }
            _notificationMessage.value = "Pasted Lightroom adjustments & masks to current photo"
        } else {
            val updated = _photos.value.map { p ->
                if (targets.contains(p.id)) {
                    p.copy(editState = mergeFilteredState(p.editState))
                } else p
            }
            _photos.value = updated
            _notificationMessage.value = "Pasted settings & masks across ${targets.size} photos"
            _selectedPhotoIdsForBatch.value = emptySet()
        }
    }

    fun toggleBatchSelection(photoId: String) {
        val set = _selectedPhotoIdsForBatch.value.toMutableSet()
        if (set.contains(photoId)) set.remove(photoId) else set.add(photoId)
        _selectedPhotoIdsForBatch.value = set
    }

    // Export & Sharing (MOD-021, MOD-026)
    fun exportPhoto(options: ExportOptions, onComplete: (File) -> Unit) {
        _isProcessing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val base = baseSourceBitmap ?: return@launch
            var fullRes = ImageProcessingEngine.applyEdits(base, _currentEditState.value)

            // Dimension resize options
            if (options.dimensionPreset == "2048px (Social)") {
                val scale = 2048f / kotlin.math.max(fullRes.width, fullRes.height)
                if (scale < 1.0f) {
                    fullRes = Bitmap.createScaledBitmap(fullRes, (fullRes.width * scale).toInt(), (fullRes.height * scale).toInt(), true)
                }
            } else if (options.dimensionPreset == "1080px (Web)") {
                val scale = 1080f / kotlin.math.max(fullRes.width, fullRes.height)
                if (scale < 1.0f) {
                    fullRes = Bitmap.createScaledBitmap(fullRes, (fullRes.width * scale).toInt(), (fullRes.height * scale).toInt(), true)
                }
            }

            // Watermark Rendering (MOD-021, MOD-029)
            if (options.addWatermark && options.watermarkText.isNotBlank()) {
                val mutableBmp = fullRes.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = android.graphics.Canvas(mutableBmp)
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textSize = (mutableBmp.width * 0.024f).coerceIn(22f, 68f)
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    setShadowLayer(4f, 2f, 2f, android.graphics.Color.argb(190, 0, 0, 0))
                }
                val text = options.watermarkText
                val textBounds = android.graphics.Rect()
                paint.getTextBounds(text, 0, text.length, textBounds)
                val padding = (mutableBmp.width * 0.035f).coerceIn(20f, 60f)
                val x = mutableBmp.width - textBounds.width() - padding
                val y = mutableBmp.height - padding
                canvas.drawText(text, x, y, paint)
                fullRes = mutableBmp
            }

            val context = getApplication<Application>()
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val ext = when (options.format.uppercase()) {
                "PNG" -> "png"
                "WEBP" -> "webp"
                else -> "jpg"
            }
            val file = File(exportDir, "Lightfield_${System.currentTimeMillis()}.$ext")
            FileOutputStream(file).use { out ->
                when (options.format.uppercase()) {
                    "PNG" -> fullRes.compress(Bitmap.CompressFormat.PNG, 100, out)
                    "WEBP" -> fullRes.compress(Bitmap.CompressFormat.WEBP, options.quality, out)
                    else -> fullRes.compress(Bitmap.CompressFormat.JPEG, options.quality, out)
                }
            }

            withContext(Dispatchers.Main) {
                _isProcessing.value = false
                _notificationMessage.value = "Exported ${options.format} (${file.length() / 1024} KB) successfully"
                onComplete(file)
            }
        }
    }

    fun shareExportedFile(file: File) {
        val context = getApplication<Application>()
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (file.name.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Edited with Lightfield")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Edited Photo").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            _notificationMessage.value = "Sharing error: ${e.localizedMessage}"
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }
}
