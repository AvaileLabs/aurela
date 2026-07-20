package com.availelabs.aurela.monitoring

import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.WebUtils

class AdminCsrfCookieFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val csrf = request.getAttribute(CsrfToken::class.java.name) as CsrfToken?
        if (csrf != null) {
            val currentCookie = WebUtils.getCookie(request, CSRF_COOKIE_NAME)
            val token = csrf.token
            if (currentCookie == null || token != currentCookie.value) {
                response.addCookie(Cookie(CSRF_COOKIE_NAME, token).apply { path = "/" })
            }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        private const val CSRF_COOKIE_NAME = "XSRF-TOKEN"
    }
}