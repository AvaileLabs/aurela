package com.availelabs.aurela.eia

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/eia/bulk-data")
class EiaBulkDataController(
    private val bulkDataService: EiaBulkDataService,
) {
    @PutMapping("/{datasetId}")
    fun synchronizeBulkData(
        @PathVariable datasetId: String,
    ): ResponseEntity<EiaBulkDataSynchronizationOutcome> {
        val parsedDatasetId = try {
            EiaDatasetId(datasetId)
        } catch (exception: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                exception.message,
                exception,
            )
        }

        val outcome =
            bulkDataService.synchronizeBulkData(
                parsedDatasetId,
            )

        val status = when (outcome) {
            EiaBulkDataSynchronizationOutcome.NOT_FOUND ->
                HttpStatus.NOT_FOUND

            EiaBulkDataSynchronizationOutcome.DOWNLOADED,
            EiaBulkDataSynchronizationOutcome.UNCHANGED ->
                HttpStatus.OK
        }

        return ResponseEntity.status(status).body(
            outcome,
        )
    }
}
