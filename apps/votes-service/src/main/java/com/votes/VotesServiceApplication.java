package com.votes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.votes.repository.postgres")
@EnableCassandraRepositories(basePackages = "com.votes.repository.cassandra")
public class VotesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VotesServiceApplication.class, args);
	}

}
