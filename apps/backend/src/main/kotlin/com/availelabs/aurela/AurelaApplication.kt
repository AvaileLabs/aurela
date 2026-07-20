package com.availelabs.aurela

import de.codecentric.boot.admin.server.config.EnableAdminServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableAdminServer
@SpringBootApplication
class AurelaApplication

fun main(args: Array<String>) {
    runApplication<AurelaApplication>(*args)
}
