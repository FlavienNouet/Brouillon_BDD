CREATE TABLE IF NOT EXISTS utilisateur (
	id_utilisateur BIGINT AUTO_INCREMENT PRIMARY KEY,
	login VARCHAR(50) NOT NULL UNIQUE,
	password_hash VARCHAR(255) NOT NULL,
	role VARCHAR(20) NOT NULL,
	id_client BIGINT NULL,
	id_livreur BIGINT NULL,
	actif BOOLEAN NOT NULL DEFAULT TRUE,
	date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (id_client) REFERENCES client(id_client) ON DELETE SET NULL,
	FOREIGN KEY (id_livreur) REFERENCES livreur(id_livreur) ON DELETE SET NULL
);

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
