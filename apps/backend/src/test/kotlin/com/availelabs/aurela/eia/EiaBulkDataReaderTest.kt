package com.availelabs.aurela.eia

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import tools.jackson.databind.json.JsonMapper
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EiaPetroleumBulkReaderTest {
    private val jsonMapper = JsonMapper.builder().build()
    private val reader = EiaPetroleumBulkReader(jsonMapper)

    @TempDir
    lateinit var tempDirectory: Path

    @Test
    fun `classifies petroleum record types`() {
        val archivePath = tempDirectory.resolve("PET.zip")

        val records = listOf(
            jsonMapper.createObjectNode()
                .put("series_id", "TEST")
                .put("name", "Test series"),

            jsonMapper.createObjectNode()
                .put("category_id", "12345")
                .put("name", "Test category"),

            jsonMapper.createObjectNode()
                .put("name", "Unknown record"),
        )

        ZipOutputStream(
            Files.newOutputStream(archivePath),
        ).use { zipOutput ->
            zipOutput.putNextEntry(ZipEntry("PET.txt"))

            jsonMapper.writer()
                .withRootValueSeparator("\n")
                .writeValues(zipOutput)
                .use { writer ->
                    writer.writeAll(records)
                }
            }

        val actualRecords = mutableListOf<EiaPetroleumRecord>()

        reader.forEachRecord(
            archivePath,
            actualRecords::add,
        )

        assertEquals(3, actualRecords.size)
        assertInstanceOf(
            EiaPetroleumRecord.Series::class.java,
            actualRecords[0],
        )
        assertInstanceOf(
            EiaPetroleumRecord.Category::class.java,
            actualRecords[1],
        )
        assertInstanceOf(
            EiaPetroleumRecord.Unrecognized::class.java,
            actualRecords[2],
        )
    }
}
