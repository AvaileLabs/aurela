package com.availelabs.aurela.eia

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class EiaBulkDataService(
    private val bulkDataClient: EiaBulkDataClient,
    private val bulkDataStore: EiaBulkDataStore,
    private val bulkArchive: EiaBulkArchive,
) {
    private val synchronizationLocks =
        ConcurrentHashMap<EiaDatasetId, ReentrantLock>()

    fun synchronizeBulkData(
        datasetId: EiaDatasetId,
    ): EiaBulkDataSynchronizationOutcome {
        val synchronizationLock =
            synchronizationLocks.computeIfAbsent(datasetId) {
                ReentrantLock()
            }

        return synchronizationLock.withLock {
            synchronizeLocked(datasetId)
        }
    }

    private fun synchronizeLocked(
        datasetId: EiaDatasetId,
    ): EiaBulkDataSynchronizationOutcome {
        val manifestEntry =
            bulkDataClient.downloadManifest()[datasetId]
                ?: return EiaBulkDataSynchronizationOutcome.NOT_FOUND

        if (bulkDataStore.isCurrent(manifestEntry)) {
            return EiaBulkDataSynchronizationOutcome.UNCHANGED
        }

        val temporaryArchive =
            bulkDataStore.createTemporaryArchive(datasetId)

        try {
            return when (
                val download = bulkDataClient.downloadDataset(
                    manifestEntry,
                    temporaryArchive,
                )
            ) {
                EiaBulkDataDownloadResult.NotFound ->
                    EiaBulkDataSynchronizationOutcome.NOT_FOUND

                is EiaBulkDataDownloadResult.Downloaded -> {
                    bulkArchive.validate(
                        temporaryArchive,
                        manifestEntry.accessUrl,
                    )

                    bulkDataStore.install(
                        manifestEntry,
                        temporaryArchive,
                        download,
                    )

                    EiaBulkDataSynchronizationOutcome.DOWNLOADED
                }
            }
        } finally {
            bulkDataStore.deleteTemporaryArchive(temporaryArchive)
        }
    }
}

enum class EiaBulkDataSynchronizationOutcome {
    DOWNLOADED,
    UNCHANGED,
    NOT_FOUND,
}
