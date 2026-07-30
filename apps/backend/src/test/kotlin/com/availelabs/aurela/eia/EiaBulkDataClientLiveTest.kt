package com.availelabs.aurela.eia

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import org.springframework.web.client.RestClient
import tools.jackson.databind.json.JsonMapper
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Tag("live")
class EiaBulkDataClientLiveTest {
    private val client = EiaBulkDataClient(
        RestClient.builder(),
        JsonMapper.builder().build(),
    )

    @TempDir
    lateinit var tempDirectory: Path

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    fun `downloads a manifest archive from EIA`() {
        val manifestEntry = assertNotNull(
            client.downloadManifest()[
                EiaDatasetId("TOTAL")
            ],
        )

        val destination =
            tempDirectory.resolve("TOTAL.zip.part")

        val result = assertIs<EiaBulkDataDownloadResult.Downloaded>(
            client.downloadDataset(
                manifestEntry,
                destination,
            ),
        )

        assertTrue(result.sizeBytes > 0)
    }
}
