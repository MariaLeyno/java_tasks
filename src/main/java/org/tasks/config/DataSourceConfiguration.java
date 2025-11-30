package org.tasks.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {
    //@Value("${spring.datasource.url}")
    private String url = "jdbc:postgresql://localhost:15432/market";

    //@Value("${spring.datasource.username}")
    private String username = "market_admin";

    //@Value("${spring.datasource.password}")
    private String password = "marketplace1579";

    //@Value("${spring.datasource.driver-class-name}")
    private String driver = "org.postgresql.Driver";

    //@Value("${spring.liquibase.default-schema}")
    private String defaultSchema = "marketplace";

    @Bean
    public DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setSchema(defaultSchema);
        return dataSource;
    }
}
