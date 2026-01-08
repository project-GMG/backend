package eusyaeusya.gmg.infra.kakao.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakao.map.api")
public class KakaoMapProperties {
    private String baseUrl;
    private String key;
    private String categoryGroupCode;
}
