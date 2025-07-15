package com.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.auth.repository")
@EnableCassandraRepositories(basePackages = "com.auth.repository.cassandra")
public class AuthServiceApplication {
    public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}
