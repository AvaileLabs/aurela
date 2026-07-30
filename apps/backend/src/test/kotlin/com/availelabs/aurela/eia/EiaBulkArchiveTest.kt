package com.availelabs.aurela.eia

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EiaBulkArchiveTest {
    private val bulkArchive = EiaBulkArchive()

    @TempDir
    lateinit var tempDirectory: Path

    @Test
    fun `validates the single same-named data entry`() {
        val archivePath = createStoredArchive(
            entryName = "AEO2014.txt",
            contents = "{}\n".toByteArray(),
        )

        bulkArchive.validate(
            archivePath,
            URI.create(
                "https://www.eia.gov/opendata/bulk/AEO2014.zip",
            ),
        )
    }

    @Test
    fun `rejects an archive with the wrong data entry name`() {
        val archivePath = createStoredArchive(
            entryName = "different.txt",
            contents = "{}\n".toByteArray(),
        )

        assertFailsWith<IllegalArgumentException> {
            bulkArchive.validate(
                archivePath,
                URI.create(
                    "https://www.eia.gov/opendata/bulk/AEO2014.zip",
                ),
            )
        }
    }

    @Test
    fun `rejects an archive whose data fails CRC validation`() {
        val contents = "unique-petroleum-record".toByteArray()
        val archivePath = createStoredArchive(
            entryName = "PET.txt",
            contents = contents,
        )
        val archiveBytes = Files.readAllBytes(archivePath)
        val contentsIndex =
            archiveBytes.indexOf(contents)

        assertTrue(contentsIndex >= 0)

        archiveBytes[contentsIndex] =
            (archiveBytes[contentsIndex].toInt() xor 1).toByte()

        Files.write(archivePath, archiveBytes)

        assertFailsWith<IllegalArgumentException> {
            bulkArchive.validate(
                archivePath,
                URI.create(
                    "https://www.eia.gov/opendata/bulk/PET.zip",
                ),
            )
        }
    }

    private fun createStoredArchive(
        entryName: String,
        contents: ByteArray,
    ): Path {
        val archivePath =
            tempDirectory.resolve("$entryName.zip")

        val crc = CRC32().apply {
            update(contents)
        }

        ZipOutputStream(
            Files.newOutputStream(archivePath),
        ).use { zipOutput ->
            val entry = ZipEntry(entryName).apply {
                method = ZipEntry.STORED
                size = contents.size.toLong()
                compressedSize = contents.size.toLong()
                this.crc = crc.value
            }

            zipOutput.putNextEntry(entry)
            zipOutput.write(contents)
            zipOutput.closeEntry()
        }

        return archivePath
    }

    private fun ByteArray.indexOf(
        target: ByteArray,
    ): Int {
        if (target.isEmpty()) {
            return 0
        }

        for (startIndex in 0..size - target.size) {
            var matches = true

            for (targetIndex in target.indices) {
                if (this[startIndex + targetIndex] !=
                    target[targetIndex]
                ) {
                    matches = false
                    break
                }
            }

            if (matches) {
                return startIndex
            }
        }

        return -1
    }
}
