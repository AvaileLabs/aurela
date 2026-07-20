package com.availelabs.aurela.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Defines authentication and authorization rules for HTTP requests.
 */
@Configuration
class SecurityConfiguration {
    /**
     * Requires authentication for every request and enables OAuth 2.0 login.
     *
     * @param http the Spring Security HTTP configuration to customize
     * @return the configured security filter chain
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http.authorizeHttpRequests { it.anyRequest().authenticated() }
            .oauth2Login(Customizer.withDefaults())
            .build()
}