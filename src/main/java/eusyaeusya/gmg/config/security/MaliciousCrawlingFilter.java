package eusyaeusya.gmg.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
public class MaliciousCrawlingFilter extends OncePerRequestFilter {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            ".php", ".env", ".asp", ".aspx", ".cgi", ".jsp",
            ".bak", ".sql", ".tar", ".gz", ".zip", ".rar",
            ".config", ".ini", ".log", ".yml", ".yaml");

    private static final List<String> BLOCKED_PATH_PATTERNS = List.of(
            "/wp-admin", "/wp-content", "/wp-includes", "/wp-login",
            "/wordpress", "/wp-json",
            "/backup", "/phpmyadmin", "/pma", "/myadmin",
            "/.git", "/.svn", "/.hg", "/.env",
            "/.vscode", "/.idea",
            "/vendor/", "/node_modules/",
            "/xmlrpc", "/cgi-bin",
            "/docker-compose", "/Dockerfile");

    private static final List<String> BLOCKED_USER_AGENTS = List.of(
            "sqlmap", "nikto", "dirbuster", "masscan", "nmap",
            "zgrab", "gobuster", "wfuzz", "ffuf", "nuclei",
            "httpx", "census", "scaninfo", "expanse");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI().toLowerCase();
        String userAgent = request.getHeader("User-Agent");

        if (isSuspiciousUri(uri) || isSuspiciousUserAgent(userAgent)) {
            logBlocked(request, uri, userAgent);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSuspiciousUri(String uri) {
        // 확장자 검사
        for (String ext : BLOCKED_EXTENSIONS) {
            if (uri.endsWith(ext)) {
                return true;
            }
        }

        // 경로 패턴 검사
        for (String pattern : BLOCKED_PATH_PATTERNS) {
            if (uri.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSuspiciousUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return false;
        }
        String lowerUA = userAgent.toLowerCase();
        for (String scanner : BLOCKED_USER_AGENTS) {
            if (lowerUA.contains(scanner)) {
                return true;
            }
        }
        return false;
    }

    private void logBlocked(HttpServletRequest request, String uri, String userAgent) {
        String clientIp = resolveClientIp(request);
        log.warn("[BLOCKED] ip={}, method={}, uri={}, ua={}",
                clientIp, request.getMethod(), uri,
                userAgent != null ? userAgent : "N/A");
    }

    private String resolveClientIp(HttpServletRequest request) {
        // Cloudflare IP 헤더 최우선 확인
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp.trim();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
