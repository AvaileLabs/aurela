package com.availelabs.aurela

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AurelaApplication

fun main(args: Array<String>) {
    runApplication<AurelaApplication>(*args)
}
