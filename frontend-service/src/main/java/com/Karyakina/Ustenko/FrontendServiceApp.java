package com.Karyakina.Ustenko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;

@SpringBootApplication(exclude = {
        ReactiveSecurityAutoConfiguration.class
})
public class FrontendServiceApp {
    public static void main(String[] args) {

        SpringApplication.run(FrontendServiceApp.class, args);
    }
}
