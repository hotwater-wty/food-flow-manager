package com.foodflow.testsupport;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestContainers {

    /*
     * Spring caches an application context across test classes. Keep one container
     * pair alive for the whole Maven JVM so the cached DataSource never points at
     * a container that a previous test class has already stopped.
     */
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("food_flow_manager")
            .withUsername("root")
            .withPassword("1234")
            .withInitScript("schema.sql");

    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8.6.2")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "1234");

    static {
        MYSQL.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
        registry.add("spring.data.redis.password", () -> "1234");
    }
}
