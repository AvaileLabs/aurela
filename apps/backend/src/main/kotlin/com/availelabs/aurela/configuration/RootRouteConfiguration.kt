package com.availelabs.aurela.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Configures convenience routes for browser clients.
 *
 * The application root redirects to the Scalar API reference at `/scalar`.
 */
@Configuration
class RootRouteConfiguration : WebMvcConfigurer {
    /**
     * Registers the application-root redirect.
     *
     * @param registry registry receiving the redirect view controller
     */
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "/scalar")
    }
}