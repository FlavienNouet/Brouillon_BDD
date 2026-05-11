-- Initialisation des utilisateurs de test pour MySQL
-- Prerequis:
-- 1. Executer sql/01_create_schema_mysql.sql
-- 2. Executer sql/02_business_logic_mysql.sql
-- 3. Executer sql/03_seed_data_mysql.sql
-- 4. Verifier que client #1 et livreur #1 existent

INSERT INTO utilisateur (login, password_hash, role, actif)
VALUES ('admin', '$2a$10$yl2tI5ss3DpDVTeCnaUMZ.Xgwyd/EMwrl2n3lKUXPVuiI2BMLlrBy', 'ADMIN', TRUE);

INSERT INTO utilisateur (login, password_hash, role, id_client, actif)
VALUES (
	'client1',
	'$2a$10$aIYmUDtXP0DtyKmO1W.OI.oXcG.rrqq2PfkpvQKgopL.YSn.ANX2.',
	'CLIENT',
	(SELECT id_client FROM client WHERE email = 'client1@mail.com' LIMIT 1),
	TRUE
);

INSERT INTO utilisateur (login, password_hash, role, id_livreur, actif)
VALUES (
	'livreur1',
	'$2a$10$/Xom55qfi67R.ycdyG8zYuUZaMHOz8bmUlVlX1EoDApz/5.JW68Z6',
	'LIVREUR',
	(SELECT id_livreur FROM livreur WHERE nom = 'Leo' LIMIT 1),
	TRUE
);
