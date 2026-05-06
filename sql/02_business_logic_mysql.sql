-- MySQL 8.0+
-- Fonctions, triggers et procedures metier

DELIMITER $$

DROP FUNCTION IF EXISTS fn_calc_prix_ligne$$

CREATE FUNCTION fn_calc_prix_ligne(
    p_prix_base DECIMAL(10,2),
    p_code_taille VARCHAR(20),
    p_quantite INT,
    p_est_gratuite BOOLEAN
) RETURNS DECIMAL(10,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_coef DECIMAL(10,4);
    DECLARE msg VARCHAR(255);
    
    IF p_est_gratuite THEN
        RETURN 0;
    END IF;
    
    SELECT coefficient_prix INTO v_coef
    FROM taille
    WHERE code_taille = p_code_taille;
    
    IF v_coef IS NULL THEN
        SET msg = CONCAT('Taille inconnue: ', p_code_taille);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = msg;
    END IF;
    
    RETURN ROUND(p_prix_base * v_coef * p_quantite, 2);
END$$

DROP TRIGGER IF EXISTS before_commande_ligne_calc_prix$$

CREATE TRIGGER before_commande_ligne_calc_prix
BEFORE INSERT ON commande_ligne
FOR EACH ROW
BEGIN
    SET NEW.prix_facture = fn_calc_prix_ligne(
        NEW.prix_unitaire_base,
        NEW.code_taille,
        NEW.quantite,
        NEW.est_gratuite
    );
END$$

DROP PROCEDURE IF EXISTS fn_recharger_compte$$

CREATE PROCEDURE fn_recharger_compte(
    IN p_id_client BIGINT,
    IN p_montant DECIMAL(10,2)
)
MODIFIES SQL DATA
BEGIN
    DECLARE msg VARCHAR(255);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    IF p_montant <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Le montant de recharge doit etre > 0';
    END IF;
    
    UPDATE client
    SET solde = solde + p_montant
    WHERE id_client = p_id_client;
    
    IF ROW_COUNT() = 0 THEN
        SET msg = CONCAT('Client introuvable: ', p_id_client);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = msg;
    END IF;
    
    INSERT INTO compte_transaction(id_client, type_transaction, montant, commentaire)
    VALUES (p_id_client, 'recharge', p_montant, 'Recharge compte prepaye');
    
    COMMIT;
END$$

DROP PROCEDURE IF EXISTS fn_appliquer_gratuite_retard$$

CREATE PROCEDURE fn_appliquer_gratuite_retard(
    IN p_id_commande BIGINT
)
MODIFIES SQL DATA
proc_appliquer: BEGIN
    DECLARE v_id_client BIGINT;
    DECLARE v_total_deja_facture DECIMAL(10,2);
    
    SELECT c.id_client, COALESCE(SUM(cl.prix_facture), 0)
    INTO v_id_client, v_total_deja_facture
    FROM commande c
    JOIN commande_ligne cl ON cl.id_commande = c.id_commande
    WHERE c.id_commande = p_id_commande
    GROUP BY c.id_client;
    
    IF v_total_deja_facture <= 0 THEN
        LEAVE proc_appliquer;
    END IF;
    
    UPDATE commande_ligne
    SET est_gratuite = TRUE
    WHERE id_commande = p_id_commande
    AND est_gratuite = FALSE;
    
    UPDATE client
    SET solde = solde + v_total_deja_facture
    WHERE id_client = v_id_client;
    
    INSERT INTO compte_transaction(id_client, type_transaction, montant, commentaire)
    VALUES (v_id_client, 'remboursement_retard', v_total_deja_facture, 'Livraison > 30 min, commande gratuite');
END$$

DROP TRIGGER IF EXISTS after_commande_retard_gratuite$$

CREATE TRIGGER after_commande_retard_gratuite
AFTER UPDATE ON commande
FOR EACH ROW
BEGIN
    DECLARE v_minutes_retard INT;
    
    IF NEW.date_livraison_reelle IS NOT NULL THEN
        SET v_minutes_retard = TIMESTAMPDIFF(MINUTE, NEW.date_commande, NEW.date_livraison_reelle);
        
        IF v_minutes_retard > 30 THEN
            CALL fn_appliquer_gratuite_retard(NEW.id_commande);
        END IF;
    END IF;
END$$

DROP PROCEDURE IF EXISTS fn_passer_commande$$

CREATE PROCEDURE fn_passer_commande(
    IN p_id_client BIGINT,
    IN p_id_livreur BIGINT,
    IN p_lignes_json JSON,
    IN p_minutes_livraison INT,
    OUT p_id_commande BIGINT
)
MODIFIES SQL DATA
BEGIN
    DECLARE v_solde DECIMAL(10,2);
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    DECLARE v_prix_base DECIMAL(10,2);
    DECLARE v_id_pizza BIGINT;
    DECLARE v_taille VARCHAR(20);
    DECLARE v_qte INT;
    DECLARE v_nb_pizzas_historique INT;
    DECLARE v_compteur INT;
    DECLARE v_est_gratuite BOOLEAN;
    DECLARE v_cout_ligne DECIMAL(10,2);
    DECLARE i INT DEFAULT 0;
    DECLARE v_array_length INT;
    DECLARE msg VARCHAR(255);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_id_commande = NULL;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    SELECT solde INTO v_solde
    FROM client
    WHERE id_client = p_id_client
    FOR UPDATE;
    
    IF v_solde IS NULL THEN
        SET msg = CONCAT('Client introuvable: ', p_id_client);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = msg;
    END IF;
    
    SET v_array_length = JSON_LENGTH(p_lignes_json);
    
    IF v_array_length = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La commande doit contenir au moins une pizza';
    END IF;
    
    INSERT INTO commande(id_client, id_livreur, date_livraison_prevue, statut)
    VALUES (p_id_client, p_id_livreur, DATE_ADD(NOW(), INTERVAL 30 MINUTE), 'preparee');
    
    SET p_id_commande = LAST_INSERT_ID();
    
    SELECT COALESCE(SUM(cl.quantite), 0)
    INTO v_nb_pizzas_historique
    FROM commande c
    JOIN commande_ligne cl ON cl.id_commande = c.id_commande
    WHERE c.id_client = p_id_client
    AND c.id_commande <> p_id_commande;
    
    SET v_compteur = v_nb_pizzas_historique;
    
    WHILE i < v_array_length DO
        SET v_id_pizza = JSON_UNQUOTE(JSON_EXTRACT(p_lignes_json, CONCAT('$[', i, '].pizza_id')));
        SET v_taille = JSON_UNQUOTE(JSON_EXTRACT(p_lignes_json, CONCAT('$[', i, '].taille')));
        SET v_qte = JSON_UNQUOTE(JSON_EXTRACT(p_lignes_json, CONCAT('$[', i, '].quantite')));
        
        SELECT prix_base INTO v_prix_base
        FROM pizza
        WHERE id_pizza = v_id_pizza;
        
        IF v_prix_base IS NULL THEN
            SET msg = CONCAT('Pizza introuvable: ', v_id_pizza);
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = msg;
        END IF;
        
        IF v_qte <= 0 THEN
            SET msg = CONCAT('Quantite invalide pour pizza ', v_id_pizza);
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = msg;
        END IF;
        
        BLOCK_LOOP: BEGIN
            DECLARE j INT DEFAULT 1;
            WHILE j <= v_qte DO
                SET v_compteur = v_compteur + 1;
                SET v_est_gratuite = (MOD(v_compteur, 10) = 0);

                SET v_cout_ligne = fn_calc_prix_ligne(v_prix_base, v_taille, 1, v_est_gratuite);
                SET v_total = v_total + v_cout_ligne;

                INSERT INTO commande_ligne(
                    id_commande, id_pizza, code_taille, quantite,
                    prix_unitaire_base, est_gratuite
                ) VALUES (
                    p_id_commande, v_id_pizza, v_taille, 1,
                    v_prix_base, v_est_gratuite
                );

                SET j = j + 1;
            END WHILE;
        END BLOCK_LOOP;
        
        SET i = i + 1;
    END WHILE;
    
    IF v_total > v_solde THEN
        INSERT INTO refus_commande(id_client, montant_requis, solde_disponible, motif)
        VALUES (p_id_client, v_total, v_solde, 'Solde insuffisant');
        
        DELETE FROM commande WHERE id_commande = p_id_commande;
        
        SET p_id_commande = NULL;
        ROLLBACK;
    ELSE
        UPDATE client
        SET solde = solde - v_total
        WHERE id_client = p_id_client;
        
        INSERT INTO compte_transaction(id_client, type_transaction, montant, commentaire)
        VALUES (p_id_client, 'debit_commande', v_total, CONCAT('Debitage commande ', p_id_commande));
        
        UPDATE commande
        SET date_livraison_reelle = DATE_ADD(date_commande, INTERVAL p_minutes_livraison MINUTE),
            statut = 'livree'
        WHERE id_commande = p_id_commande;
        
        COMMIT;
    END IF;
END$$

DELIMITER ;
