package com.availelabs.aurela.eia

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class EiaBulkDataService(private val bulkDataClient: EiaBulkDataClient) {
    private val bulkDataDirectory: Path =
        Path.of(
            System.getProperty("user.home"),
            "Aurela",
            "workspace",
            "eia",
            "bulk-data",
        ).toAbsolutePath().normalize()

    init {
        Files.createDirectories(bulkDataDirectory)
    }

    fun upsertBulkData(dataSetId: String): BulkDataUpsertOutcome {
        val downloadedFile = bulkDataClient.downloadDatasetOrNull(dataSetId)
            ?: return BulkDataUpsertOutcome.NOT_FOUND

        val destination = bulkDataDirectory.resolve("$dataSetId.zip")
        Files.write(destination, downloadedFile)

        return BulkDataUpsertOutcome.UPSERTED
    }
}

enum class BulkDataUpsertOutcome {
    UPSERTED,
    NOT_FOUND
}