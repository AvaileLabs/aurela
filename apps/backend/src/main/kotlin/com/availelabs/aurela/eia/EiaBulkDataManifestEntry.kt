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
        EIA identifiers include values such as:

        PET
        PET_IMPORTS
        AEO.2014
        IEO.2023

        Dots separate non-empty identifier segments. Slashes, backslashes,
        whitespace, and empty dot-separated segments are deliberately rejected.
         */
        val PATTERN =
            Regex("[A-Z][A-Z0-9_]*(?:\\.[A-Z0-9_]+)*")
    }
}