package com.availelabs.aurela.eia

import org.springframework.stereotype.Component
import java.net.URI
import java.nio.file.Path
import java.util.zip.CRC32
import java.util.zip.ZipFile

@Component
class EiaBulkArchive {
    fun validate(
        archivePath: Path,
        accessUrl: URI,
    ) {
        ZipFile(archivePath.toFile()).use { zipFile ->
            val entries = zipFile.entries()
                .asSequence()
                .toList()

            require(entries.size == 1) {
                "Expected one entry in the EIA archive, " +
                    "but found ${entries.size}"
            }

            val dataEntry = entries.single()

            require(!dataEntry.isDirectory) {
                "Expected an EIA archive data file, but found a directory"
            }

            val expectedEntryName =
                expectedEntryName(accessUrl)

            require(dataEntry.name == expectedEntryName) {
                "Expected EIA archive entry $expectedEntryName, " +
                    "but found ${dataEntry.name}"
            }

            val crc = CRC32()
            var sizeBytes = 0L
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

            zipFile.getInputStream(dataEntry).use { input ->
                while (true) {
                    val bytesRead = input.read(buffer)

                    if (bytesRead < 0) {
                        break
                    }

                    crc.update(buffer, 0, bytesRead)
                    sizeBytes += bytesRead
                }
            }

            require(dataEntry.size == sizeBytes) {
                "EIA archive entry ${dataEntry.name} has an invalid size"
            }

            require(dataEntry.crc == crc.value) {
                "EIA archive entry ${dataEntry.name} failed CRC validation"
            }
        }
    }

    private fun expectedEntryName(
        accessUrl: URI,
    ): String {
        val archiveFileName = Path.of(accessUrl.path)
            .fileName
            .toString()

        require(archiveFileName.endsWith(".zip")) {
            "Expected the EIA access URL to reference a ZIP file: $accessUrl"
        }

        return archiveFileName.removeSuffix(".zip") +
            ".txt"
    }
}
