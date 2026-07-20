package com.availelabs.aurela.security

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfiguration {
    @Bean
    @Order(1)
    fun actuatorSecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .httpBasic(Customizer.withDefaults())
            .csrf { it.disable() }
            .build()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http.authorizeHttpRequests { it.anyRequest().authenticated() }
            .oauth2Login(Customizer.withDefaults())
            .build()
}