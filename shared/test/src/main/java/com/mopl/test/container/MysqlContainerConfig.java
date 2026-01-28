package com.mopl.test.container;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class MysqlContainerConfig {

    private static final String MYSQL_IMAGE = "mysql:8.0";
    private static final String DATABASE_NAME = "mopl_test";

    @Bean(destroyMethod = "stop")
    @ServiceConnection
    @SuppressWarnings("resource") // Spring 컨테이너가 생명주기 관리
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
            .withDatabaseName(DATABASE_NAME)
            .withReuse(true);
    }
}
