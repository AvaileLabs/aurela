package com.availelabs.aurela.eia

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/eia/bulk-data")
class EiaBulkDataController {
    @GetMapping("/{dataSetId}")
    fun getBulkData(@PathVariable dataSetId: String) {

    }
}
