package com.availelabs.aurela.eia

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path

@ConfigurationProperties("aurela.eia.bulk-data")
data class EiaBulkDataProperties(
    val directory: Path = Path.of(
        System.getProperty("user.home"),
        "Aurela",
        "workspace",
        "eia",
        "bulk-data",
    ),
)
