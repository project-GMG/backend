package eusyaeusya.gmg.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI baseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GMG API Documentation")
                        .version("v1.0.0")
                        .description("가면가 API 문서입니다.")
                )
                .addServersItem(new Server().url("/")); // 리버스 프록시 쓰면 수정 가능
    }

    @Bean
    public OpenApiCustomizer globalResponseCustomizer() {
        return openApi -> openApi.getPaths()
                .values()
                .forEach(path -> path.readOperations().forEach(operation -> {
                    operation.getResponses().addApiResponse("400",
                            new ApiResponse().description("잘못된 요청"));
                    operation.getResponses().addApiResponse("401",
                            new ApiResponse().description("인증 필요"));
                    operation.getResponses().addApiResponse("500",
                            new ApiResponse().description("서버 내부 오류"));
                }));
    }
}
