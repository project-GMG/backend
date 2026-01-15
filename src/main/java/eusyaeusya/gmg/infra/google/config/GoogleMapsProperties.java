package eusyaeusya.gmg.infra.google.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google.maps.api")
public class GoogleMapsProperties {
    private String baseUrl;
    private String key;

    public boolean hasKey() {
        return key != null && !key.isBlank();
    }
}
