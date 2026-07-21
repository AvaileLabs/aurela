package com.availelabs.aurela.eia

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertNotNull
import org.springframework.web.client.RestClient
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertTrue

class EiaBulkDataClientLiveTest {
    private val client = EiaBulkDataClient(RestClient.builder())

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    fun `downloads a valid zip file from EIA`() {
        val downloadedFile = client.downloadDatasetOrNull("TOTAL")
        assertNotNull(downloadedFile)
        assertTrue(downloadedFile.isNotEmpty())
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    fun `downloads a non-existent database from EIA returns null`() {
        val downloadedFile = client.downloadDatasetOrNull("DOES_NOT_EXIST")
        assertNull(downloadedFile)
    }
}