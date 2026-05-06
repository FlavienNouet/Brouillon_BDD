-- Jeu de donnees et boucles de generation

INSERT INTO taille(code_taille, coefficient_prix) VALUES
('naine', 0.6667),
('humaine', 1.0000),
('ogresse', 1.3333);

INSERT INTO pizza(nom, prix_base) VALUES
('Margherita', 10.00),
('Regina', 12.50),
('4 Fromages', 13.00),
('Pepperoni', 14.00),
('Vegetarienne', 11.50);

INSERT INTO ingredient(nom) VALUES
('tomate'), ('mozzarella'), ('jambon'), ('champignon'), ('chevre'),
('emmental'), ('gorgonzola'), ('pepperoni'), ('olive'), ('poivron');

INSERT INTO pizza_ingredient(id_pizza, id_ingredient)
SELECT p.id_pizza, i.id_ingredient
FROM pizza p
JOIN ingredient i ON (
    (p.nom = 'Margherita' AND i.nom IN ('tomate', 'mozzarella')) OR
    (p.nom = 'Regina' AND i.nom IN ('tomate', 'mozzarella', 'jambon', 'champignon')) OR
    (p.nom = '4 Fromages' AND i.nom IN ('mozzarella', 'chevre', 'emmental', 'gorgonzola')) OR
    (p.nom = 'Pepperoni' AND i.nom IN ('tomate', 'mozzarella', 'pepperoni')) OR
    (p.nom = 'Vegetarienne' AND i.nom IN ('tomate', 'mozzarella', 'olive', 'poivron', 'champignon'))
);

INSERT INTO vehicule(type_vehicule, immatriculation) VALUES
('voiture', 'AA-101-AA'),
('moto', 'BB-202-BB'),
('aucun', NULL),
('voiture', 'CC-303-CC');

INSERT INTO livreur(nom, id_vehicule) VALUES
('Leo', 1),
('Mina', 2),
('Nox', 3),
('Ryu', 4);

-- Looping de creation de clients
DO $$
DECLARE
    i INT;
BEGIN
    FOR i IN 1..12 LOOP
        INSERT INTO client(nom, email, solde, date_abonnement)
        VALUES (
            'Client_' || i,
            'client' || i || '@mail.com',
            50 + (i * 10),
            CURRENT_DATE - (i || ' days')::INTERVAL
        );
    END LOOP;
END;
$$;

-- Quelques recharges
SELECT fn_recharger_compte(1, 30);
SELECT fn_recharger_compte(2, 20);

-- Commandes test (certaines > 30 min pour activer gratuite)
SELECT fn_passer_commande(
    1,
    1,
    '[{"pizza_id":1,"taille":"humaine","quantite":2},{"pizza_id":3,"taille":"ogresse","quantite":1}]'::jsonb,
    25
);

SELECT fn_passer_commande(
    2,
    2,
    '[{"pizza_id":2,"taille":"naine","quantite":3}]'::jsonb,
    45
);

SELECT fn_passer_commande(
    3,
    3,
    '[{"pizza_id":4,"taille":"humaine","quantite":5}]'::jsonb,
    18
);

-- Boucle pour injecter plusieurs commandes et tester la pizza gratuite toutes les 10
DO $$
DECLARE
    i INT;
    c BIGINT;
BEGIN
    FOR i IN 1..15 LOOP
        c := ((i - 1) % 5) + 1;
        PERFORM fn_passer_commande(
            c,
            ((i - 1) % 4) + 1,
            '[{"pizza_id":1,"taille":"humaine","quantite":1}]'::jsonb,
            CASE WHEN i % 4 = 0 THEN 40 ELSE 20 END
        );
    END LOOP;
END;
$$;
