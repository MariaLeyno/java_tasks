package org.tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@ComponentScan
@EnableWebMvc
public class MarketplaceApplication {
    public static void main(String[] args) {
        System.out.println("Starting MarketplaceApplication...");
        SpringApplication.run(MarketplaceApplication.class);
        System.out.println("MarketplaceApplication works!");
    }
}
