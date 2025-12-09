package org.tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.tasks.starter.config.EnableEventAudit;

@SpringBootApplication
@EnableWebMvc
@EnableEventAudit
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
