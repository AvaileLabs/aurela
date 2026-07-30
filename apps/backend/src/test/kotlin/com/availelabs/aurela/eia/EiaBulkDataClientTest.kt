package com.availelabs.aurela.eia

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.io.TempDir
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.HexFormat
import kotlin.test.Test
import kotlin.time.Instant

class EiaBulkDataClientTest {
    private val restClientBuilder = RestClient.builder()
    private val mockServer = MockRestServiceServer
        .bindTo(restClientBuilder).build()
    private val client = EiaBulkDataClient(
        restClientBuilder,
        JsonMapper.builder().build(),
    )

    @TempDir
    lateinit var tempDirectory: Path

    @AfterEach
    fun verifyRequests() {
        mockServer.verify()
    }

    @Test
    fun `parses the manifest into typed domain objects`() {
        val manifestJson = """
            {
              "dataset": {
                "AEO.2014": {
                  "data_set": "AEO.2014",
                  "identifier": "AEO.2014",
                  "last_updated": "2026-07-22T17:49:48-04:00",
                  "accessURL": "https://www.eia.gov/opendata/bulk/AEO2014.zip",
                  "name": "Ignored"
                }
              }
            }
        """.trimIndent()

        mockServer.expect(
            requestTo(
                "https://www.eia.gov/opendata/bulk/manifest.txt",
            ),
        ).andRespond(
            withSuccess(
                manifestJson,
                MediaType.APPLICATION_JSON,
            ),
        )

        val datasetId = EiaDatasetId("AEO.2014")
        val manifestEntry =
            requireNotNull(client.downloadManifest()[datasetId])

        assertEquals(datasetId, manifestEntry.datasetId)
        assertEquals(
            Instant.parse("2026-07-22T21:49:48Z"),
            manifestEntry.lastUpdated,
        )
        assertEquals(
            URI.create(
                "https://www.eia.gov/opendata/bulk/AEO2014.zip",
            ),
            manifestEntry.accessUrl,
        )
    }

    @Test
    fun `streams the manifest access URL to disk`() {
        val expectedFile = byteArrayOf(
            0x50,
            0x4B,
            0x03,
            0x04,
        )
        val manifestEntry = manifestEntry()

        mockServer.expect(
            requestTo(manifestEntry.accessUrl),
        ).andRespond(
            withSuccess(
                expectedFile,
                MediaType.APPLICATION_OCTET_STREAM,
            ),
        )

        val destination =
            tempDirectory.resolve("download.zip.part")

        val result = assertInstanceOf(
            EiaBulkDataDownloadResult.Downloaded::class.java,
            client.downloadDataset(
                manifestEntry,
                destination,
            ),
        )

        assertArrayEquals(
            expectedFile,
            Files.readAllBytes(destination),
        )
        assertEquals(expectedFile.size.toLong(), result.sizeBytes)
        assertEquals(
            HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                    .digest(expectedFile),
            ),
            result.sha256,
        )
    }

    @Test
    fun `returns not found when the manifest archive disappeared`() {
        val manifestEntry = manifestEntry()

        mockServer.expect(
            requestTo(manifestEntry.accessUrl),
        ).andRespond(
            withStatus(HttpStatus.NOT_FOUND),
        )

        val result = client.downloadDataset(
            manifestEntry,
            tempDirectory.resolve("missing.zip.part"),
        )

        assertSame(
            EiaBulkDataDownloadResult.NotFound,
            result,
        )
    }

    private fun manifestEntry() =
        EiaBulkDataManifestEntry(
            datasetId = EiaDatasetId("AEO.2014"),
            lastUpdated =
                Instant.parse("2026-07-22T21:49:48Z"),
            accessUrl = URI.create(
                "https://www.eia.gov/opendata/bulk/AEO2014.zip",
            ),
        )
}
