package eusyaeusya.gmg.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.function.Supplier;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${management-config.allowed-ip}")
    private String allowedIp;

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(actuatorBasePath + "/**")
                        .access(this::hasIpAddress)
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    private AuthorizationDecision hasIpAddress(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context) {
        String remoteAddress = context.getRequest().getRemoteAddr();

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
}
