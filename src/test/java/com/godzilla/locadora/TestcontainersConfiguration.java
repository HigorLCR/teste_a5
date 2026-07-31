package com.godzilla.locadora;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		// Mesma imagem do compose.yaml: o teste roda contra a versao exata que a
		// aplicacao usa. Com 'latest', teste e producao poderiam divergir.
		return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
	}

}
