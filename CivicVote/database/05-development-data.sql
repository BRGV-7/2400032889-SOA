-- Auth Service creates the configured administrator with BCrypt at startup.
-- Override ADMIN_USERNAME and ADMIN_PASSWORD before starting that service.
USE civicvote_election_db;
INSERT INTO elections (title, description, starts_at, ends_at, status)
SELECT 'CivicVote Demonstration Election', 'Seeded open election for local development; remove this fixture if not needed.',
       UTC_TIMESTAMP() - INTERVAL 1 MINUTE, UTC_TIMESTAMP() + INTERVAL 30 DAY, 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM elections WHERE title='CivicVote Demonstration Election');
INSERT INTO candidates (election_id, name, party, manifesto)
SELECT e.id, seed.name, seed.party, seed.manifesto
FROM elections e
JOIN (
  SELECT 'Jordan Lee' AS name, 'Community First' AS party, 'Prioritize accessible neighborhood services.' AS manifesto
  UNION ALL
  SELECT 'Casey Morgan', 'Forward Together', 'Build safer, greener public spaces.'
) seed
WHERE e.title='CivicVote Demonstration Election'
  AND NOT EXISTS (SELECT 1 FROM candidates c WHERE c.election_id=e.id AND c.name=seed.name);
