# CivicVote Technologies

A React/Vite civic election application backed by six Spring Boot services, Eureka service discovery, a Spring Cloud Gateway, and four independent MySQL 8 databases.

## Project layout

```text
CivicVote/
├── frontend/                 React/Vite application
├── backend/                  Maven parent and six Spring Boot applications
│   ├── eureka-server/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── election-service/
│   ├── voting-service/
│   └── result-service/
└── database/                 Ordered MySQL schema and development data
```

## Requirements

- Java 21, Maven 3.9+, Node.js 20+, npm
- MySQL 8.0+
- VS Code (frontend/root) and Spring Tool Suite (optional backend IDE)

## Initialize MySQL

In MySQL Workbench, open and execute `database/01-auth-schema.sql` through `database/05-development-data.sql` in numerical order. `database/06-voting-test-database.sql` is optional and creates an isolated schema for gated MySQL integration tests. Or, from the project root with a MySQL account allowed to create databases:

```powershell
Get-Content .\database\01-auth-schema.sql, .\database\02-election-schema.sql, .\database\03-voting-schema.sql, .\database\04-result-schema.sql, .\database\05-development-data.sql | mysql -u root -p
```

The scripts create `civicvote_auth_db`, `civicvote_election_db`, `civicvote_voting_db`, and `civicvote_result_db`. Supply environment variables before starting backend services (defaults are for local development):

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-mysql-password"
$env:JWT_SECRET = "replace-this-local-development-secret-with-a-long-random-value"
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "ChangeMe-Admin-123!"
```

The Auth Service creates/updates the configured development administrator using BCrypt at startup. Change the development password before use. Production deployments must use managed secrets, TLS, restricted network access, and operational security controls.

## Run the backend

Open this `CivicVote` folder in VS Code. To import all backend modules in STS, choose **File → Import → Maven → Existing Maven Projects**, select `CivicVote/backend/`, and finish. The root `backend/pom.xml` is the reactor parent; each service also has its own `pom.xml` and Spring Boot main class and can be run independently.

From `backend/` build/test the complete reactor:

```powershell
mvn clean verify
```

Start services in separate terminals, in this order:

```powershell
cd backend\eureka-server; mvn spring-boot:run
cd backend\auth-service; mvn spring-boot:run
cd backend\election-service; mvn spring-boot:run
cd backend\voting-service; mvn spring-boot:run
cd backend\result-service; mvn spring-boot:run
cd backend\api-gateway; mvn spring-boot:run
```

Eureka dashboard: `http://localhost:8761`. Gateway: `http://localhost:8080`. Each service's `application.properties` reads database credentials, JWT secret, Eureka URL, and ports from environment variables. Eureka clients register as `localhost` for this single-machine setup; when services run on separate machines, set `EUREKA_INSTANCE_HOSTNAME` to a hostname reachable by the other services.

## Run the frontend

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

Visit `http://localhost:5173`. The Vite proxy sends `/api` requests through the gateway at port 8080. To use another gateway, set `VITE_API_BASE_URL` in `.env`.

## Typical workflow

1. On the sign-in page choose **Administrator** and sign in with the configured admin account. The default local development account is `admin` / `ChangeMe-Admin-123!` unless `ADMIN_USERNAME` / `ADMIN_PASSWORD` were overridden. Set a private password before use outside a local demo.
2. Use **Admin dashboard → Create election** to create an election with a future start and end time, or **Manage elections** to edit a scheduled election.
3. Open a scheduled election and add or remove candidates from its candidate-management section. At least two real candidates are needed for the election to open. Every election also includes a system-managed **NOTA (None of the Above)** choice; voters can select it, but administrators cannot edit or remove it.
4. Administrators can use **Monitor results** to review a closed election and publish its tally. Results are hidden until published.
5. Choose **Voter** on the sign-in page to register or sign in as a voter, select an open election and candidate, and confirm the ballot.
6. A successful response is returned only after participation and anonymous ballot records commit in the Voting Service database. Participation (`vote_participations`) and candidate selection (`anonymous_ballots`) are separate tables and have no voter-to-ballot key.
7. View results after an administrator publishes them. Result Service obtains aggregate counts from persisted ballots, not browser state, and reports NOTA votes separately.

Useful persistence check:

```sql
SELECT election_id, COUNT(*) AS ballots FROM civicvote_voting_db.anonymous_ballots GROUP BY election_id;
SELECT election_id, COUNT(*) AS participants FROM civicvote_voting_db.vote_participations GROUP BY election_id;
```

Do not join or expose voter participation rows with ballot selections. The local development environment, MySQL administrator, and service operators are trusted components; production use requires independent security review, key management, privacy assessment, audit logging, accessibility review, disaster recovery, and election-specific legal/operational controls. This sample is not certified for binding public elections.

## Tests and troubleshooting

Run `mvn test` from `backend/`. The normal test run includes JWT signing, role authorization, election validation, duplicate-vote protection, and result aggregation tests. A real MySQL persistence/rollback/concurrency integration suite is included but gated so a normal build never writes test data to an application database. To run it against the isolated test schema, execute `database/06-voting-test-database.sql`, then:

```powershell
mvn -pl voting-service "-Dcivicvote.mysql.integration=true" "-Dcivicvote.mysql.url=jdbc:mysql://localhost:3306/civicvote_voting_test_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" "-Dcivicvote.mysql.username=root" "-Dcivicvote.mysql.password=your-password" -Dtest=MySqlVotePersistenceIT test
```

The MySQL-backed suite covers committed persistence, transaction rollback on ballot failure, and concurrent duplicate submissions. The ordinary unit suite does not claim to verify a live Eureka/MySQL deployment.

With all six services running and registered in Eureka, the optional registry integration check can be run with `mvn -pl eureka-server "-Dcivicvote.eureka.integration=true" "-Dcivicvote.eureka.url=http://localhost:8761/eureka/apps" -Dtest=EurekaRegistrationIT test`. Gateway route mappings are also checked by the normal unit suite.

- **Services do not register:** start Eureka first and check each service's `EUREKA_URL`; inspect `http://localhost:8761`.
- **Gateway cannot reach a registered service:** for a single-machine setup, keep `EUREKA_INSTANCE_HOSTNAME` at its `localhost` default; for distributed deployments, set it to a hostname reachable from the Gateway and peer services.
- **Database connection errors:** verify MySQL is running, schema scripts ran, and `DB_USERNAME` / `DB_PASSWORD` are set.
- **JWT errors between services:** configure the identical sufficiently long `JWT_SECRET` for Auth, Gateway, Election, Voting, and Result.
- **Frontend network errors:** start Gateway and services, then check `VITE_API_BASE_URL` and browser network errors.
- **Port conflicts:** change the applicable service port environment variable and update gateway/service configuration as needed.
