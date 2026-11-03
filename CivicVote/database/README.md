# Database setup

Run `01-auth-schema.sql`, `02-election-schema.sql`, `03-voting-schema.sql`, `04-result-schema.sql`, then `05-development-data.sql` using MySQL Workbench or `mysql -u root -p < database/01-auth-schema.sql` for each file. Run `06-voting-test-database.sql` only if you want to run the optional isolated MySQL integration tests. Each microservice owns its schema and database connection. The voting database intentionally separates participation from anonymous ballots; do not add a voter reference to `anonymous_ballots` or a ballot reference to `vote_participations`.
