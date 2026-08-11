# CognoDB Graph Relationship Explorer

A small web application backed by CognoDB that allows a non-technical user to explore a Person graph and discover relationships between people through graph traversals.

The application provides:

- Person lookup by ID
- Direct friend traversal
- Friends-of-friends traversal up to 2 hops
- Wider network traversal up to 3 hops
- Result counts
- Loading/status messages
- Empty-result handling
- Database error handling
- Responsive web interface

# 1. Project Overview

The CognoDB Graph Relationship Explorer demonstrates how a graph database can be used to explore relationships between people.

The application is built using:

- Java 21
- Spring Boot 3.5.5
- CognoDB
- Neo4j Java Driver
- openCypher
- HTML
- CSS
- JavaScript
- Maven

The backend exposes REST APIs and communicates with CognoDB using the Neo4j Java Driver.

The frontend provides a simple interface where a user can enter a Person ID and explore the person's connections.

# 2. Use Case

The selected use case is a Person Relationship Explorer.

A user can search for a person and answer questions such as:

- Who is this person's direct friend?
- Who are this person's friends of friends?
- Which people are connected within three relationship hops?
- How large is the person's local network?

This type of relationship traversal is naturally represented using a graph database.

# 3. Why a Graph Database?

The application is centered on relationships between people.

Questions such as "Who are this person's friends of friends?" require traversing multiple relationships.

In a relational database, this type of query may require multiple joins and becomes increasingly complex as the traversal depth increases.

In a graph database, people are represented as nodes and their relationships are represented directly as edges.

For this application, the relationship is represented as:

(:Person)-[:FRIEND]->(:Person)

This makes multi-hop relationship traversal natural to express using openCypher.

# 4. Graph Data Model

The application uses the following graph model:

(:Person)-[:FRIEND]->(:Person)

Each Person contains:

Person
 ├── id
 ├── name
 └── age

Example:

(:Person {
    id: 50000,
    name: "Person50000",
    age: 56
})

People are connected using:

(:Person)-[:FRIEND]->(:Person)

Example:

Person50000
     |
     | FRIEND
     v
Person25497

# 5. Graph Traversals

The application supports multiple traversal depths.

## 1-Hop - Direct Friends

Person
   |
   | FRIEND
   v
Friend

Query:

MATCH (p:Person {id: $id})-[:FRIEND]->(f:Person)
RETURN f.id AS id, f.name AS name, f.age AS age
ORDER BY f.id
LIMIT 50

## 2-Hop - Friends of Friends

Person
   |
   | FRIEND
   v
Friend
   |
   | FRIEND
   v
Friend of Friend

Query:

MATCH (p:Person {id: $id})-[:FRIEND]->()-[:FRIEND]->(f:Person)
WHERE f.id <> $id
RETURN DISTINCT
       f.id AS id,
       f.name AS name,
       f.age AS age
ORDER BY f.id
LIMIT 50

## Up to 3-Hop Network

Query:

MATCH (p:Person {id: $id})-[:FRIEND*1..3]->(f:Person)
WHERE f.id <> $id
RETURN DISTINCT
       f.id AS id,
       f.name AS name,
       f.age AS age
ORDER BY f.id
LIMIT 100

This retrieves people reachable within one, two, or three FRIEND relationships.

# 6. Parameterized Queries

All application queries use parameters instead of directly concatenating user input into Cypher.

Example:

MATCH (p:Person {id: $id})
RETURN p.id AS id, p.name AS name, p.age AS age

The parameter is supplied by the Neo4j Java Driver:

Values.parameters("id", id)

This keeps user input separate from the Cypher query structure.

# 7. Application Architecture

The application follows a simple layered architecture:

                    Web Browser
                         |
                         v
                  HTML / CSS / JS
                         |
                         v
                PersonController
                         |
                         v
                   PersonService
                         |
                         v
                  PersonRepository
                         |
                         v
                Neo4j Java Driver
                         |
                         v
                      CognoDB

## Controller

PersonController.java

Responsible for exposing REST endpoints.

## Service

PersonService.java

Provides the service layer between the controller and repository.

## Repository

PersonRepository.java

Contains the Cypher queries used to retrieve data from CognoDB.

## Configuration

CognoDbConfig.java

Configures the CognoDB database connection.

## Seed

SeedData.java

Provides the mechanism for loading the required graph dataset into a target CognoDB instance when needed.

