package com.nuvio.app.features.player

internal object PlayerNightMode {
    const val MPV_FILTER_LABEL = "@nuvio_night"

    const val MPV_FILTER =
        "$MPV_FILTER_LABEL:lavfi=[acompressor=threshold=0.063:ratio=6:attack=5:release=300:makeup=3.98:detection=rms]"
}

internal val PlayerSettingsUiState.nightModeActive: Boolean
    get() = nightModeButtonEnabled && nightModeEnabled
