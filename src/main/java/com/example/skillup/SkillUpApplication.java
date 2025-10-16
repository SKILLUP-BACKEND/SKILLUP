package com.example.skillup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SkillUpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillUpApplication.class, args);
    }

}
