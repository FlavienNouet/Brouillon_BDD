-- MySQL 8.0+

DROP TABLE IF EXISTS refus_commande;
DROP TABLE IF EXISTS compte_transaction;
DROP TABLE IF EXISTS commande_ligne;
DROP TABLE IF EXISTS commande;
DROP TABLE IF EXISTS utilisateur;
DROP TABLE IF EXISTS livreur;
DROP TABLE IF EXISTS vehicule;
DROP TABLE IF EXISTS pizza_ingredient;
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS pizza;
DROP TABLE IF EXISTS taille;
DROP TABLE IF EXISTS client;

CREATE TABLE client (
    id_client BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    date_abonnement DATE NOT NULL DEFAULT CURDATE(),
    solde DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (solde >= 0)
);

CREATE TABLE pizza (
    id_pizza BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    prix_base DECIMAL(10,2) NOT NULL CHECK (prix_base > 0)
);

CREATE TABLE ingredient (
    id_ingredient BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE pizza_ingredient (
    id_pizza BIGINT NOT NULL,
    id_ingredient BIGINT NOT NULL,
    PRIMARY KEY (id_pizza, id_ingredient),
    FOREIGN KEY (id_pizza) REFERENCES pizza(id_pizza) ON DELETE CASCADE,
    FOREIGN KEY (id_ingredient) REFERENCES ingredient(id_ingredient) ON DELETE CASCADE
);

CREATE TABLE taille (
    code_taille VARCHAR(20) PRIMARY KEY,
    coefficient_prix DECIMAL(10,4) NOT NULL CHECK (coefficient_prix > 0)
);

CREATE TABLE vehicule (
    id_vehicule BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_vehicule VARCHAR(20) NOT NULL CHECK (type_vehicule IN ('voiture', 'moto', 'aucun')),
    immatriculation VARCHAR(20) UNIQUE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (
        (type_vehicule = 'aucun' AND immatriculation IS NULL)
        OR (type_vehicule IN ('voiture', 'moto'))
    )
);

CREATE TABLE livreur (
    id_livreur BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    id_vehicule BIGINT,
    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id_vehicule)
);

CREATE TABLE utilisateur (
    id_utilisateur BIGINT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CLIENT', 'LIVREUR', 'ADMIN')),
    id_client BIGINT NULL,
    id_livreur BIGINT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_client) REFERENCES client(id_client) ON DELETE SET NULL,
    FOREIGN KEY (id_livreur) REFERENCES livreur(id_livreur) ON DELETE SET NULL
);

CREATE TABLE commande (
    id_commande BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_client BIGINT NOT NULL,
    id_livreur BIGINT NOT NULL,
    date_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_livraison_prevue TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_livraison_reelle TIMESTAMP NULL DEFAULT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'cree' CHECK (statut IN ('cree', 'preparee', 'livree', 'refusee')),
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_livreur) REFERENCES livreur(id_livreur)
);

CREATE TABLE commande_ligne (
    id_ligne BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_commande BIGINT NOT NULL,
    id_pizza BIGINT NOT NULL,
    code_taille VARCHAR(20) NOT NULL,
    quantite INT NOT NULL CHECK (quantite > 0),
    prix_unitaire_base DECIMAL(10,2) NOT NULL CHECK (prix_unitaire_base > 0),
    prix_facture DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (prix_facture >= 0),
    est_gratuite BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_commande) REFERENCES commande(id_commande) ON DELETE CASCADE,
    FOREIGN KEY (id_pizza) REFERENCES pizza(id_pizza),
    FOREIGN KEY (code_taille) REFERENCES taille(code_taille)
);

CREATE TABLE compte_transaction (
    id_transaction BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_client BIGINT NOT NULL,
    type_transaction VARCHAR(30) NOT NULL CHECK (type_transaction IN ('recharge', 'debit_commande', 'remboursement_retard')),
    montant DECIMAL(10,2) NOT NULL CHECK (montant > 0),
    date_transaction TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_client) REFERENCES client(id_client)
);

CREATE TABLE refus_commande (
    id_refus BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_client BIGINT NOT NULL,
    date_refus TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_requis DECIMAL(10,2) NOT NULL,
    solde_disponible DECIMAL(10,2) NOT NULL,
    motif TEXT NOT NULL,
    FOREIGN KEY (id_client) REFERENCES client(id_client)
);

CREATE INDEX idx_commande_client_date ON commande(id_client, date_commande);
CREATE INDEX idx_commande_ligne_pizza ON commande_ligne(id_pizza);
CREATE INDEX idx_commande_livreur ON commande(id_livreur);
CREATE INDEX idx_transaction_client_date ON compte_transaction(id_client, date_transaction);
