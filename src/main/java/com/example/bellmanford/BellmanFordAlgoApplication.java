package com.example.bellmanford;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.n52.jackson.datatype.jts.JtsModule;

@SpringBootApplication
public class BellmanFordAlgoApplication {
    public static void main(String[] args) {
        SpringApplication.run(BellmanFordAlgoApplication.class, args);
    }

    @Bean
    public JtsModule jtsModule() {
        return new JtsModule();
    }
}
