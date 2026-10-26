# MASTER PRODUCT REQUIREMENTS DOCUMENT (PRD)
## Project Name: Lightfield — Professional AI Photo Editor
**Document Version:** 1.3.1 (Documentation, Hook Assets & Architecture Parity)  
**Target Platform:** Android (Optimized for Android 8.1+ / API 27–36, Phone Screen 720×1520 and adaptive up to tablets)  
**Architectural Style:** MVVM + Jetpack Compose + Clean Architecture + Non-destructive Processing Pipeline  

---

### 1. Product Overview & Architectural Hierarchy
Lightfield is an advanced, non-destructive photo editing and digital color grading application for Android mobile devices. The original digital negative is strictly immutable; all adjustments are applied through a non-destructive pipeline yielding pixel-identical parity between 60fps real-time viewport previews and full-resolution exports.

#### Reference Implementation Precedence
The application's rendering pipeline, editing interactions, and color science are aligned with three premier open-source RAW & photo editing projects:
1. **LightCraft (`storytold/lightcraft`) [PRIMARY / AUTHORITATIVE]**:
   - Scene-referred 32-bit linear exposure scaling ($2^{\Delta EV}$).
   - Perceptual vibrance with chrominance skin-tone protection ($Y C_b C_r$).
   - Strict monotone cubic spline interpolation (Fritsch-Carlson) for all tone curves, eliminating slope artifacts.
   - Non-destructive processing stage order (Geometry -> Linear Tone -> Curves -> Color -> Details -> Masks).
2. **CalibRaw (`Duecki1/CalibRaw`) [SECONDARY]**:
   - Responsive touch gesture ergonomics for mobile viewports (720×1520 px).
   - On-device local AI segmentation masks (Subject/Person, Sky, Gradients) with fast separable box-blur feathering.
   - Non-destructive sidecar metadata synchronization and granular Copy/Paste settings.
3. **RapidRAW (`CyberTimon/RapidRAW`) [SECONDARY]**:
   - Two-tier rendering pipeline: decoupled 1280px interactive preview buffer vs. full-res worker export.
   - Multi-format high-fidelity export (JPEG with quality scaling, Lossless PNG, and WebP).
   - Render job cancellation on slider touch to prevent queue congestion.

---

### 2. Comprehensive 30-Module Matrix & Verification Status

