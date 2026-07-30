package com.availelabs.aurela.eia

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount.twice
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import tools.jackson.databind.json.JsonMapper
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EiaBulkDataServiceTest {
    private val restClientBuilder = RestClient.builder()
    private val mockServer = MockRestServiceServer
        .bindTo(restClientBuilder).build()
    private val jsonMapper = JsonMapper.builder().build()

    @TempDir
    lateinit var tempDirectory: Path

    @AfterEach
    fun verifyRequests() {
        mockServer.verify()
    }

    @Test
    fun `downloads once and then uses manifest metadata`() {
        val manifestJson = """
            {
              "dataset": {
                "AEO.2014": {
                  "data_set": "AEO.2014",
                  "identifier": "AEO.2014",
                  "last_updated": "2026-07-22T17:49:48-04:00",
                  "accessURL": "https://www.eia.gov/opendata/bulk/AEO2014.zip"
                }
              }
            }
        """.trimIndent()

        mockServer.expect(
            twice(),
            requestTo(
                "https://www.eia.gov/opendata/bulk/manifest.txt",
            ),
        ).andRespond(
            withSuccess(
                manifestJson,
                MediaType.APPLICATION_JSON,
            ),
        )

        mockServer.expect(
            requestTo(
                "https://www.eia.gov/opendata/bulk/AEO2014.zip",
            ),
        ).andRespond(
            withSuccess(
                createArchiveBytes(),
                MediaType.APPLICATION_OCTET_STREAM,
            ),
        )

        val bulkDataStore = EiaBulkDataStore(
            EiaBulkDataProperties(tempDirectory),
            jsonMapper,
        )
        val service = EiaBulkDataService(
            EiaBulkDataClient(
                restClientBuilder,
                jsonMapper,
            ),
            bulkDataStore,
            EiaBulkArchive(),
        )
        val datasetId = EiaDatasetId("AEO.2014")

        assertEquals(
            EiaBulkDataSynchronizationOutcome.DOWNLOADED,
            service.synchronizeBulkData(datasetId),
        )
        assertEquals(
            EiaBulkDataSynchronizationOutcome.UNCHANGED,
            service.synchronizeBulkData(datasetId),
        )
        assertTrue(
            Files.isRegularFile(
                bulkDataStore.archivePath(datasetId),
            ),
        )
        assertTrue(
            Files.isRegularFile(
                tempDirectory.resolve(
                    "AEO.2014.metadata.json",
                ),
            ),
        )
    }

    private fun createArchiveBytes(): ByteArray {
        val archivePath =
            tempDirectory.resolve("response.zip")

        ZipOutputStream(
            Files.newOutputStream(archivePath),
        ).use { zipOutput ->
            zipOutput.putNextEntry(
                ZipEntry("AEO2014.txt"),
            )
            zipOutput.write("{}\n".toByteArray())
            zipOutput.closeEntry()
        }

        return Files.readAllBytes(archivePath)
    }
}
