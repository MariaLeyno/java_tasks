package org.tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class MarketplaceApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        System.out.println("Starting MarketplaceApplication...");
        SpringApplication.run(MarketplaceApplication.class);
        System.out.println("MarketplaceApplication works!");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MarketplaceApplication.class);
    }
}
