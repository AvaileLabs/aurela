package com.availelabs.aurela.eia

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import kotlin.test.Test

class EiaBulkDataClientTest {
    private val restClientBuilder = RestClient.builder()
    private val mockServer = MockRestServiceServer
        .bindTo(restClientBuilder).build()
    private val client = EiaBulkDataClient(restClientBuilder)

    @AfterEach
    fun verifyRequests() { mockServer.verify() }

    @Test
    fun `returns downloaded bytes`() {
        val expectedFile = byteArrayOf(
            0x50,
            0x4B,
            0x03,
            0x04,
        )

        mockServer.expect(
            requestTo("https://www.eia.gov/opendata/bulk/PET.zip")
        ).andRespond(
            withSuccess(
                expectedFile,
                MediaType.APPLICATION_OCTET_STREAM)
        )

        val downloadedFile = client.downloadDatasetOrNull("PET")

        assertArrayEquals(expectedFile, downloadedFile)
    }

    @Test
    fun `returns nulls when dataset does not exist`() {
        mockServer.expect(
            requestTo("https://www.eia.gov/opendata/bulk/MISSING.zip")
        ).andRespond(
            withStatus(HttpStatus.NOT_FOUND)
        )

        val downloadedFile = client.downloadDatasetOrNull("MISSING")
        assertNull(downloadedFile)
    }
}