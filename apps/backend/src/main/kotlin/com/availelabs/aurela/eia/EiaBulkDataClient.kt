package com.availelabs.aurela.eia

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.HexFormat
import kotlin.time.Instant

@Component
class EiaBulkDataClient(
    restClientBuilder: RestClient.Builder,
    private val jsonMapper: JsonMapper,
) {
    private val restClient = restClientBuilder
        .baseUrl(EIA_BULK_DATA_BASE_URL)
        .defaultHeader(
            HttpHeaders.USER_AGENT,
            "Aurela EIA bulk-data client",
        )
        .build()

    fun downloadManifest(): EiaBulkDataManifest {
        val responseBody = restClient.get()
            .uri("manifest.txt")
            .exchange { _, response ->
                if (!response.statusCode.is2xxSuccessful) {
                    throw response.createException()
                }

                response.bodyTo(ByteArray::class.java)
                    ?: throw IllegalStateException(
                        "EIA returned no manifest response body",
                    )
            }

        val manifestDto = jsonMapper.readValue(
            responseBody,
            EiaBulkDataManifestDto::class.java,
        )

        return manifestDto.toDomain()
    }

    fun downloadDataset(
        manifestEntry: EiaBulkDataManifestEntry,
        destination: Path,
    ): EiaBulkDataDownloadResult {
        requireTrustedAccessUrl(manifestEntry.accessUrl)

        Files.createDirectories(
            requireNotNull(destination.parent) {
                "The download destination must have a parent directory"
            },
        )

        return restClient.get()
            .uri(manifestEntry.accessUrl)
            .exchange { _, response ->
                val status = response.statusCode

                if (status == HttpStatus.NOT_FOUND) {
                    return@exchange EiaBulkDataDownloadResult.NotFound
                }

                if (!status.is2xxSuccessful) {
                    throw response.createException()
                }

                val digest = MessageDigest.getInstance("SHA-256")

                val sizeBytes = Files.newOutputStream(
                    destination,
                    CREATE,
                    TRUNCATE_EXISTING,
                    WRITE,
                ).use { fileOutput ->
                    DigestOutputStream(fileOutput, digest).use { digestOutput ->
                        response.body.use { responseBody ->
                            responseBody.transferTo(digestOutput)
                        }
                    }
                }

                val expectedSizeBytes = response.headers.contentLength

                check(
                    expectedSizeBytes < 0 ||
                        expectedSizeBytes == sizeBytes,
                ) {
                    "Incomplete EIA download for " +
                        "${manifestEntry.datasetId.value}: " +
                        "expected $expectedSizeBytes bytes, " +
                        "received $sizeBytes"
                }

                EiaBulkDataDownloadResult.Downloaded(
                    sizeBytes = sizeBytes,
                    sha256 = HexFormat.of()
                        .formatHex(digest.digest()),
                )
            }
    }

    private fun requireTrustedAccessUrl(
        accessUrl: URI,
    ) {
        require(accessUrl.scheme == "https") {
            "EIA access URL must use HTTPS: $accessUrl"
        }

        require(
            accessUrl.host.equals(
                "www.eia.gov",
                ignoreCase = true,
            ),
        ) {
            "Untrusted EIA access URL host: $accessUrl"
        }

        require(
            accessUrl.port == -1 ||
                accessUrl.port == 443,
        ) {
            "Unexpected EIA access URL port: $accessUrl"
        }

        require(accessUrl.userInfo == null) {
            "EIA access URL must not contain user information: $accessUrl"
        }

        require(accessUrl.rawQuery == null) {
            "EIA access URL must not contain a query: $accessUrl"
        }

        require(accessUrl.rawFragment == null) {
            "EIA access URL must not contain a fragment: $accessUrl"
        }

        require(
            accessUrl.path.startsWith("/opendata/bulk/") &&
                accessUrl.path.endsWith(".zip"),
        ) {
            "Unexpected EIA bulk-data path: $accessUrl"
        }
    }

    private companion object {
        const val EIA_BULK_DATA_BASE_URL =
            "https://www.eia.gov/opendata/bulk/"
    }
}

sealed interface EiaBulkDataDownloadResult {
    data class Downloaded(
        val sizeBytes: Long,
        val sha256: String,
    ) : EiaBulkDataDownloadResult

    data object NotFound : EiaBulkDataDownloadResult
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class EiaBulkDataManifestDto @JsonCreator constructor(
    @param:JsonProperty("dataset")
    val entries: Map<String, EiaBulkDataManifestEntryDto>,
) {
    fun toDomain(): EiaBulkDataManifest =
        EiaBulkDataManifest(
            entries = buildMap {
                for (
                    (rawDatasetId, entryDto) in
                    this@EiaBulkDataManifestDto.entries
                ) {
                    val datasetId =
                        EiaDatasetId(rawDatasetId)

                    require(
                        entryDto.datasetId ==
                            datasetId.value,
                    ) {
                        "Manifest key ${datasetId.value} " +
                            "does not match data_set " +
                            entryDto.datasetId
                    }

                    require(
                        entryDto.identifier ==
                            datasetId.value,
                    ) {
                        "Manifest key ${datasetId.value} " +
                            "does not match identifier " +
                            entryDto.identifier
                    }

                    put(
                        datasetId,
                        EiaBulkDataManifestEntry(
                            datasetId = datasetId,
                            lastUpdated = Instant.parse(
                                entryDto.lastUpdated,
                            ),
                            accessUrl = entryDto.accessUrl,
                        ),
                    )
                }
            },
        )
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class EiaBulkDataManifestEntryDto @JsonCreator constructor(
    @param:JsonProperty("data_set")
    val datasetId: String,

    @param:JsonProperty("identifier")
    val identifier: String,

    @param:JsonProperty("last_updated")
    val lastUpdated: String,

    @param:JsonProperty("accessURL")
    val accessUrl: URI,
)
