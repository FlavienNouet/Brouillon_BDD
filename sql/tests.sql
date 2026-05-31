INSERT INTO client (nom, email, solde) VALUES ('Alice', 'alice@example.com', 100.00)
ON DUPLICATE KEY UPDATE solde = VALUES(solde), nom = VALUES(nom);
INSERT INTO client (nom, email, solde) VALUES ('Bob', 'bob@example.com', 5.00)
ON DUPLICATE KEY UPDATE solde = VALUES(solde), nom = VALUES(nom);

INSERT INTO pizza (nom, prix_base) VALUES ('Margherita', 8.00)
ON DUPLICATE KEY UPDATE prix_base = VALUES(prix_base);
INSERT INTO pizza (nom, prix_base) VALUES ('Pepperoni', 9.50)
ON DUPLICATE KEY UPDATE prix_base = VALUES(prix_base);

INSERT INTO taille (code_taille, coefficient_prix) VALUES ('S', 0.9)
ON DUPLICATE KEY UPDATE coefficient_prix = VALUES(coefficient_prix);
INSERT INTO taille (code_taille, coefficient_prix) VALUES ('M', 1.0)
ON DUPLICATE KEY UPDATE coefficient_prix = VALUES(coefficient_prix);
INSERT INTO taille (code_taille, coefficient_prix) VALUES ('L', 1.2)
ON DUPLICATE KEY UPDATE coefficient_prix = VALUES(coefficient_prix);

INSERT INTO ingredient (nom) VALUES ('Tomate')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);
INSERT INTO ingredient (nom) VALUES ('Mozzarella')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);
INSERT INTO ingredient (nom) VALUES ('Peperoni')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

INSERT IGNORE INTO pizza_ingredient (id_pizza, id_ingredient)
SELECT p.id_pizza, i.id_ingredient
FROM pizza p
JOIN ingredient i ON (
  (p.nom = 'Margherita' AND i.nom IN ('Tomate', 'Mozzarella')) OR
  (p.nom = 'Pepperoni' AND i.nom IN ('Tomate', 'Peperoni'))
);

INSERT INTO vehicule (type_vehicule, immatriculation, actif) VALUES ('voiture','ABC-123',TRUE)
ON DUPLICATE KEY UPDATE type_vehicule = VALUES(type_vehicule), actif = VALUES(actif);
INSERT INTO vehicule (type_vehicule, immatriculation, actif) VALUES ('moto','MOTO-1',TRUE)
ON DUPLICATE KEY UPDATE type_vehicule = VALUES(type_vehicule), actif = VALUES(actif);

INSERT INTO livreur (nom) VALUES ('Jean')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);
INSERT INTO livreur (nom) VALUES ('Pierre')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

INSERT INTO commande (id_client, id_livreur, date_commande, date_livraison_prevue, statut, id_vehicule, id_pizza, code_taille, quantite)
VALUES (1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE), 'cree', 1, 1, 'M', 1);

SELECT p.nom AS pizza, p.prix_base, t.coefficient_prix, p.prix_base * t.coefficient_prix AS prix_calcule,
       GROUP_CONCAT(i.nom SEPARATOR ', ') AS ingredients
FROM pizza p
JOIN pizza_ingredient pi ON p.id_pizza = pi.id_pizza
JOIN ingredient i ON pi.id_ingredient = i.id_ingredient
JOIN taille t ON t.code_taille = 'M'
GROUP BY p.id_pizza, t.coefficient_prix;

SELECT c.id_commande, lv.nom AS livreur, v.type_vehicule, cl.nom AS client, c.date_commande,
       CASE WHEN c.date_livraison_reelle IS NULL OR c.date_prise_en_charge IS NULL THEN NULL
            ELSE TIMESTAMPDIFF(MINUTE, c.date_prise_en_charge, c.date_livraison_reelle)
       END AS retard_min,
       p.nom AS pizza, p.prix_base
FROM commande c
JOIN livreur lv ON c.id_livreur = lv.id_livreur
LEFT JOIN vehicule v ON c.id_vehicule = v.id_vehicule
JOIN client cl ON c.id_client = cl.id_client
JOIN pizza p ON c.id_pizza = p.id_pizza;

SELECT * FROM vehicule v WHERE NOT EXISTS (SELECT 1 FROM utilisation_vehicule u WHERE u.id_vehicule = v.id_vehicule);

SELECT id_client, COUNT(*) AS nb_commandes FROM commande GROUP BY id_client;

SELECT AVG(nb) AS moyenne_commandes_par_client FROM (SELECT COUNT(*) AS nb FROM commande GROUP BY id_client) x;

SELECT id_client, COUNT(*) AS nb FROM commande GROUP BY id_client HAVING COUNT(*) > (
  SELECT AVG(nb) FROM (SELECT COUNT(*) AS nb FROM commande GROUP BY id_client) y
);

SELECT c.id_client, c.nom, COALESCE(SUM(com.prix_facture),0) AS ca
FROM client c
LEFT JOIN commande com ON com.id_client = c.id_client
GROUP BY c.id_client ORDER BY ca DESC LIMIT 1;

SELECT l.id_livreur, l.nom, COUNT(*) AS retards
FROM livreur l
JOIN commande com ON com.id_livreur = l.id_livreur
WHERE com.date_livraison_reelle IS NOT NULL AND com.date_prise_en_charge IS NOT NULL
  AND TIMESTAMPDIFF(MINUTE, com.date_prise_en_charge, com.date_livraison_reelle) > 30
GROUP BY l.id_livreur ORDER BY retards DESC LIMIT 1;

SELECT p.id_pizza, p.nom, COUNT(*) AS nb
FROM pizza p
JOIN commande com ON com.id_pizza = p.id_pizza
GROUP BY p.id_pizza ORDER BY nb DESC LIMIT 1;

SELECT i.id_ingredient, i.nom, COUNT(*) AS nb
FROM ingredient i
JOIN pizza_ingredient pi ON pi.id_ingredient = i.id_ingredient
JOIN commande com ON com.id_pizza = pi.id_pizza
GROUP BY i.id_ingredient ORDER BY nb DESC LIMIT 1;