# 8. Project Structure

CognoDB-Graph-App/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/wexa/cognograph/
│   │   │       ├── CognoGraphApplication.java
│   │   │       │
│   │   │       ├── config/
│   │   │       │   └── CognoDbConfig.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   └── PersonController.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   └── PersonRepository.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   └── PersonService.java
│   │   │       │
│   │   │       └── seed/
│   │   │           └── SeedData.java
│   │   │
│   │   └── resources/
│   │       ├── datasets/
│   │       │   ├── person.csv
│   │       │   └── person_knows_person.csv
│   │       │
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── style.css
│   │       │   └── app.js
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md

# 9. Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot 3.5.5 | Web application framework |
| CognoDB | Graph database |
| Neo4j Java Driver | Database connectivity |
| openCypher | Graph query language |
| HTML | Web page structure |
| CSS | User interface styling |
| JavaScript | Frontend interaction |
| Maven | Project/build management |

# 10. Environment Variables

The application uses environment variables for database configuration.

Set the following variables before running the application:

COGNODB_URI=bolt+s://<instance>.databases.cognodb.cloud
COGNODB_USERNAME=cognodb
COGNODB_PASSWORD=<your-password>

Never commit real database credentials to GitHub.

Example application.properties:

cognodb.uri=${COGNODB_URI}
cognodb.username=${COGNODB_USERNAME}
cognodb.password=${COGNODB_PASSWORD}

# 11. Dataset

The application uses a Person graph dataset containing Person nodes and FRIEND relationships.

The repository contains CSV files under:

src/main/resources/datasets/

The dataset files are:

person.csv
person_knows_person.csv

The dataset was adapted from the graph data used during the earlier CognoDB benchmarking work.

The existing CognoDB dataset can also be reused when the target database already contains the required Person and FRIEND graph.

# 12. Seed Data

The repository includes:

com.wexa.cognograph.seed.SeedData

The seed process can be used when the target CognoDB instance needs the dataset.

The purpose of the seed process is to provide a repeatable way to create the required graph data.

Before running the seed process, ensure that:

1. The CognoDB connection is configured.
2. The correct database credentials are available.
3. The required CSV files are present.
4. The target database is intended to receive the seed data.

If the target database already contains the required Assignment 1 dataset, the existing data can be reused instead of loading the dataset again.

# 13. Running the Application

## Step 1 - Clone the repository

git clone <YOUR_GITHUB_REPOSITORY_URL>

## Step 2 - Open the project

Open the project in Eclipse or another Java IDE.

## Step 3 - Configure CognoDB

Set:

COGNODB_URI
COGNODB_USERNAME
COGNODB_PASSWORD

## Step 4 - Build the project

mvn clean package

## Step 5 - Run the application

mvn spring-boot:run

Or run:

CognoGraphApplication.java

from Eclipse.

## Step 6 - Open the application

Open:

http://localhost:8080

# 14. Using the Web Application

The application provides a simple Person search interface.

Example:

Enter:

50000

and click:

Search

The application retrieves the person and displays:

Person50000
Person ID: 50000
Age: 56

The application then displays:

Direct Friends

Friends of Friends (2 hops)

Network (up to 3 hops)

# 15. REST API

## Person Lookup

GET /api/person/{id}

Example:

GET /api/person/50000

Example response:

{
  "id": 50000,
  "name": "Person50000",
  "age": 56
}

## Direct Friends

GET /api/person/{id}/friends

Example:

GET /api/person/50000/friends

## Friends of Friends

GET /api/person/{id}/friends-of-friends

Example:

GET /api/person/50000/friends-of-friends

## Network

GET /api/person/{id}/network

Example:

GET /api/person/50000/network

# 16. Example Test Results

The application was tested using Person ID:

50000

Person lookup returned:

ID   : 50000
Name : Person50000
Age  : 56

Direct friend traversal returned:

4 results

Friends-of-friends traversal returned:

11 results

The network traversal up to three hops returned:

48 results

These results demonstrate that the application is successfully retrieving graph relationships from CognoDB.

# 17. Error Handling

The application handles database/request errors through the REST controller.

If CognoDB cannot be reached or a database query fails, the application returns an internal server error response with a user-friendly message.

Example:

{
  "error": "Unable to reach or query CognoDB",
  "message": "Please check the database connection and try again."
}

