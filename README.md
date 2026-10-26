# 📸 Lightfield — Pro RAW & AI Photo Studio for Android

> **Transform Mobile Photos into Lightroom-Grade Masterpieces — Non-Destructive, Instant, and Right in Your Pocket.**

[![Android](https://img.shields.io/badge/Android-8.1%2B%20(API%2027%2B)-brightgreen.svg?style=for-the-badge&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean%20Pipeline-orange.svg?style=for-the-badge)]()
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)]()

---

## ⚡ Why Lightfield? Stop Settling for Washed-Out Mobile Edits.

Most mobile editors crush your shadows, blow out highlights, and turn human skin bright orange the second you touch the vibrance slider. 

**Lightfield changes the game.** Built on the color science of top open-source RAW processors (**LightCraft**, **CalibRaw**, and **RapidRAW**), Lightfield provides true **scene-referred 32-bit linear tone processing**, **Adobe Lightroom DNG preset & mask compatibility**, and **on-device AI subject/sky segmentation** — delivering desktop-class color grading directly to your phone.

---

## ✨ Standout Features That Hook Creators

### 🎨 1. Pro Color Science & Skin-Tone Protection
- **Smart Vibrance:** Uses $Y C_b C_r$ chrominance isolation to boost dull scenery without making human faces look sunburned.
- **8-Channel HSL Mixer:** Surgical control over Hue, Saturation, and Luminance for Red, Orange, Yellow, Green, Aqua, Blue, Purple, and Magenta.
- **3-Way Color Split Grading:** Dial in cinematic warmth in the highlights, rich teals in the shadows, and balance in the midtones.

### 📈 2. Fritsch-Carlson Monotone Tone Curves
- Zero posterization. Zero staircases. Zero color banding.
- Independent **Composite RGB**, **Red**, **Green**, and **Blue** channel curves with 5 draggable control points, mapped through real-time 256-LUT interpolation.

### 🎭 3. AI Local Masking & Adobe DNG Presets
- **One-Tap AI Subject & Person Detection:** Instantly isolates the subject for targeted exposure, warmth, contrast, and clarity adjustments.
- **AI Sky Selection:** Deepen dramatic skies without darkening mountain peaks or trees.
- **Adobe CRS DNG Import:** Open DNG files exported from Adobe Lightroom Mobile or Desktop — Lightfield parses and synchronizes all local mask sliders automatically.
- **Linear & Radial Gradients + Brushes:** Fast, smooth separable feathering with zero edge fringing.

### 🔍 4. Aspect-Preserving Before / After Comparison
- Real-time interactive vertical divider bar. Swipe back and forth across the image to evaluate subtle color grades without distortion.
- Live 256-bin RGB Luminance Histogram with highlight and shadow clipping indicators.

### 📱 5. Built for Real Phones (720×1520 & Beyond)
- Optimized for mobile viewports, including **720×1520 px (19:9)** and edge-to-edge layouts.
- **Zero Slider Lag:** High-speed 1280px interactive viewport buffer with in-flight coroutine job cancellation delivers a silky-smooth **60fps** editing experience.
- Minimum 48dp touch targets on every handle, slider, and tool.

### 🚀 6. Multi-Format High-Fidelity Studio Export
- Export in **JPEG** (customizable 50%–100% quality), **Lossless PNG**, or modern **WebP**.
- Resolution presets: **Original (Full Resolution)**, **2048px (Social Media)**, or **1080px (Web Optimized)**.
- **Studio Watermark Engine:** Add your custom artist tag or brand watermark with resolution-scaled typography and soft drop shadow.

---

## 🛠️ Architecture & Color Pipeline Hierarchy

Lightfield adopts a non-destructive pipeline where the original photo remains untouched:

```
[Immutable Digital Negative]
         │
         ▼
[1. Centered Aspect Ratio Cropping & Geometry] (1:1, 4:5, 16:9, etc.)
         │
         ▼
[2. Optics: Linear Lens Vignette Correction]
         │
         ▼
[3. Scene-Referred Linear Light Tone Stage] (Exposure 2^ΔEV, Highlights, Shadows)
         │
         ▼
[4. Fritsch-Carlson Monotone Spline Curves] (RGB, R, G, B channels)
         │
         ▼
[5. White Balance, Temperature & Tint Matrix]
         │
         ▼
[6. 8-Channel HSL Mixer & 3-Way Split Tone Color Grading]
         │
         ▼
[7. Vibrance with Skin-Tone YCbCr Locus Protection & Saturation]
         │
         ▼
[8. Texture, Clarity, Dehaze, Vignette & Film Grain]
         │
         ▼
[9. High-Pass Unsharp Masking & Local Noise Reduction]
         │
         ▼
[10. Portrait Retouching (Skin Smoothing, Face Light, Lip Tint)]
         │
         ▼
[11. Per-Pixel AI Mask Composite (Subject, Sky, Gradient, Brush)]
         │
         ▼
[Interactive 1280px Preview @ 60fps] / [Full-Res Lossless Export]
```

---

## 🚀 Quick Start Guide

### Requirements
- **OS:** Android 8.1 (Oreo MR1, API 27) or higher (tested through Android 14+ / API 36).
- **Display:** Responsive on all screens; tailored for standard 720×1520 and modern tall displays.
- **Language & Toolkit:** Kotlin 2.0+ & Jetpack Compose (Material Design 3).

### How to Edit a Photo in 3 Steps:
1. **Import:** Tap the gallery icon to pick any photo or Adobe DNG file.
2. **Grade:** Adjust Exposure, Tone Curves, and HSL colors with real-time feedback.
3. **Export:** Tap the Share/Export button, choose your format (JPEG/PNG/WebP), toggle your studio watermark, and save or share instantly.

---

## 📜 Open-Source Lineage & Credits
Lightfield's processing principles are calibrated against three pioneering open-source projects:
- **[LightCraft](https://github.com/storytold/lightcraft):** Primary authority for scene-referred linear exposure, skin-protected vibrance, and monotone spline curves.
- **[CalibRaw](https://github.com/Duecki1/CalibRaw):** Guidance for mobile touch ergonomics, local AI segmentation, and non-destructive metadata.
- **[RapidRAW](https://github.com/CyberTimon/RapidRAW):** Two-tier decoupled preview/export rendering and multi-format WebP pipeline.

---

## 📄 License
This project is licensed under the Apache License, Version 2.0. Feel free to use, inspect, and elevate your mobile photography workflow!
