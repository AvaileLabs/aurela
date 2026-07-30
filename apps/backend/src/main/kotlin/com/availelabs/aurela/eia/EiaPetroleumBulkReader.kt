package com.availelabs.aurela.eia

import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.ZipFile

@Component
class EiaPetroleumBulkReader(private val jsonMapper: JsonMapper) {
    fun forEachRecord(
        archivePath: Path,
        action: (EiaPetroleumRecord) -> Unit,
    ) {
        ZipFile(archivePath.toFile()).use { zipFile ->
            val dataEntry = zipFile.entries()
                .asSequence()
                .filterNot { it.isDirectory }
                .single()

            require(dataEntry.name.endsWith(".txt")) {
                "Expected the EIA archive data entry to be a text file, " +
                    "but found ${dataEntry.name}"
            }

            zipFile.getInputStream(dataEntry)
                .bufferedReader(StandardCharsets.UTF_8)
                .useLines { jsonLines ->
                    jsonLines.forEachIndexed { index, jsonLine ->
                        if (jsonLine.isBlank()) {
                            return@forEachIndexed
                        }

                        val jsonObject =
                            jsonMapper.readTree(jsonLine)

                        require(jsonObject is ObjectNode) {
                            "Expected a JSON object on line ${index + 1}, " +
                                "but found ${jsonObject.nodeType}"
                        }

                        val petroleumRecord = when {
                            jsonObject.has("series_id") ->
                                EiaPetroleumRecord.Series(jsonObject)

                            jsonObject.has("category_id") ->
                                EiaPetroleumRecord.Category(jsonObject)

                            else ->
                                EiaPetroleumRecord.Unrecognized(jsonObject)
                        }

                        action(petroleumRecord)
                    }
                }
        }
    }
}