The frontend also provides status messages such as:

Loading graph data...

and:

Graph loaded successfully.

If no connections are found, the application displays:

No connections found.

If a person does not exist, the application reports:

Person not found.

# 18. UI Features

The web interface provides:

- Person ID search
- Person profile information
- Age display
- Direct friend results
- 2-hop friend results
- 3-hop network results
- Result counts
- Loading/status message
- Empty state
- Error message
- Responsive layout

The UI is implemented using plain HTML, CSS, and JavaScript and does not require a separate frontend framework.

# 19. Data Flow

The application follows this flow:

User enters Person ID
          |
          v
      Web Browser
          |
          v
      REST Request
          |
          v
   PersonController
          |
          v
     PersonService
          |
          v
   PersonRepository
          |
          v
    Cypher Query
          |
          v
      CognoDB
          |
          v
     Query Results
          |
          v
      REST Response
          |
          v
      Web Browser

# 20. Security Considerations

Database credentials are not stored directly in the source code.

The application uses environment variables:

COGNODB_URI
COGNODB_USERNAME
COGNODB_PASSWORD

Cypher queries use parameters such as:

$id

instead of directly concatenating user-provided values into queries.

Real credentials must not be committed to GitHub.

# 21. Limitations

The current application is intentionally small and focused on demonstrating graph traversal.

Current limitations include:

- The application focuses on Person and FRIEND relationships.
- Results are limited to a fixed number of records.
- The network view currently returns relationship results rather than a visual node-link graph.
- Authentication for application users is not implemented.
- The application depends on the availability of the configured CognoDB instance.
- Deployment depends on the availability of a suitable hosting environment and database connection.
- The application is intended as a demonstration of graph traversal rather than a production-scale social networking platform.

# 22. Future Improvements

Possible future enhancements include:

- Interactive visual graph rendering
- Clickable Person nodes
- Dynamic traversal depth selection
- Relationship type filtering
- Search by name
- Pagination
- User authentication
- Caching
- More detailed graph analytics
- Production deployment
- Monitoring and logging
- Additional automated tests

# 23. Assignment 2 Deliverables

The final submission should include:

- Working CognoDB-backed application
- Seed/data-loading mechanism
- Graph data model
- Explanation of why a graph database is appropriate
- Parameterized Cypher queries
- At least one multi-hop traversal
- Functional web interface
- Loading and empty states
- Error handling
- README documentation
- UI screenshots
- Deployment details
- Hosted demo URL
- Short screen recording
- Public GitHub repository

# 24. Screenshots

Add application screenshots here before final submission.

## Application Home Page

Add screenshot here.

## Person Search Result

Add screenshot here.

## Direct Friends

Add screenshot here.

## Friends of Friends - 2 Hops

Add screenshot here.

## Network - Up to 3 Hops

Add screenshot here.

# 25. Hosted Demo

Add the deployed application URL here after deployment.

Hosted Demo:
Deployment pending.

# 26. GitHub Repository

Add the final public repository URL here:

GitHub Repository:
https://github.com/Mahalakshmi-2105/CognoDB-Graph-Relationship-Explorer

# 27. Demo Recording

Add the screen recording reference/link here before submission.

The recording should demonstrate:

1. Opening the application.
2. Searching for a Person.
3. Viewing the Person details.
4. Viewing direct friends.
5. Viewing 2-hop friends.
6. Viewing the 3-hop network.
7. Showing the application working with CognoDB.

Demo Recording:
<ADD_RECORDING_LINK>

# 28. Final Verification Checklist

Before submitting the project, verify:

[ ] Application starts successfully
[ ] CognoDB connection works
[ ] Person lookup works
[ ] Direct friends query works
[ ] 2-hop query works
[ ] 3-hop/network query works
[ ] Parameterized Cypher is used
[ ] Error handling works
[ ] Loading/status message works
[ ] Empty state works
[ ] UI is responsive
[ ] SeedData is present
[ ] Dataset files are present
[ ] No real credentials are committed
[ ] README is complete
[ ] Screenshots are added
[ ] Application is deployed
[ ] Hosted URL is added
[ ] Demo recording is completed
[ ] GitHub repository is public
[ ] Final repository is tested from a clean checkout

# 29. Author

Mahalakshmi

Graph Relationship Explorer using CognoDB, Spring Boot, and openCypher.