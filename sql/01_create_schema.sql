-- PostgreSQL 14+

DROP TABLE IF EXISTS refus_commande CASCADE;
DROP TABLE IF EXISTS compte_transaction CASCADE;
DROP TABLE IF EXISTS commande_ligne CASCADE;
DROP TABLE IF EXISTS commande CASCADE;
DROP TABLE IF EXISTS livreur CASCADE;
DROP TABLE IF EXISTS vehicule CASCADE;
DROP TABLE IF EXISTS pizza_ingredient CASCADE;
DROP TABLE IF EXISTS ingredient CASCADE;
DROP TABLE IF EXISTS pizza CASCADE;
DROP TABLE IF EXISTS taille CASCADE;
DROP TABLE IF EXISTS client CASCADE;

CREATE TABLE client (
    id_client BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    date_abonnement DATE NOT NULL DEFAULT CURRENT_DATE,
    solde NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (solde >= 0)
);

CREATE TABLE pizza (
    id_pizza BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    prix_base NUMERIC(10,2) NOT NULL CHECK (prix_base > 0)
);

CREATE TABLE ingredient (
    id_ingredient BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE pizza_ingredient (
    id_pizza BIGINT NOT NULL REFERENCES pizza(id_pizza) ON DELETE CASCADE,
    id_ingredient BIGINT NOT NULL REFERENCES ingredient(id_ingredient) ON DELETE CASCADE,
    PRIMARY KEY (id_pizza, id_ingredient)
);

CREATE TABLE taille (
    code_taille VARCHAR(20) PRIMARY KEY,
    coefficient_prix NUMERIC(10,4) NOT NULL CHECK (coefficient_prix > 0)
);

CREATE TABLE vehicule (
    id_vehicule BIGSERIAL PRIMARY KEY,
    type_vehicule VARCHAR(20) NOT NULL CHECK (type_vehicule IN ('voiture', 'moto', 'aucun')),
    immatriculation VARCHAR(20) UNIQUE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (
        (type_vehicule = 'aucun' AND immatriculation IS NULL)
        OR (type_vehicule IN ('voiture', 'moto'))
    )
);

CREATE TABLE livreur (
    id_livreur BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    id_vehicule BIGINT REFERENCES vehicule(id_vehicule)
);

CREATE TABLE commande (
    id_commande BIGSERIAL PRIMARY KEY,
    id_client BIGINT NOT NULL REFERENCES client(id_client),
    id_livreur BIGINT NOT NULL REFERENCES livreur(id_livreur),
    date_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_livraison_prevue TIMESTAMP NOT NULL,
    date_livraison_reelle TIMESTAMP,
    statut VARCHAR(20) NOT NULL DEFAULT 'cree' CHECK (statut IN ('cree', 'preparee', 'livree', 'refusee'))
);

CREATE TABLE commande_ligne (
    id_ligne BIGSERIAL PRIMARY KEY,
    id_commande BIGINT NOT NULL REFERENCES commande(id_commande) ON DELETE CASCADE,
    id_pizza BIGINT NOT NULL REFERENCES pizza(id_pizza),
    code_taille VARCHAR(20) NOT NULL REFERENCES taille(code_taille),
    quantite INT NOT NULL CHECK (quantite > 0),
    prix_unitaire_base NUMERIC(10,2) NOT NULL CHECK (prix_unitaire_base > 0),
    prix_facture NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (prix_facture >= 0),
    est_gratuite BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE compte_transaction (
    id_transaction BIGSERIAL PRIMARY KEY,
    id_client BIGINT NOT NULL REFERENCES client(id_client),
    type_transaction VARCHAR(30) NOT NULL CHECK (type_transaction IN ('recharge', 'debit_commande', 'remboursement_retard')),
    montant NUMERIC(10,2) NOT NULL CHECK (montant > 0),
    date_transaction TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT
);

CREATE TABLE refus_commande (
    id_refus BIGSERIAL PRIMARY KEY,
    id_client BIGINT NOT NULL REFERENCES client(id_client),
    date_refus TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_requis NUMERIC(10,2) NOT NULL,
    solde_disponible NUMERIC(10,2) NOT NULL,
    motif TEXT NOT NULL
);

CREATE INDEX idx_commande_client_date ON commande(id_client, date_commande);
CREATE INDEX idx_commande_ligne_pizza ON commande_ligne(id_pizza);
CREATE INDEX idx_commande_livreur ON commande(id_livreur);
CREATE INDEX idx_transaction_client_date ON compte_transaction(id_client, date_transaction);
