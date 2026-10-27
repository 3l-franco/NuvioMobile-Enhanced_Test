package com.nuvio.app.features.details.components

import com.nuvio.app.features.details.MetaVideo
import com.nuvio.app.features.watchprogress.buildPlaybackVideoId

internal fun episodeDownloadSeasonKey(
    parentMetaId: String,
    seasonNumber: Int,
    episodeNumber: Int,
): String = "$parentMetaId|season:$seasonNumber|episode:$episodeNumber"

internal fun episodeDownloadVideoKey(parentMetaId: String, videoId: String): String =
    "$parentMetaId|video:$videoId"

internal fun MetaVideo.isDownloadedEpisode(
    parentMetaId: String,
    downloadedEpisodeKeys: Set<String>,
): Boolean {
    val seasonNumber = season
    val episodeNumber = episode
    if (
        seasonNumber != null &&
        episodeNumber != null &&
        episodeDownloadSeasonKey(parentMetaId, seasonNumber, episodeNumber) in downloadedEpisodeKeys
    ) {
        return true
    }

    if (episodeDownloadVideoKey(parentMetaId, id) in downloadedEpisodeKeys) return true

    val playbackVideoId = buildPlaybackVideoId(
        parentMetaId = parentMetaId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        fallbackVideoId = id,
    )
    return episodeDownloadVideoKey(parentMetaId, playbackVideoId) in downloadedEpisodeKeys
}
