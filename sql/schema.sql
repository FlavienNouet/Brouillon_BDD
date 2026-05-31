SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS client (
    id_client BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    solde DECIMAL(10,2) NOT NULL DEFAULT 0,
    nb_pizzas_achetees INT NOT NULL DEFAULT 0,
    free_pizzas INT NOT NULL DEFAULT 0,
    date_abonnement DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS taille (
    code_taille VARCHAR(20) PRIMARY KEY,
    coefficient_prix DECIMAL(10,4) NOT NULL
);

CREATE TABLE IF NOT EXISTS pizza (
    id_pizza BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    prix_base DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS ingredient (
    id_ingredient BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS pizza_ingredient (
    id_pizza BIGINT NOT NULL,
    id_ingredient BIGINT NOT NULL,
    PRIMARY KEY (id_pizza, id_ingredient),
    FOREIGN KEY (id_pizza) REFERENCES pizza(id_pizza),
    FOREIGN KEY (id_ingredient) REFERENCES ingredient(id_ingredient)
);

CREATE TABLE IF NOT EXISTS vehicule (
    id_vehicule BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_vehicule VARCHAR(30) NOT NULL,
    immatriculation VARCHAR(30) NULL UNIQUE,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS livreur (
    id_livreur BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    id_vehicule BIGINT NULL,
    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id_vehicule)
);

CREATE TABLE IF NOT EXISTS commande (
    id_commande BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_client BIGINT NOT NULL,
    id_vehicule BIGINT NULL,
    id_livreur BIGINT NOT NULL,
    id_pizza BIGINT NOT NULL,
    code_taille VARCHAR(20) NOT NULL,
    date_commande DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_livraison_prevue DATETIME NOT NULL,
    date_prise_en_charge DATETIME NULL DEFAULT NULL,
    date_livraison_reelle DATETIME NULL DEFAULT NULL,
    prix_unitaire_base DECIMAL(10,2) NULL,
    prix_facture DECIMAL(10,2) NOT NULL DEFAULT 0,
    quantite INT NOT NULL DEFAULT 1,
    est_gratuite BOOLEAN NOT NULL DEFAULT FALSE,
    debit_effectue BOOLEAN NOT NULL DEFAULT FALSE,
    statut VARCHAR(20) NOT NULL DEFAULT 'cree',
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id_vehicule),
    FOREIGN KEY (id_livreur) REFERENCES livreur(id_livreur),
    FOREIGN KEY (id_pizza) REFERENCES pizza(id_pizza),
    FOREIGN KEY (code_taille) REFERENCES taille(code_taille)
);

CREATE TABLE IF NOT EXISTS compte_transaction (
    id_transaction BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_client BIGINT NOT NULL,
    type_transaction VARCHAR(30) NOT NULL,
    montant DECIMAL(10,2) NOT NULL,
    date_transaction TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_client) REFERENCES client(id_client)
);

CREATE TABLE IF NOT EXISTS refus_commande (
    id_refus BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_client BIGINT NOT NULL,
    date_refus TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_requis DECIMAL(10,2) NOT NULL,
    solde_disponible DECIMAL(10,2) NOT NULL,
    motif TEXT NOT NULL,
    FOREIGN KEY (id_client) REFERENCES client(id_client)
);

CREATE TABLE IF NOT EXISTS utilisation_vehicule (
    id_utilisation BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_commande BIGINT NULL,
    id_livreur BIGINT NOT NULL,
    id_vehicule BIGINT NOT NULL,
    date_debut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP NULL,
    km_parcourus INT NULL,
    commentaire TEXT,
    FOREIGN KEY (id_commande) REFERENCES commande(id_commande),
    FOREIGN KEY (id_livreur) REFERENCES livreur(id_livreur),
    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id_vehicule)
);

CREATE INDEX idx_commande_client_date ON commande(id_client, date_commande);
CREATE INDEX idx_commande_livreur ON commande(id_livreur);
CREATE INDEX idx_commande_pizza ON commande(id_pizza);
CREATE INDEX idx_transaction_client_date ON compte_transaction(id_client, date_transaction);

DELIMITER $$

