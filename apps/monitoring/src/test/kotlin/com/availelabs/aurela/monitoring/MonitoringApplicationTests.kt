package com.availelabs.aurela.monitoring

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@SpringBootTest
class MonitoringApplicationTests(@Autowired private val mockMvc: MockMvc) {
    @Test
    fun `login page is publicly available`() {
        mockMvc.get("/login").andExpect { status { isOk() } }
    }

    @Test
    fun `dashboard requires authentication`() {
        mockMvc.get("/").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `health endpoint is publicly available`() {
        mockMvc.get("/actuator/health").andExpect { status { isOk() } }
    }

    @Test
    fun `authenticated clients can register without a csrf token`() {
        val registration = mockMvc.post("/instances") {
            with(httpBasic("admin", "admin"))
            contentType = MediaType.APPLICATION_JSON
            content =
                """{"name":"test-service","managementUrl":"http://localhost:1/actuator","healthUrl":"http://localhost:1/actuator/health","serviceUrl":"http://localhost:1"}"""
        }.andExpect { request { asyncStarted() } }.andReturn()

        mockMvc.perform(asyncDispatch(registration)).andExpect(status().isCreated)
    }
}