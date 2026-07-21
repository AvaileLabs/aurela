package com.availelabs.aurela.eia

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class EiaBulkDataClient(
    restClientBuilder: RestClient.Builder,
) {
    private val restClient = restClientBuilder
        .baseUrl("https://www.eia.gov/opendata/bulk")
        .build()

    fun downloadDatasetOrNull(datasetId: String): ByteArray? {
        val request = restClient.get()
            .uri("/{datasetId}.zip", datasetId)
        val downloadedFile = request.exchange { _, response ->
            val status = response.statusCode

            if (status == HttpStatus.NOT_FOUND) {
                return@exchange null
            }

            if (!status.is2xxSuccessful) {
                throw response.createException()
            }

            val responseBody = response.bodyTo(ByteArray::class.java)
                ?: throw IllegalStateException("EIA returned no response body for dataset: $datasetId")

            responseBody
        }

        return downloadedFile
    }
}
