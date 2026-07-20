package com.availelabs.aurela.eia

import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import java.nio.file.Path
import kotlin.io.path.useLines

/**
 * Streams newline-delimited EIA petroleum JSON records from disk.
 *
 * Records are classified by their identifying field without loading the complete file into
 * memory. Blank lines are ignored.
 *
 * @property jsonMapper mapper used to parse each JSON object
 */
@Component
class EiaPetroleumBulkReader(private val jsonMapper: JsonMapper) {
    /**
     * Parses each record in [path] and passes it to [action] in file order.
     *
     * Parsing, file-access, and callback exceptions are propagated to the caller.
     *
     * @param path newline-delimited JSON file to read
     * @param action callback invoked once for every non-blank record
     * @throws IllegalArgumentException if a non-blank line does not contain a JSON object
     */
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