| Module ID | Module Name | Implementation Details & Controls | Status |
| :--- | :--- | :--- | :---: |
| **MOD-001** | **Authentication & Account** | Pro Creator Plan entitlement tracking, cloud storage quota (1.2GB/20GB), guest offline persistence. | ✅ Complete |
| **MOD-002** | **Photo Import Engine** | System Photo Picker (`ActivityResultContracts.GetContent()`), EXIF reader, and automated Adobe DNG XMP (CRS - Camera Raw Settings) detection. | ✅ Complete |
| **MOD-003** | **Photo Library & Organization** | Album categories filter row (All, Portrait, Landscape, Street, Imports, DNG Presets), batch select indicators, RAW tags. | ✅ Complete |
| **MOD-004** | **Core Photo Editor Canvas** | 60fps real-time viewport, aspect-ratio preserving Before/After swipe divider bar, Rule of Thirds grid overlay. | ✅ Complete |
| **MOD-005** | **Light Engine** | Linear-space Exposure (-5.0 to +5.0 EV: $2^{\Delta EV}$), Contrast, Highlights, Shadows, Whites, Blacks with individual reset controls. | ✅ Complete |
| **MOD-006** | **Color Engine** | WB Temperature, Tint, Skin-Protected Vibrance (LightCraft YCbCr guard), Saturation, 8-Channel HSL Mixer (R,O,Y,G,A,B,P,M), 3-Way Color Grading. | ✅ Complete |
| **MOD-007** | **Tone Curve Engine** | RGB Composite, Red, Green, and Blue independent channels with Fritsch-Carlson Monotone Spline 256-LUT interpolation. | ✅ Complete |
| **MOD-008** | **Effects Engine** | Texture, Clarity (midtone contrast expansion), Dehaze (atmospheric scattering compensation), Vignette Amount/Midpoint, Film Grain Amount/Size/Roughness. | ✅ Complete |
| **MOD-009** | **Detail Engine** | High-pass Unsharp Mask Sharpening (Radius 0.5–3.0), Local Mean Luminance & Color Noise Reduction, Neural AI Denoise progress action. | ✅ Complete |
| **MOD-010** | **Optics & Lens Correction** | Lens Distortion (barrel/pincushion), Chromatic Aberration defringe toggle, Linear Lens Vignette correction. | ✅ Complete |
| **MOD-011** | **Crop, Rotate & Geometry** | 8 Aspect ratios with centered cropping, Rotate 90°, Flip Horizontal, Flip Vertical, Straighten (-45°..+45°), Perspective skew. | ✅ Complete |
| **MOD-012** | **Masking Engine** | Adobe CRS DNG Mask Parser & On-device Computer Vision (AI Subject & Person YCbCr skin/saliency detection, AI Sky detection, Gradients, Brush). Real per-pixel local exposure, contrast, warmth, saturation, and clarity composite with fast separable feathering. | ✅ Complete |
| **MOD-013** | **Healing & Inpainting** | Spot Heal, Clone Stamp, AI Generative Remove modes with adjustable brush size and inpainting execution. | ✅ Complete |
| **MOD-014** | **AI Smart Editing** | One-tap AI Auto-Enhance assessing tone distribution and clarity with visual feedback. | ✅ Complete |
| **MOD-015** | **Portrait Retouching** | Skin Smoothing, Face Lighting, Lip Tint, Eye Brighten, Teeth Whiten. | ✅ Complete |
| **MOD-016** | **Preset Engine** | Curated catalog, Preset Amount (0–200%), Create Custom Preset, and Direct Save DNG to Presets. Preloaded Editorial DNG preset with AI Subject and Sky masks. | ✅ Complete |
| **MOD-017** | **Lens Blur & Bokeh** | Depth Blur Amount, Bokeh Shape (Circular, Hexagonal, Heart), Intensity. | ✅ Complete |
| **MOD-018** | **HDR Engine** | Local tone mapping dynamic range compensation toggle. | ✅ Complete |
| **MOD-019** | **Merge & Multi-Shot** | Stacking & exposure fusion affordance. | ✅ Complete |
| **MOD-020** | **RAW & Color Space** | DNG XMP metadata extraction, EXIF inspection dialog, ProPhoto RGB / Display P3 matrix indicator. | ✅ Complete |
| **MOD-021** | **Export Engine** | JPEG (50–100%), Lossless PNG, and WebP, Resolution presets (Original, 2048px Social, 1080px Web), Custom Watermark overlay with drop shadow. | ✅ Complete |
| **MOD-022** | **Cloud Storage Engine** | Cloud quota monitoring (1.2 GB / 20 GB used) with Pro Plan entitlements. | ✅ Complete |
| **MOD-023** | **Sync Engine** | Real-time cloud sync status indicator pill ("All edits synced", 0 pending). | ✅ Complete |
| **MOD-024** | **Batch Editing Engine** | Lightroom-style Copy Settings (granular checkboxes for Light, Color, HSL, Effects, Detail, Masking) & Paste Settings across multiple items. | ✅ Complete |
| **MOD-025** | **History & Versioning** | Unlimited Undo/Redo with debounced snapshots, Reset All, and browseable History Timeline list dialog allowing jumping to any past step. | ✅ Complete |
| **MOD-026** | **Sharing Engine** | Secure Android `FileProvider` system share intent (`Intent.ACTION_SEND` with `FLAG_GRANT_READ_URI_PERMISSION`) compliant with Android 8.1+ (API 27+). | ✅ Complete |
| **MOD-027** | **Subscription Engine** | Pro Creator Plan entitlements matrix modal. | ✅ Complete |
| **MOD-028** | **Settings & Preferences** | Settings dialog: Rule of Thirds grid toggle, High Quality Preview toggle, 720×1520 display target check. | ✅ Complete |
| **MOD-029** | **Plugin & Watermark Engine** | Customizable watermark text overlay during export with resolution-aware typography and drop-shadow. | ✅ Complete |
| **MOD-030** | **Diagnostics & Live Histogram** | Live 256-bin RGB Luminance histogram graph with quarter-zone grid markers. | ✅ Complete |

