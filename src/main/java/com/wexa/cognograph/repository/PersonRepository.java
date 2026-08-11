package com.wexa.cognograph.repository;

import java.util.List;
import java.util.Map;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepository {
    private final Driver driver;

    public PersonRepository(Driver driver) { this.driver = driver; }

    public Map<String,Object> findPerson(long id) {
        try (var session = driver.session()) {

            var result = session.run(
                "MATCH (p:Person {id: $id}) " +
                "RETURN p.id AS id, p.name AS name, p.age AS age",
                Values.parameters("id", id));

            var records = result.list();

            return records.isEmpty() ? null : records.get(0).asMap();
        }
    }

    public List<Map<String,Object>> directFriends(long id) {
        try (var session = driver.session()) {
            return session.run(
                "MATCH (p:Person {id: $id})-[:FRIEND]->(f:Person) " +
                "RETURN f.id AS id, f.name AS name, f.age AS age ORDER BY f.id LIMIT 50",
                Values.parameters("id", id)).list(Record::asMap);
        }
    }

    public List<Map<String,Object>> friendsOfFriends(long id) {
        try (var session = driver.session()) {
            return session.run(
                "MATCH (p:Person {id: $id})-[:FRIEND]->()-[:FRIEND]->(f:Person) " +
                "WHERE f.id <> $id " +
                "RETURN DISTINCT f.id AS id, f.name AS name, f.age AS age " +
                "ORDER BY f.id LIMIT 50",
                Values.parameters("id", id)).list(Record::asMap);
        }
    }

    public List<Map<String,Object>> network(long id) {
        try (var session = driver.session()) {
            return session.run(
                "MATCH (p:Person {id: $id})-[:FRIEND*1..3]->(f:Person) " +
                "WHERE f.id <> $id " +
                "RETURN DISTINCT f.id AS id, f.name AS name, f.age AS age " +
                "ORDER BY f.id LIMIT 100",
                Values.parameters("id", id)).list(Record::asMap);
        }
    }
}
