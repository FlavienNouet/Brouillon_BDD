-- MySQL 8.0+

DELIMITER $$

-- Avant insertion d'une commande : calculer prix et consommer pizza gratuite si disponible
CREATE TRIGGER before_commande_insert
BEFORE INSERT ON commande
FOR EACH ROW
BEGIN
    DECLARE v_base DECIMAL(10,2);
    DECLARE v_coeff DECIMAL(10,4);
    DECLARE v_free INT DEFAULT 0;
    -- récupérer prix de base et coefficient
    SELECT prix_base INTO v_base FROM pizza WHERE id_pizza = NEW.id_pizza;
    SELECT coefficient_prix INTO v_coeff FROM taille WHERE code_taille = NEW.code_taille;
    SET NEW.prix_unitaire_base = v_base;
    IF NEW.quantite IS NULL OR NEW.quantite < 1 THEN
        SET NEW.quantite = 1;
    END IF;
    SELECT COALESCE(free_pizzas,0) INTO v_free FROM client WHERE id_client = NEW.id_client;
    IF v_free > 0 THEN
        SET NEW.est_gratuite = TRUE;
        SET NEW.prix_facture = 0;
        UPDATE client SET free_pizzas = free_pizzas - 1 WHERE id_client = NEW.id_client;
    ELSE
        SET NEW.prix_facture = v_base * v_coeff * NEW.quantite;
    END IF;
END$$

-- Après insertion : effectuer un seul débit si possible, sinon enregistrer le refus ; gérer fidélité
CREATE TRIGGER after_commande_insert_debit_and_loyalty
AFTER INSERT ON commande
FOR EACH ROW
BEGIN
    DECLARE v_solde DECIMAL(10,2) DEFAULT 0;
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    DECLARE v_nb INT DEFAULT 0;
    SET v_total = NEW.prix_facture;
    IF NEW.est_gratuite = FALSE THEN
        SELECT solde INTO v_solde FROM client WHERE id_client = NEW.id_client FOR UPDATE;
        IF v_solde >= v_total THEN
            INSERT INTO compte_transaction (id_client, type_transaction, montant, commentaire)
            VALUES (NEW.id_client, 'debit_commande', v_total, CONCAT('Débit commande ', NEW.id_commande));
            UPDATE client SET solde = solde - v_total WHERE id_client = NEW.id_client;
            UPDATE commande SET debit_effectue = TRUE WHERE id_commande = NEW.id_commande;
        ELSE
            INSERT INTO refus_commande (id_client, montant_requis, solde_disponible, motif)
            VALUES (NEW.id_client, v_total, v_solde, CONCAT('Solde insuffisant pour commande ', NEW.id_commande));
            UPDATE commande SET statut = 'refusee' WHERE id_commande = NEW.id_commande;
        END IF;
        -- mettre à jour la fidélité
        UPDATE client SET nb_pizzas_achetees = nb_pizzas_achetees + NEW.quantite WHERE id_client = NEW.id_client;
        SELECT nb_pizzas_achetees INTO v_nb FROM client WHERE id_client = NEW.id_client FOR UPDATE;
        WHILE v_nb >= 10 DO
            UPDATE client SET nb_pizzas_achetees = nb_pizzas_achetees - 10, free_pizzas = free_pizzas + 1 WHERE id_client = NEW.id_client;
            SELECT nb_pizzas_achetees INTO v_nb FROM client WHERE id_client = NEW.id_client FOR UPDATE;
        END WHILE;
    ELSE
        -- commande gratuite : marquer débit_effectue mais ne pas toucher au solde
        UPDATE commande SET debit_effectue = TRUE WHERE id_commande = NEW.id_commande;
    END IF;
END$$

-- Après update sur commande : si livraison réelle > 30 min, rembourser et marquer gratuite
CREATE TRIGGER after_commande_update_delivery
AFTER UPDATE ON commande
FOR EACH ROW
BEGIN
    DECLARE v_diff INT;
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    -- n'agir que la première fois où date_livraison_reelle est renseignée
    IF NEW.date_livraison_reelle IS NOT NULL AND OLD.date_livraison_reelle IS NULL AND NEW.date_prise_en_charge IS NOT NULL THEN
        SET v_diff = TIMESTAMPDIFF(MINUTE, NEW.date_prise_en_charge, NEW.date_livraison_reelle);
        IF v_diff > 30 THEN
            SET v_total = NEW.prix_facture;
            IF v_total > 0 THEN
                INSERT INTO compte_transaction (id_client, type_transaction, montant, commentaire)
                VALUES (NEW.id_client, 'remboursement_retard', v_total, CONCAT('Remboursement retard commande ', NEW.id_commande));
                UPDATE client SET solde = solde + v_total WHERE id_client = NEW.id_client;
            END IF;
            UPDATE commande SET est_gratuite = TRUE, prix_facture = 0 WHERE id_commande = NEW.id_commande;
        END IF;
    END IF;
END$$

DELIMITER ;
    date_livraison_reelle TIMESTAMP NULL DEFAULT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'cree' CHECK (statut IN ('cree', 'preparee', 'en_livraison', 'livree', 'refusee')),
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id_vehicule),
    FOREIGN KEY (id_livreur) REFERENCES livreur(id_livreur),
    FOREIGN KEY (id_pizza) REFERENCES pizza(id_pizza),
    FOREIGN KEY (code_taille) REFERENCES taille(code_taille)
);

-- commande_ligne supprimée : modèle simplifié (1 commande = 1 pizza)

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

CREATE TABLE utilisation_vehicule (
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

-- Les triggers de ligne de commande ont été supprimés : voir triggers sur `commande`.