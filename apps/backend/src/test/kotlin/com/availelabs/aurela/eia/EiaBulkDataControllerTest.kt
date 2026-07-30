package com.availelabs.aurela.eia

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class EiaBulkDataControllerTest {
    private val bulkDataService =
        mock(EiaBulkDataService::class.java)

    private val mockMvc = MockMvcBuilders
        .standaloneSetup(
            EiaBulkDataController(bulkDataService),
        )
        .build()

    @Test
    fun `synchronizes a dotted dataset ID with PUT`() {
        `when`(
            bulkDataService.synchronizeBulkData(
                EiaDatasetId("AEO.2014"),
            ),
        ).thenReturn(
            EiaBulkDataSynchronizationOutcome.DOWNLOADED,
        )

        mockMvc.perform(
            put("/api/eia/bulk-data/AEO.2014"),
        )
            .andExpect(status().isOk)
            .andExpect(content().json("\"DOWNLOADED\""))

        mockMvc.perform(
            get("/api/eia/bulk-data/AEO.2014"),
        )
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `returns not found when the dataset is absent`() {
        `when`(
            bulkDataService.synchronizeBulkData(
                EiaDatasetId("MISSING"),
            ),
        ).thenReturn(
            EiaBulkDataSynchronizationOutcome.NOT_FOUND,
        )

        mockMvc.perform(
            put("/api/eia/bulk-data/MISSING"),
        )
            .andExpect(status().isNotFound)
            .andExpect(content().json("\"NOT_FOUND\""))
    }

    @Test
    fun `rejects an invalid dataset ID`() {
        mockMvc.perform(
            put("/api/eia/bulk-data/not-valid"),
        )
            .andExpect(status().isBadRequest)
    }
}
