-- PostgreSQL 14+
-- Fonctions, triggers et procedures metier

CREATE OR REPLACE FUNCTION fn_calc_prix_ligne(
    p_prix_base NUMERIC,
    p_code_taille VARCHAR,
    p_quantite INT,
    p_est_gratuite BOOLEAN
) RETURNS NUMERIC AS $$
DECLARE
    v_coef NUMERIC;
BEGIN
    IF p_est_gratuite THEN
        RETURN 0;
    END IF;

    SELECT coefficient_prix INTO v_coef
    FROM taille
    WHERE code_taille = p_code_taille;

    IF v_coef IS NULL THEN
        RAISE EXCEPTION 'Taille inconnue: %', p_code_taille;
    END IF;

    RETURN ROUND(p_prix_base * v_coef * p_quantite, 2);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_commande_ligne_calc_prix()
RETURNS TRIGGER AS $$
BEGIN
    NEW.prix_facture := fn_calc_prix_ligne(
        NEW.prix_unitaire_base,
        NEW.code_taille,
        NEW.quantite,
        NEW.est_gratuite
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS before_commande_ligne_calc_prix ON commande_ligne;
CREATE TRIGGER before_commande_ligne_calc_prix
BEFORE INSERT OR UPDATE ON commande_ligne
FOR EACH ROW
EXECUTE FUNCTION trg_commande_ligne_calc_prix();

CREATE OR REPLACE FUNCTION fn_recharger_compte(
    p_id_client BIGINT,
    p_montant NUMERIC
) RETURNS VOID AS $$
BEGIN
    IF p_montant <= 0 THEN
        RAISE EXCEPTION 'Le montant de recharge doit etre > 0';
    END IF;

    UPDATE client
    SET solde = solde + p_montant
    WHERE id_client = p_id_client;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Client introuvable: %', p_id_client;
    END IF;

    INSERT INTO compte_transaction(id_client, type_transaction, montant, commentaire)
    VALUES (p_id_client, 'recharge', p_montant, 'Recharge compte prepaye');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_appliquer_gratuite_retard(
    p_id_commande BIGINT
) RETURNS VOID AS $$
DECLARE
    v_id_client BIGINT;
    v_total_deja_facture NUMERIC;
BEGIN
    SELECT c.id_client,
           COALESCE(SUM(cl.prix_facture), 0)
      INTO v_id_client, v_total_deja_facture
    FROM commande c
    JOIN commande_ligne cl ON cl.id_commande = c.id_commande
    WHERE c.id_commande = p_id_commande
    GROUP BY c.id_client;

    IF v_total_deja_facture <= 0 THEN
        RETURN;
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
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_commande_retard_gratuite()
RETURNS TRIGGER AS $$
DECLARE
    v_minutes_retard INT;
BEGIN
    IF NEW.date_livraison_reelle IS NULL THEN
        RETURN NEW;
    END IF;

    v_minutes_retard := FLOOR(EXTRACT(EPOCH FROM (NEW.date_livraison_reelle - NEW.date_commande)) / 60);

    IF v_minutes_retard > 30 THEN
        PERFORM fn_appliquer_gratuite_retard(NEW.id_commande);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS after_commande_retard_gratuite ON commande;
CREATE TRIGGER after_commande_retard_gratuite
AFTER UPDATE OF date_livraison_reelle ON commande
FOR EACH ROW
WHEN (NEW.date_livraison_reelle IS NOT NULL)
EXECUTE FUNCTION trg_commande_retard_gratuite();

CREATE OR REPLACE FUNCTION fn_passer_commande(
    p_id_client BIGINT,
    p_id_livreur BIGINT,
    p_lignes JSONB,
    p_minutes_livraison INT DEFAULT 20
) RETURNS BIGINT AS $$
DECLARE
    v_id_commande BIGINT;
    v_solde NUMERIC;
    v_total NUMERIC := 0;
    v_prix_base NUMERIC;
    v_id_pizza BIGINT;
    v_taille VARCHAR(20);
    v_qte INT;
    v_nb_pizzas_historique INT;
    v_compteur INT;
    v_est_gratuite BOOLEAN;
    v_cout_ligne NUMERIC;
    v_line JSONB;
BEGIN
    SELECT solde INTO v_solde
    FROM client
    WHERE id_client = p_id_client
    FOR UPDATE;

    IF v_solde IS NULL THEN
        RAISE EXCEPTION 'Client introuvable: %', p_id_client;
    END IF;

    IF p_lignes IS NULL OR jsonb_array_length(p_lignes) = 0 THEN
        RAISE EXCEPTION 'La commande doit contenir au moins une pizza';
    END IF;

    INSERT INTO commande(id_client, id_livreur, date_livraison_prevue, statut)
    VALUES (p_id_client, p_id_livreur, CURRENT_TIMESTAMP + INTERVAL '30 minutes', 'preparee')
    RETURNING id_commande INTO v_id_commande;

    SELECT COALESCE(SUM(cl.quantite), 0)
      INTO v_nb_pizzas_historique
    FROM commande c
    JOIN commande_ligne cl ON cl.id_commande = c.id_commande
    WHERE c.id_client = p_id_client
      AND c.id_commande <> v_id_commande;

    v_compteur := v_nb_pizzas_historique;

    FOR v_line IN SELECT * FROM jsonb_array_elements(p_lignes)
    LOOP
        v_id_pizza := (v_line ->> 'pizza_id')::BIGINT;
        v_taille := (v_line ->> 'taille')::VARCHAR(20);
        v_qte := (v_line ->> 'quantite')::INT;

        SELECT prix_base INTO v_prix_base
        FROM pizza
        WHERE id_pizza = v_id_pizza;

        IF v_prix_base IS NULL THEN
            RAISE EXCEPTION 'Pizza introuvable: %', v_id_pizza;
        END IF;

        IF v_qte <= 0 THEN
            RAISE EXCEPTION 'Quantite invalide pour pizza %', v_id_pizza;
        END IF;

        FOR i IN 1..v_qte
        LOOP
            v_compteur := v_compteur + 1;
            v_est_gratuite := (v_compteur % 10 = 0);

            v_cout_ligne := fn_calc_prix_ligne(v_prix_base, v_taille, 1, v_est_gratuite);
            v_total := v_total + v_cout_ligne;

            INSERT INTO commande_ligne(
                id_commande, id_pizza, code_taille, quantite,
                prix_unitaire_base, est_gratuite
            ) VALUES (
                v_id_commande, v_id_pizza, v_taille, 1,
                v_prix_base, v_est_gratuite
            );
        END LOOP;
    END LOOP;

    IF v_total > v_solde THEN
        INSERT INTO refus_commande(id_client, montant_requis, solde_disponible, motif)
        VALUES (p_id_client, v_total, v_solde, 'Solde insuffisant');

        DELETE FROM commande WHERE id_commande = v_id_commande;

        RETURN NULL;
    END IF;

    UPDATE client
    SET solde = solde - v_total
    WHERE id_client = p_id_client;

    INSERT INTO compte_transaction(id_client, type_transaction, montant, commentaire)
    VALUES (p_id_client, 'debit_commande', v_total, 'Debitage commande ' || v_id_commande);

    UPDATE commande
    SET date_livraison_reelle = date_commande + make_interval(mins => p_minutes_livraison),
        statut = 'livree'
    WHERE id_commande = v_id_commande;

    RETURN v_id_commande;
END;
$$ LANGUAGE plpgsql;
