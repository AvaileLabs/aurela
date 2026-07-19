package com.availelabs.aurela.eia

import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import java.nio.file.Path
import kotlin.io.path.useLines

@Component
class EiaPetroleumBulkReader(private val jsonMapper: JsonMapper) {
    fun forEachRecord(
        path: Path,
        action: (EiaPetroleumRecord) -> Unit
    ) {
        path.useLines { jsonLines ->
            jsonLines.forEachIndexed { index, jsonLine ->
                if (jsonLine.isBlank())
                    return@forEachIndexed

                val jsonObject = jsonMapper.readTree(jsonLine)

                require(jsonObject is ObjectNode) {
                    "Expected a JSON object on line ${index + 1}, but found ${jsonObject.nodeType}"
                }

                val petroleumRecord = when {
                    jsonObject.has("series_id") -> EiaPetroleumRecord.Series(jsonObject)
                    jsonObject.has("category_id") -> EiaPetroleumRecord.Category(jsonObject)
                    else -> EiaPetroleumRecord.Unrecognized(jsonObject)
                }

                action(petroleumRecord)
            }
        }
    }
}