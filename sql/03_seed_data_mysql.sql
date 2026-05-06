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
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 12 DO
        INSERT INTO client(nom, email, solde, date_abonnement)
        VALUES (
            CONCAT('Client_', i),
            CONCAT('client', i, '@mail.com'),
            50 + (i * 10),
            DATE_SUB(CURDATE(), INTERVAL i DAY)
        );
        SET i = i + 1;
    END WHILE;
END;

-- Quelques recharges
CALL fn_recharger_compte(1, 30);
CALL fn_recharger_compte(2, 20);
