package com.availelabs.aurela

import kotlin.test.Test
import kotlin.test.assertFalse

class AurelaApplicationArchitectureTest {
    @Test
    fun `backend does not enable the admin server`() {
        val annotationNames = AurelaApplication::class.java.annotations.map { it.annotationClass.qualifiedName }

        assertFalse("de.codecentric.boot.admin.server.config.EnableAdminServer" in annotationNames)
    }
}