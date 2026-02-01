package io.github.avstarodub.mosd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class MosdApplication {

    public static void main(String[] args) {
        SpringApplication.run(MosdApplication.class, args);
    }
}
