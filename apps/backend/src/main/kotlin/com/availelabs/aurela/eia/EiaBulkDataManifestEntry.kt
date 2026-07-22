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
        /*
        Matches a sequence that starts with an ASCII uppercase letter,
        followed by zero or more ASCII uppercase letters, digits, or underscores.
         */
        val PATTERN = Regex("[A-Z][A-Z0-9_]*")
    }
}