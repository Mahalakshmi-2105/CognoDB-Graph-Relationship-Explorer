package com.wexa.cognograph.seed;

import java.io.*;
import java.util.*;
import org.neo4j.driver.*;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

public class SeedData {
    public static void main(String[] args) throws Exception {
        String uri = require("COGNODB_URI");
        String username = System.getenv().getOrDefault("COGNODB_USERNAME", "cognodb");
        String password = require("COGNODB_PASSWORD");
        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
             Session session = driver.session()) {
            System.out.println("Creating Person nodes...");
            loadPersons(session);
            System.out.println("Creating FRIEND relationships...");
            loadRelationships(session);
            System.out.println("Seed completed.");
        }
    }

    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is not set");
        return value;
    }

    private static void loadPersons(Session session) throws IOException {
        try (BufferedReader br = resource("datasets/person.csv")) {
            br.readLine();
            List<Map<String,Object>> batch = new ArrayList<>();
            String line; int total = 0;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length < 3) continue;
                batch.add(Map.of("id", Long.parseLong(p[0]), "name", p[1], "age", Integer.parseInt(p[2])));
                if (batch.size() == 1000) { writePersons(session, batch); total += batch.size(); batch.clear(); }
            }
            if (!batch.isEmpty()) { writePersons(session, batch); total += batch.size(); }
            System.out.println("Persons loaded: " + total);
        }
    }

    private static void writePersons(Session session, List<Map<String,Object>> batch) {
        session.run("UNWIND $persons AS p MERGE (n:Person {id:p.id}) SET n.name=p.name, n.age=p.age",
                Values.parameters("persons", batch)).consume();
    }

    private static void loadRelationships(Session session) throws IOException {
        try (BufferedReader br = resource("datasets/person_knows_person.csv")) {
            br.readLine();
            List<Map<String,Object>> batch = new ArrayList<>();
            String line; int total = 0;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length < 2) continue;
                long from = Long.parseLong(p[0]), to = Long.parseLong(p[1]);
                if (from == to) continue;
                batch.add(Map.of("from", from, "to", to));
                if (batch.size() == 1000) { writeRelationships(session, batch); total += batch.size(); batch.clear(); }
            }
            if (!batch.isEmpty()) { writeRelationships(session, batch); total += batch.size(); }
            System.out.println("Relationships loaded: " + total);
        }
    }

    private static void writeRelationships(Session session, List<Map<String,Object>> batch) {
        session.run("UNWIND $rels AS r MATCH (a:Person {id:r.from}), (b:Person {id:r.to}) MERGE (a)-[:FRIEND]->(b)",
                Values.parameters("rels", batch)).consume();
    }

    private static BufferedReader resource(String path) {
        InputStream in = SeedData.class.getClassLoader().getResourceAsStream(path);
        if (in == null) throw new IllegalArgumentException("Missing resource: " + path);
        return new BufferedReader(new InputStreamReader(in));
    }
}
