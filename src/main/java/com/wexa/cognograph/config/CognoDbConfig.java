package com.wexa.cognograph.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CognoDbConfig {
    @Bean
    public Driver driver(
            @Value("${cognodb.uri}") String uri,
            @Value("${cognodb.username}") String username,
            @Value("${cognodb.password}") String password) {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }
}
