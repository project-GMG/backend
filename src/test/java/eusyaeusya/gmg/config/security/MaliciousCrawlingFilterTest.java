package eusyaeusya.gmg.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MaliciousCrawlingFilterTest {

    private MaliciousCrawlingFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new MaliciousCrawlingFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/backup/phpinfo.php",
            "/api/phpinfo.php",
            "/some/path/info.php",
            "/web/api.php"
    })
    @DisplayName("PHP 확장자 요청은 404로 차단한다")
    void shouldBlock_phpExtension(String uri) throws ServletException, IOException {
        request.setRequestURI(uri);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/.env",
            "/vue-end/vue-cli/.env",
            "/vue_CRM/.env",
            "/vod_installer/.env",
            "/videos/.env"
    })
    @DisplayName(".env 파일 요청은 404로 차단한다")
    void shouldBlock_envFiles(String uri) throws ServletException, IOException {
        request.setRequestURI(uri);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/wp-admin/login",
            "/wp-content/uploads/shell.php",
            "/backup/db.sql",
            "/.git/config",
            "/.vscode/ftp-sync.json",
            "/var-docker-compose.yml",
            "/phpmyadmin/index.php"
    })
    @DisplayName("의심스러운 경로 패턴은 404로 차단한다")
    void shouldBlock_suspiciousPaths(String uri) throws ServletException, IOException {
        request.setRequestURI(uri);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "sqlmap/1.5#stable",
            "nikto/2.1.6",
            "Mozilla/5.0 (compatible; Nmap Scripting Engine)",
            "DirBuster-1.0-RC1",
            "gobuster/3.1.0",
            "nuclei - (projectdiscovery.io)"
    })
    @DisplayName("스캐너 User-Agent 요청은 404로 차단한다")
    void shouldBlock_scannerUserAgents(String userAgent) throws ServletException, IOException {
        request.setRequestURI("/api/event");
        request.addHeader("User-Agent", userAgent);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/event/abc123",
            "/api/event/abc123/participants",
            "/api/places/search",
            "/actuator/health",
            "/swagger-ui/index.html",
            "/v3/api-docs"
    })
    @DisplayName("정상 API 경로는 필터를 통과한다")
    void shouldAllow_normalApiPaths(String uri) throws ServletException, IOException {
        request.setRequestURI(uri);
        request.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("User-Agent가 없는 정상 경로 요청은 필터를 통과한다")
    void shouldAllow_noUserAgent() throws ServletException, IOException {
        request.setRequestURI("/api/event");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("차단 시 X-Forwarded-For 헤더에서 클라이언트 IP를 추출한다")
    void shouldResolveIp_fromXForwardedFor() throws ServletException, IOException {
        request.setRequestURI("/backup/phpinfo.php");
        request.addHeader("X-Forwarded-For", "1.2.3.4, 10.0.0.1");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        verifyNoInteractions(filterChain);
    }
}
