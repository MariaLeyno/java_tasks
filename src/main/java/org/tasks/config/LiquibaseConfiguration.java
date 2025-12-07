package org.tasks.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfiguration {

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    @Value("${spring.liquibase.default-schema}")
    private String defaultSchema;

    @Value("${spring.liquibase.liquibase-schema}")
    private String liquibaseSchema;

    @Bean
    @DependsOn("databaseInitializer")
    public SpringLiquibase getLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setLiquibaseSchema(liquibaseSchema);
        liquibase.setDefaultSchema(defaultSchema);
        liquibase.setChangeLog(changeLog);
        return liquibase;
    }

    @Bean("databaseInitializer")
    public DatabaseInitializer databaseInitializer(DataSource dataSource) {
        return new DatabaseInitializer(dataSource, defaultSchema, liquibaseSchema);
    }
}
