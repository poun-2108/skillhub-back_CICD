package com.example.auth.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CorsConfigTest {

    @Test
    void doFilter_setHeadersAndContinue_forAllowedOrigin() throws Exception {
        CorsConfig filter = new CorsConfig();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Origin", "http://localhost:5173");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals("http://localhost:5173", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
        assertEquals("Authorization, Content-Type, Accept", response.getHeader("Access-Control-Allow-Headers"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_doesNotSetAllowOrigin_forUnknownOrigin() throws Exception {
        CorsConfig filter = new CorsConfig();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Origin", "https://evil.example");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(response.getHeader("Access-Control-Allow-Origin"));
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_returns200AndSkipsChain_forOptions() throws Exception {
        CorsConfig filter = new CorsConfig();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/me");
        request.addHeader("Origin", "http://127.0.0.1:3000");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertEquals("http://127.0.0.1:3000", response.getHeader("Access-Control-Allow-Origin"));
        verify(chain, times(0)).doFilter(request, response);
    }

    @Test
    void corsFilterBean_returnsInstance() {
        CorsConfig filter = new CorsConfig();
        CorsConfig bean = filter.corsFilter();

        assertNotNull(bean);
    }
}
