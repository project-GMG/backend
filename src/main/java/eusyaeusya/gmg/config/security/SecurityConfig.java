package eusyaeusya.gmg.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.function.Supplier;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${management-config.allowed-ip}")
    private String allowedIp;

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    @Bean
    @Profile("local")
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/**");
    }

    @Bean
    @Profile("!local")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(actuatorBasePath + "/health/**").permitAll()
                        .requestMatchers(actuatorBasePath + "/**")
                        .access(this::hasIpAddress)
                        .anyRequest().permitAll()
                );
        http
                .headers(headers -> headers
                        .contentTypeOptions(withDefaults())
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .xssProtection(HeadersConfigurer.XXssConfig::disable)
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'self'"))
                );

        return http.build();
    }

    private AuthorizationDecision hasIpAddress(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context) {

        String remoteAddress = resolveClientIp(context.getRequest());

        if (allowedIp == null || allowedIp.isBlank()) {
            return new AuthorizationDecision(false);
        }
        // 다중 IP 지원: 쉼표로 구분
        String[] allowedIps = allowedIp.split(",");
        boolean allowed = false;

        for (String ip : allowedIps) {
            if (new IpAddressMatcher(ip.trim()).matches(remoteAddress)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            log.warn("Actuator access denied: IP={}, Allowed={}", remoteAddress, allowedIp);
        } else {
            log.debug("Actuator access granted: IP={}", remoteAddress);
        }

        return new AuthorizationDecision(allowed);
    }

    private String resolveClientIp(HttpServletRequest request) {
        // 프록시 환경: X-Forwarded-For 헤더 확인
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // 첫 번째 IP가 실제 클라이언트 (체인: client, proxy1, proxy2)
            String clientIp = xForwardedFor.split(",")[0].trim();
            log.debug("X-Forwarded-For detected: header={}, resolved={}", xForwardedFor, clientIp);
            return clientIp;
        }

        // 직접 연결: remoteAddr 사용
        return request.getRemoteAddr();
    }
}
