package com.availelabs.aurela.eia

import java.net.URI
import kotlin.time.Instant

data class EiaBulkDataManifestEntry(
    val datasetId: EiaDatasetId,
    val lastUpdated: Instant,
    val accessUrl: URI
)

@JvmInline
value class EiaDatasetId(val value: String) {
    init {
        require(PATTERN.matches(value)) { "Invalid dataset ID: $value" }
    }

    private companion object {
        val PATTERN = Regex("[A-Z][A-Z0-9_]*")
    }
}