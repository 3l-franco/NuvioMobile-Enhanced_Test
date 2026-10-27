package com.nuvio.app.features.details.components

import com.nuvio.app.features.details.MetaVideo
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodeNavigatorDownloadFilterTest {
    @Test
    fun downloadedOnlyShowsOnlyDownloadedEpisodes() {
        val episodes = listOf(
            MetaVideo(id = "ep1", title = "Episode 1", season = 1, episode = 1),
            MetaVideo(id = "ep2", title = "Episode 2", season = 1, episode = 2),
            MetaVideo(id = "ep3", title = "Episode 3", season = 1, episode = 3),
        )

        val result = episodes.filteredAndSortedForNavigator(
            query = "",
            sort = EpisodeSortOrder.FirstToLast,
            isWatched = { false },
            downloadedOnly = true,
            isDownloaded = { it.id == "ep2" || it.id == "ep3" },
        )

        assertEquals(listOf("ep2", "ep3"), result.map { it.id })
    }

    @Test
    fun downloadedOnlyCombinesWithEpisodeSearch() {
        val episodes = listOf(
            MetaVideo(id = "ep1", title = "Pilot", season = 1, episode = 1),
            MetaVideo(id = "ep2", title = "The Chase", season = 1, episode = 2),
            MetaVideo(id = "ep3", title = "The Return", season = 1, episode = 3),
        )

        val result = episodes.filteredAndSortedForNavigator(
            query = "The",
            sort = EpisodeSortOrder.FirstToLast,
            isWatched = { false },
            downloadedOnly = true,
            isDownloaded = { it.id == "ep3" },
        )

        assertEquals(listOf("ep3"), result.map { it.id })
    }
}
