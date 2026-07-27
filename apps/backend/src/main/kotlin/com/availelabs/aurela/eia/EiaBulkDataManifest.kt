package com.availelabs.aurela.eia

data class EiaBulkDataManifest(
    val entries: Map<EiaDatasetId, EiaBulkDataManifestEntry>
) {
    operator fun get(id: EiaDatasetId): EiaBulkDataManifestEntry? = entries[id]
}