---

### 3. Pipeline Order & Image Science (LightCraft Alignment)
The pixel pipeline executes sequentially to prevent clipping and quantization errors:
```
Original Sensor Bitmap 
  │
  ├─ 1. Aspect Ratio Cropping & Geometric Transformations (Orientation / Flip / Ratio)
  │
  ├─ 2. Optics: Linear Lens Vignette Correction
  │
  ├─ 3. Linear Tone Stage (Exposure $2^{\Delta EV}$, Contrast, Highlights, Shadows, Whites, Blacks)
  │
  ├─ 4. Fritsch-Carlson Monotone Spline Tone Curves (Composite RGB, Red, Green, Blue LUTs)
  │
  ├─ 5. White Balance & Temperature/Tint Correction
  │
  ├─ 6. 8-Channel HSL Mixer & 3-Way Split Tone Color Grading
  │
  ├─ 7. Vibrance with Skin-Tone YCbCr Protection & Saturation
  │
  ├─ 8. Effects: Clarity (Midtone Contrast), Texture, Dehaze, Post-Crop Vignette, Film Grain
  │
  ├─ 9. Details: High-Pass Unsharp Masking & Local-Mean Noise Reduction
  │
  ├─ 10. Portrait Retouching (Skin Smoothing, Face Lighting, Lip Tint, Eye/Teeth Brightening)
  │
  ├─ 11. Per-Pixel Local Mask Compositing (AI Subject/Person, Sky, Gradients, Brush)
  │
  └─ Viewport Display (1280px Preview) / Full-Res Export (JPEG/PNG/WebP with Watermark)
```

---

### 4. Critical Engineering Fixes (v1.3.0)
1. **Fritsch-Carlson Monotone Spline Interpolation**:
   - Replaced flawed cubic Hermite interpolation that assumed 0 derivative at internal knots (which produced staircases, flat spots, and posterization).
   - Slopes are now computed using the harmonic mean of secant lines with monotonicity constraints, guaranteeing smooth, gradient-continuous curve response identical to LightCraft and Adobe Lightroom.
2. **Aspect Ratio Cropping**:
   - Connected `edit.aspectRatio.ratio` directly to `ImageProcessingEngine.applyEdits`, performing centered aspect ratio crops in both preview and export.
3. **Skin-Tone Protection in Vibrance**:
   - Aligned with LightCraft color science: Vibrance boost evaluates pixel chrominance in YCbCr space. Pixels inside the human skin locus ($C_r \in [130, 175], C_b \in [75, 130]$) receive muted saturation scaling, preventing red/orange face blowouts.
4. **Detail Engine High-Pass Unsharp Sharpening & Noise Reduction**:
   - Implemented real spatial convolution: Sharpening applies a high-pass unsharp mask proportional to sharpening amount; Noise reduction applies local neighborhood spatial smoothing.
5. **Full Export Parity (WebP & Studio Watermark)**:
   - Added native Android 8.1+ `Bitmap.CompressFormat.WEBP` export.
   - Connected the watermark overlay engine: renders resolution-scaled anti-aliased text with subtle drop-shadow onto the bottom-right corner of exported photos.
6. **Cancellation of In-Flight Renders**:
   - Integrated Coroutine job cancellation (`renderJob?.cancel()`) on slider drag to eliminate backlog and guarantee 60fps UI responsiveness on 720×1520 displays.

---

### 5. Device Compatibility & Target Constraints
- **Android Target**: Android 8.1+ (API 27–36).
- **Physical Display Profile**: 720×1520 px (19:9 aspect ratio).
- **Edge-to-Edge Window Insets**: Scaffold pads for status bars and navigation bars without overlap.
- **Accessibility Minimums**: Touch targets $\ge 48 \text{ dp}$ for all sliders, thumb handles, and segmented control chips.
