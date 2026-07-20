package com.availelabs.aurela.eia

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Exposes endpoints for Energy Information Administration (EIA) bulk data.
 */
@RestController
@RequestMapping("/api/eia/bulk-data")
class EiaBulkDataController {
    /**
     * Accepts a request for an EIA bulk dataset.
     *
     * Dataset retrieval is not implemented yet, so the endpoint currently returns an empty
     * successful response.
     *
     * @param dataSetId identifier of the requested EIA dataset
     */
    @GetMapping("/{dataSetId}")
    fun getBulkData(@PathVariable dataSetId: String) {

    }

}
