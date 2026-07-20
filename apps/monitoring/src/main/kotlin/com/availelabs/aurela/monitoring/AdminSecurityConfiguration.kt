package com.availelabs.aurela.monitoring

import de.codecentric.boot.admin.server.config.AdminServerProperties
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import java.util.*

@Configuration(proxyBeanMethods = false)
class AdminSecurityConfiguration(private val adminServer: AdminServerProperties) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val requestMatcher = PathPatternRequestMatcher.withDefaults()
        val successHandler = SavedRequestAwareAuthenticationSuccessHandler().apply {
            setTargetUrlParameter("redirectTo")
            setDefaultTargetUrl(adminServer.path("/"))
        }

        http.authorizeHttpRequests {
            it.requestMatchers(requestMatcher.matcher(adminServer.path("/assets/**"))).permitAll()
                .requestMatchers(requestMatcher.matcher(adminServer.path("/actuator/info"))).permitAll()
                .requestMatchers(requestMatcher.matcher(adminServer.path("/actuator/health"))).permitAll()
                .requestMatchers(requestMatcher.matcher(adminServer.path("/login"))).permitAll()
                .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                .anyRequest().authenticated()
        }
            .formLogin { it.loginPage(adminServer.path("/login")).successHandler(successHandler) }
            .logout { it.logoutUrl(adminServer.path("/logout")) }
            .httpBasic(Customizer.withDefaults())
            .addFilterAfter(AdminCsrfCookieFilter(), BasicAuthenticationFilter::class.java)
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(CsrfTokenRequestAttributeHandler())
                    .ignoringRequestMatchers(
                        requestMatcher.matcher(org.springframework.http.HttpMethod.POST, adminServer.path("/instances")),
                        requestMatcher.matcher(org.springframework.http.HttpMethod.DELETE, adminServer.path("/instances/*")),
                        requestMatcher.matcher(adminServer.path("/actuator/**")),
                    )
            }
            .rememberMe { it.key(UUID.randomUUID().toString()).tokenValiditySeconds(1_209_600) }

        return http.build()
    }
}