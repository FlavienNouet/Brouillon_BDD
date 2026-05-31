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

    SELECT id_client, COALESCE(prix_facture,0)
    INTO v_id_client, v_total_deja_facture
    FROM commande
    WHERE id_commande = p_id_commande
    LIMIT 1;

    IF v_total_deja_facture IS NULL OR v_total_deja_facture <= 0 THEN
        LEAVE proc_appliquer;
    END IF;

    UPDATE commande
    SET est_gratuite = TRUE,
        prix_facture = 0
    WHERE id_commande = p_id_commande
    AND est_gratuite = FALSE;

    UPDATE client
    SET solde = solde + v_total_deja_facture
    WHERE id_client = v_id_client;

    INSERT INTO compte_transaction(id_client, type_transaction, montant, commentaire)
    VALUES (v_id_client, 'remboursement_retard', v_total_deja_facture, 'Livraison > 30 min, commande gratuite');
END$$

DROP TRIGGER IF EXISTS after_commande_retard_gratuite$$
DROP PROCEDURE IF EXISTS fn_passer_commande$$

CREATE PROCEDURE fn_passer_commande(
    IN p_id_client BIGINT,
    IN p_id_livreur BIGINT,
    IN p_id_pizza BIGINT,
    IN p_code_taille VARCHAR(20),
    IN p_quantite INT,
    IN p_minutes_livraison INT,
    OUT p_id_commande BIGINT
)
MODIFIES SQL DATA
proc_passer: BEGIN
    DECLARE v_statut VARCHAR(20);
    DECLARE msg VARCHAR(255);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_id_commande = NULL;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF p_quantite IS NULL OR p_quantite < 1 THEN
        SET p_quantite = 1;
    END IF;

    INSERT INTO commande(id_client, id_livreur, date_livraison_prevue, statut, id_pizza, code_taille, quantite)
    VALUES (p_id_client, p_id_livreur, DATE_ADD(NOW(), INTERVAL p_minutes_livraison MINUTE), 'cree', p_id_pizza, p_code_taille, p_quantite);

    SET p_id_commande = LAST_INSERT_ID();

    SELECT statut INTO v_statut FROM commande WHERE id_commande = p_id_commande FOR UPDATE;
    IF v_statut = 'refusee' THEN
        ROLLBACK;
        SET p_id_commande = NULL;
        LEAVE proc_passer;
    END IF;

    UPDATE commande
    SET statut = 'preparee'
    WHERE id_commande = p_id_commande;

    COMMIT;
END$$

DROP PROCEDURE IF EXISTS fn_prendre_en_charge_commande$$

CREATE PROCEDURE fn_prendre_en_charge_commande(
    IN p_id_commande BIGINT,
    IN p_id_livreur BIGINT
)
MODIFIES SQL DATA
BEGIN
    DECLARE v_current_status VARCHAR(20);
    DECLARE v_assigned_livreur BIGINT;

    SELECT statut, id_livreur
    INTO v_current_status, v_assigned_livreur
    FROM commande
    WHERE id_commande = p_id_commande
    FOR UPDATE;

    IF v_current_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Commande introuvable';
    END IF;

    IF v_assigned_livreur <> p_id_livreur THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Livreur non assigne a cette commande';
    END IF;

    IF v_current_status <> 'preparee' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La commande doit etre en etat preparee';
    END IF;

    UPDATE commande
    SET statut = 'en_livraison',
        date_prise_en_charge = NOW()
    WHERE id_commande = p_id_commande;
END$$

DROP PROCEDURE IF EXISTS fn_livrer_commande$$

CREATE PROCEDURE fn_livrer_commande(
    IN p_id_commande BIGINT,
    IN p_id_livreur BIGINT
)
MODIFIES SQL DATA
BEGIN
    DECLARE v_current_status VARCHAR(20);
    DECLARE v_assigned_livreur BIGINT;

    SELECT statut, id_livreur
    INTO v_current_status, v_assigned_livreur
    FROM commande
    WHERE id_commande = p_id_commande
    FOR UPDATE;

    IF v_current_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Commande introuvable';
    END IF;

    IF v_assigned_livreur <> p_id_livreur THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Livreur non assigne a cette commande';
    END IF;

    IF v_current_status <> 'en_livraison' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La commande doit etre en etat en_livraison';
    END IF;

    UPDATE commande
    SET statut = 'livree',
        date_livraison_reelle = NOW()
    WHERE id_commande = p_id_commande;
END$$

DELIMITER ;