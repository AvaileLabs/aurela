package com.availelabs.aurela

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Spring Boot configuration entry point for the Aurela backend.
 */
@SpringBootApplication
class AurelaApplication

/**
 * Starts the Aurela backend application.
 *
 * @param args command-line arguments forwarded to Spring Boot
 */
fun main(args: Array<String>) {
    runApplication<AurelaApplication>(*args)
}
