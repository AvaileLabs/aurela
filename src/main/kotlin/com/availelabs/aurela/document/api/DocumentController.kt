package com.availelabs.aurela.document.api

import com.availelabs.aurela.document.application.DocumentService
import com.availelabs.aurela.document.domain.PdfDocument
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@RestController
@RequestMapping("/api/documents")
@Tag(
    name = "Documents",
    description = "Upload and manage documents",
)
class DocumentController(private val documentService: DocumentService) {

    @OptIn(ExperimentalUuidApi::class)
    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    @Operation(
        summary = "Upload PDF documents",
        description = "Uploads one or more PDF documents.",
    )
    fun uploadPdfs(
        @RequestParam("files")
        files: List<MultipartFile>,
    ): ResponseEntity<Void> {
        val documents = files.map {
            PdfDocument(
                id = Uuid.generateV7(),
                uploadedAt = Clock.System.now(),
            )
        }

        documentService.uploadDocuments(documents)

        return ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .build()
    }
}