DROP TRIGGER IF EXISTS before_commande_insert$$
CREATE TRIGGER before_commande_insert
BEFORE INSERT ON commande
FOR EACH ROW
BEGIN
    DECLARE v_base DECIMAL(10,2);
    DECLARE v_coeff DECIMAL(10,4);
    DECLARE v_free INT DEFAULT 0;
    DECLARE v_solde DECIMAL(10,2) DEFAULT 0;
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    DECLARE v_nb INT DEFAULT 0;

    SELECT prix_base INTO v_base
    FROM pizza
    WHERE id_pizza = NEW.id_pizza;

    SELECT coefficient_prix INTO v_coeff
    FROM taille
    WHERE code_taille = NEW.code_taille;

    IF NEW.quantite IS NULL OR NEW.quantite < 1 THEN
        SET NEW.quantite = 1;
    END IF;

    SET NEW.prix_unitaire_base = v_base;
    SET v_total = ROUND(v_base * v_coeff * NEW.quantite, 2);
    SET NEW.prix_facture = v_total;

    SELECT COALESCE(free_pizzas, 0), COALESCE(solde, 0)
    INTO v_free, v_solde
    FROM client
    WHERE id_client = NEW.id_client
    FOR UPDATE;

    IF v_free > 0 THEN
        SET NEW.est_gratuite = TRUE;
        SET NEW.prix_facture = 0;
        SET NEW.debit_effectue = TRUE;

        UPDATE client
        SET free_pizzas = free_pizzas - 1,
            nb_pizzas_achetees = nb_pizzas_achetees + NEW.quantite
        WHERE id_client = NEW.id_client;
    ELSE
        IF v_solde >= v_total THEN
            SET NEW.est_gratuite = FALSE;
            SET NEW.debit_effectue = TRUE;

            UPDATE client
            SET solde = solde - v_total,
                nb_pizzas_achetees = nb_pizzas_achetees + NEW.quantite
            WHERE id_client = NEW.id_client;

            INSERT INTO compte_transaction (id_client, type_transaction, montant, commentaire)
            VALUES (NEW.id_client, 'debit_commande', v_total, CONCAT('Debit commande ', NEW.id_client));
        ELSE
            SET NEW.statut = 'refusee';
            SET NEW.debit_effectue = FALSE;

            INSERT INTO refus_commande (id_client, montant_requis, solde_disponible, motif)
            VALUES (NEW.id_client, v_total, v_solde, CONCAT('Solde insuffisant pour commande ', NEW.id_client));

            UPDATE client
            SET nb_pizzas_achetees = nb_pizzas_achetees + NEW.quantite
            WHERE id_client = NEW.id_client;
        END IF;

        SELECT nb_pizzas_achetees INTO v_nb
        FROM client
        WHERE id_client = NEW.id_client
        FOR UPDATE;

        WHILE v_nb >= 10 DO
            UPDATE client
            SET nb_pizzas_achetees = nb_pizzas_achetees - 10,
                free_pizzas = free_pizzas + 1
            WHERE id_client = NEW.id_client;

            SELECT nb_pizzas_achetees INTO v_nb
            FROM client
            WHERE id_client = NEW.id_client
            FOR UPDATE;
        END WHILE;
    END IF;
END$$

DROP TRIGGER IF EXISTS before_commande_update_delivery$$
CREATE TRIGGER before_commande_update_delivery
BEFORE UPDATE ON commande
FOR EACH ROW
BEGIN
    DECLARE v_diff INT;

    IF NEW.date_livraison_reelle IS NOT NULL
       AND OLD.date_livraison_reelle IS NULL
       AND NEW.date_prise_en_charge IS NOT NULL THEN
        SET v_diff = TIMESTAMPDIFF(MINUTE, NEW.date_prise_en_charge, NEW.date_livraison_reelle);

        IF v_diff > 30 THEN
            INSERT INTO compte_transaction (id_client, type_transaction, montant, commentaire)
            VALUES (NEW.id_client, 'remboursement_retard', NEW.prix_facture, CONCAT('Remboursement retard commande ', NEW.id_commande));

            UPDATE client
            SET solde = solde + NEW.prix_facture
            WHERE id_client = NEW.id_client;

            SET NEW.est_gratuite = TRUE;
            SET NEW.prix_facture = 0;
        END IF;
    END IF;
END$$

DELIMITER ;