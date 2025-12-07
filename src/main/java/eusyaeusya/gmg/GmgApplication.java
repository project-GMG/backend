package eusyaeusya.gmg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GmgApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmgApplication.class, args);
    }

}
