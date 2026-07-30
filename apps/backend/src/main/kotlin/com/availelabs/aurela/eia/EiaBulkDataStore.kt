package com.availelabs.aurela.eia

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.time.Instant

@Component
class EiaBulkDataStore(
    properties: EiaBulkDataProperties,
    private val jsonMapper: JsonMapper,
) {
    private val directory =
        properties.directory.toAbsolutePath().normalize()

    init {
        Files.createDirectories(directory)
    }

    fun archivePath(
        datasetId: EiaDatasetId,
    ): Path =
        directory.resolve("${datasetId.value}.zip")

    fun createTemporaryArchive(
        datasetId: EiaDatasetId,
    ): Path =
        Files.createTempFile(
            directory,
            "${datasetId.value}.",
            ".zip.part",
        )

    fun deleteTemporaryArchive(
        temporaryArchive: Path,
    ) {
        Files.deleteIfExists(temporaryArchive)
    }

    fun isCurrent(
        manifestEntry: EiaBulkDataManifestEntry,
    ): Boolean {
        val archivePath =
            archivePath(manifestEntry.datasetId)

        val metadataPath =
            metadataPath(manifestEntry.datasetId)

        if (!Files.isRegularFile(archivePath)) {
            return false
        }

        if (!Files.isRegularFile(metadataPath)) {
            return false
        }

        val metadata = readMetadata(metadataPath)
            ?: return false

        return metadata.datasetId ==
            manifestEntry.datasetId &&
            metadata.lastUpdated ==
            manifestEntry.lastUpdated &&
            metadata.accessUrl ==
            manifestEntry.accessUrl &&
            metadata.sizeBytes ==
            Files.size(archivePath)
    }

    fun install(
        manifestEntry: EiaBulkDataManifestEntry,
        temporaryArchive: Path,
        download: EiaBulkDataDownloadResult.Downloaded,
    ) {
        require(Files.isRegularFile(temporaryArchive)) {
            "Temporary archive does not exist: $temporaryArchive"
        }

        require(
            Files.size(temporaryArchive) ==
                download.sizeBytes,
        ) {
            "Temporary archive size changed before installation"
        }

        val destinationArchive =
            archivePath(manifestEntry.datasetId)

        val destinationMetadata =
            metadataPath(manifestEntry.datasetId)

        val temporaryMetadata = Files.createTempFile(
            directory,
            "${manifestEntry.datasetId.value}.",
            ".metadata.json.part",
        )

        try {
            writeMetadata(
                path = temporaryMetadata,
                metadata = LocalMetadata(
                    datasetId =
                        manifestEntry.datasetId,
                    lastUpdated =
                        manifestEntry.lastUpdated,
                    accessUrl =
                        manifestEntry.accessUrl,
                    sizeBytes =
                        download.sizeBytes,
                    sha256 =
                        download.sha256,
                ),
            )

            moveReplacing(
                source = temporaryArchive,
                destination = destinationArchive,
            )

            moveReplacing(
                source = temporaryMetadata,
                destination = destinationMetadata,
            )
        } finally {
            Files.deleteIfExists(temporaryMetadata)
        }
    }

    private fun metadataPath(
        datasetId: EiaDatasetId,
    ): Path =
        directory.resolve(
            "${datasetId.value}.metadata.json",
        )

    private fun readMetadata(
        path: Path,
    ): LocalMetadata? =
        try {
            Files.newInputStream(path).use { input ->
                jsonMapper.readValue(
                    input,
                    EiaBulkDataMetadataDto::class.java,
                ).toDomain()
            }
        } catch (_: Exception) {
            /*
            Invalid local metadata means that the archive cannot be trusted
            as current. Returning null causes the next synchronization to
            repair it.
             */
            null
        }

    private fun writeMetadata(
        path: Path,
        metadata: LocalMetadata,
    ) {
        Files.newBufferedWriter(path).use { writer ->
            jsonMapper.writeValue(
                writer,
                EiaBulkDataMetadataDto.from(metadata),
            )
        }
    }

    private fun moveReplacing(
        source: Path,
        destination: Path,
    ) {
        try {
            Files.move(
                source,
                destination,
                ATOMIC_MOVE,
                REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                source,
                destination,
                REPLACE_EXISTING,
            )
        }
    }
}

private data class LocalMetadata(
    val datasetId: EiaDatasetId,
    val lastUpdated: Instant,
    val accessUrl: URI,
    val sizeBytes: Long,
    val sha256: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class EiaBulkDataMetadataDto @JsonCreator constructor(
    @param:JsonProperty("datasetId")
    val datasetId: String,

    @param:JsonProperty("lastUpdated")
    val lastUpdated: String,

    @param:JsonProperty("accessUrl")
    val accessUrl: URI,

    @param:JsonProperty("sizeBytes")
    val sizeBytes: Long,

    @param:JsonProperty("sha256")
    val sha256: String,
) {
    fun toDomain(): LocalMetadata {
        require(sizeBytes >= 0) {
            "Local EIA metadata size must not be negative"
        }

        require(SHA_256_PATTERN.matches(sha256)) {
            "Local EIA metadata contains an invalid SHA-256 value"
        }

        return LocalMetadata(
            datasetId = EiaDatasetId(datasetId),
            lastUpdated = Instant.parse(lastUpdated),
            accessUrl = accessUrl,
            sizeBytes = sizeBytes,
            sha256 = sha256,
        )
    }

    companion object {
        private val SHA_256_PATTERN =
            Regex("[0-9a-f]{64}")

        fun from(
            metadata: LocalMetadata,
        ): EiaBulkDataMetadataDto =
            EiaBulkDataMetadataDto(
                datasetId = metadata.datasetId.value,
                lastUpdated =
                    metadata.lastUpdated.toString(),
                accessUrl = metadata.accessUrl,
                sizeBytes = metadata.sizeBytes,
                sha256 = metadata.sha256,
            )
    }
}
