plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.ksp)
}

group = "com.availelabs"
version = "0.0.1-SNAPSHOT"
description = "aurela"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter)
    implementation(libs.kotlin.reflect)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.launcher)

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.springdoc.openapi.starter.webmvc.scalar)
    implementation(libs.jimmer.spring.boot.starter)
    implementation(platform(libs.spring.modulith.bom))
    implementation(libs.spring.modulith.starter.core)
    testImplementation(libs.spring.modulith.starter.test)
    ksp(libs.jimmer.ksp)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.spring.boot.docker.compose)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
