package com.availelabs.aurela.document.domain

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class PdfDocument(
    val id: Uuid,
    val uploadedAt: Instant
)