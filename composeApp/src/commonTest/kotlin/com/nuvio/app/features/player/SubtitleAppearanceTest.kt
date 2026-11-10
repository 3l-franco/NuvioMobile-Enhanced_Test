package com.nuvio.app.features.player

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubtitleAppearanceTest {
    @Test
    fun oldAndInvalidSettingsKeepExistingAppearance() {
        listOf(null, "", "{", "null").forEach { assertEquals(SubtitleAppearance(), decodeSubtitleAppearance(it)) }
        assertTrue(SubtitleAppearance().shadowEnabled)
        assertEquals(DEFAULT_SUBTITLE_SHADOW_OPACITY, SubtitleAppearance().shadowOpacity)
        assertFalse(SubtitleStyleState.DEFAULT.usesStyledOverlay)
        assertEquals(SubtitleAppearance(shadowEnabled = true), decodeSubtitleAppearance("""{"shadowEnabled":true,"future":1}"""))
        assertFalse(decodeSubtitleAppearance("""{"shadowEnabled":false,"shadowOpacity":40}""").shadowEnabled)
    }

    @Test
    fun syncedValuesAreBoundedAndCannotReferenceFiles() {
        val style = decodeSubtitleAppearance("""{"fontId":"../../font.ttf","backgroundRadius":999,"shadowOpacity":-1,"shadowSize":999}""")
        assertEquals("default", style.fontId)
        assertEquals(24, style.backgroundRadius)
        assertEquals(MIN_SUBTITLE_SHADOW_OPACITY, style.shadowOpacity)
        assertEquals(8, style.shadowSize)
    }

    @Test
    fun shadowOpacityMatchesReferenceTenPercentSteps() {
        assertEquals(10, normalizeSubtitleShadowOpacity(-20))
        assertEquals(10, normalizeSubtitleShadowOpacity(4))
        assertEquals(20, normalizeSubtitleShadowOpacity(15))
        assertEquals(70, normalizeSubtitleShadowOpacity(72))
        assertEquals(80, normalizeSubtitleShadowOpacity(75))
        assertEquals(100, normalizeSubtitleShadowOpacity(250))
    }

    @Test
    fun shadowGeometryScalesWithSubtitleTextSize() {
        assertEquals(4f, subtitleShadowOffsetPx(48f), 0.0001f)
        assertEquals(1.5f, subtitleShadowBlurRadiusPx(48f), 0.0001f)
    }

    @Test
    fun shadowIsDisabledWhenSubtitleBackgroundBoxIsVisible() {
        val default = SubtitleStyleState.DEFAULT
        assertTrue(default.drawsSubtitleShadow)
        assertEquals(0xB3000000.toInt(), default.subtitleShadowColor.toArgb())
        assertFalse(default.copy(backgroundColor = Color.Black.copy(alpha = 0.5f)).drawsSubtitleShadow)
        assertFalse(default.copy(appearance = default.appearance.copy(shadowEnabled = false)).drawsSubtitleShadow)
    }

    @Test
    fun outlineBlurDefaultsToZeroAndIsBounded() {
        assertEquals(0, SubtitleAppearance().outlineBlur)
        assertEquals(0, SubtitleAppearance(outlineBlur = -5).normalized().outlineBlur)
        assertEquals(10, SubtitleAppearance(outlineBlur = 50).normalized().outlineBlur)
    }

    @Test
    fun outlineBlurUsesIndependentLibassHorizontalAndVerticalScales() {
        assertEquals(0f, subtitleOutlineBlurSigmaXPx(0, 1280f))
        assertEquals(0f, subtitleOutlineBlurSigmaYPx(0, 720f))
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE, subtitleOutlineBlurSigmaXPx(1, 1280f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE, subtitleOutlineBlurSigmaYPx(1, 720f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE * 1.5f, subtitleOutlineBlurSigmaXPx(1, 1920f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE * 1.5f, subtitleOutlineBlurSigmaYPx(1, 1080f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE * 3f, subtitleOutlineBlurSigmaXPx(1, 3840f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE * 3f, subtitleOutlineBlurSigmaYPx(1, 2160f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE * 10f, subtitleOutlineBlurSigmaXPx(10, 1280f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE * 10f, subtitleOutlineBlurSigmaYPx(10, 720f), 0.0001f)
        assertEquals(0f, subtitleOutlineBlurSigmaXPx(-1, 1280f))
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE * 10f, subtitleOutlineBlurSigmaYPx(11, 720f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE, subtitleOutlineBlurSigmaXPx(1, 0f), 0.0001f)
        assertEquals(SUBTITLE_BLUR_SIGMA_FACTOR * SUBTITLE_BLUR_SOFTNESS_SCALE, subtitleOutlineBlurSigmaYPx(1, Float.NaN), 0.0001f)
    }

    @Test
    fun subtitleVideoBoundsRespectFitFillAndZoomModes() {
        val fit = subtitleVideoBoundsPx(1920f, 1080f, 4f / 3f, PlayerResizeMode.Fit)
        assertEquals(1440f, fit.width, 0.001f)
        assertEquals(1080f, fit.height, 0.001f)

        val fill = subtitleVideoBoundsPx(1920f, 1080f, 4f / 3f, PlayerResizeMode.Fill)
        assertEquals(1920f, fill.width, 0.001f)
        assertEquals(1080f, fill.height, 0.001f)

        val zoom = subtitleVideoBoundsPx(1920f, 1080f, 4f / 3f, PlayerResizeMode.Zoom)
        assertEquals(1920f, zoom.width, 0.001f)
        assertEquals(1440f, zoom.height, 0.001f)

        val unknown = subtitleVideoBoundsPx(0f, 0f, 0f, PlayerResizeMode.Fit)
        assertEquals(0f, unknown.width)
        assertEquals(0f, unknown.height)
    }

    @Test
    fun shadowAloneStaysOnTheNativeLibmpvPath() {
        assertTrue(SubtitleStyleState.DEFAULT.drawsSubtitleShadow)
        assertFalse(SubtitleStyleState.DEFAULT.requiresComposeStyledOverlay)
        assertFalse(SubtitleStyleState.DEFAULT.usesStyledOverlay)
    }

    @Test
    fun blurEnabledTextSubtitlesUseTheSharedComposeRenderer() {
        val blurOnly = SubtitleStyleState(appearance = SubtitleAppearance(outlineBlur = 4))
        assertTrue(blurOnly.usesStyledOverlay)
        assertTrue(blurOnly.requiresComposeStyledOverlay)
        assertEquals(4, blurOnly.mpvNativeOutlineBlurLevel)
        assertEquals(6f, blurOnly.mpvNativeOutlineBlurAmount)
        val maximumBlur = blurOnly.copy(appearance = blurOnly.appearance.copy(outlineBlur = 10))
        assertEquals(15f, maximumBlur.mpvNativeOutlineBlurAmount)

        val customFont = blurOnly.copy(appearance = blurOnly.appearance.copy(fontId = "serif"))
        assertTrue(customFont.requiresComposeStyledOverlay)
        assertEquals(4, customFont.mpvNativeOutlineBlurLevel)

        val outlineOff = blurOnly.copy(outlineEnabled = false)
        assertFalse(outlineOff.requiresComposeStyledOverlay)
        assertFalse(outlineOff.usesStyledOverlay)
        assertEquals(0, outlineOff.mpvNativeOutlineBlurLevel)
        assertEquals(0f, outlineOff.mpvNativeOutlineBlurAmount)
        assertEquals(4, outlineOff.appearance.outlineBlur)

        val zeroWidth = blurOnly.copy(outlineWidth = 0)
        assertFalse(zeroWidth.requiresComposeStyledOverlay)
        assertEquals(0, zeroWidth.mpvNativeOutlineBlurLevel)
    }

    @Test
    fun legacyAppearancePayloadDefaultsOutlineBlurToZeroAndNewValueRoundTrips() {
        val legacy = decodeSubtitleAppearance(
            """{"fontId":"serif","backgroundRadius":4,"shadowEnabled":true,"shadowOpacity":65,"shadowSize":3}""",
        )
        assertEquals(0, legacy.outlineBlur)
        val current = SubtitleAppearance(fontId = "serif", outlineBlur = 7)
        assertEquals(current, decodeSubtitleAppearance(current.encode()))
    }

    @Test
    fun outlineBlurActivatesStyledOverlayOnlyWhileOutlineIsEnabled() {
        assertTrue(SubtitleStyleState(appearance = SubtitleAppearance(outlineBlur = 4)).usesStyledOverlay)
        assertFalse(
            SubtitleStyleState(
                outlineEnabled = false,
                appearance = SubtitleAppearance(outlineBlur = 4),
            ).usesStyledOverlay,
        )
    }

    @Test
    fun resetKeepsSubtitleSelectionPreferences() {
        val reset = SubtitleStyleState(
            stripSdh = true, useForcedSubtitles = true, showOnlyPreferredLanguages = true,
            backgroundColor = Color.Red, appearance = SubtitleAppearance(fontId = "serif", shadowEnabled = true),
        ).resetAppearance()
        assertTrue(reset.stripSdh && reset.useForcedSubtitles && reset.showOnlyPreferredLanguages)
        assertEquals(SubtitleStyleState.DEFAULT.backgroundColor, reset.backgroundColor)
        assertEquals(SubtitleAppearance(), reset.appearance)
    }

    @Test
    fun styledAndImageTracksStayWithTheirNativeRenderer() {
        listOf("ass", "ssa", "hdmv_pgs_subtitle", "dvd_subtitle", "dvb_subtitle", "", "unknown").forEach {
            assertFalse(SubtitleRendering.supportsTextOverlay(it), it)
        }
        listOf("subrip", "webvtt", "mov_text", "SRT").forEach {
            assertTrue(SubtitleRendering.supportsTextOverlay(it), it)
        }
    }

    @Test
    fun fontImportRejectsTruncatedTablesAndUnsupportedContainers() {
        val truncated = byteArrayOf(0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0)
        assertFalse(isSupportedSubtitleFont("font.ttf", truncated))
        assertFalse(isSupportedSubtitleFont("font.otf", "this is not a font".encodeToByteArray()))
        assertFalse(isSupportedSubtitleFont("font.woff", truncated))
        val overflowing = truncated + ByteArray(16) { -1 }
        assertFalse(isSupportedSubtitleFont("font.ttf", overflowing))
    }
}
