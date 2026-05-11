-- Requetes de pilotage (MySQL 8+)

-- 1) Solde du compte + facturation par client
SELECT c.id_client, c.nom, c.solde,
       COALESCE(SUM(CASE WHEN t.type_transaction = 'debit_commande' THEN t.montant END), 0) AS total_facture,
       COALESCE(SUM(CASE WHEN t.type_transaction = 'recharge' THEN t.montant END), 0) AS total_recharge,
       COALESCE(SUM(CASE WHEN t.type_transaction = 'remboursement_retard' THEN t.montant END), 0) AS total_rembourse
FROM client c
LEFT JOIN compte_transaction t ON t.id_client = c.id_client
GROUP BY c.id_client, c.nom, c.solde
ORDER BY c.id_client;

-- 2) Chiffre d'affaires (hors pizzas gratuites)
SELECT ROUND(COALESCE(SUM(cl.prix_facture), 0), 2) AS chiffre_affaires
FROM commande_ligne cl;

-- 3) Refus de commande pour manque d'argent
SELECT rc.*, c.nom
FROM refus_commande rc
JOIN client c ON c.id_client = rc.id_client
ORDER BY rc.date_refus DESC;

-- 4) Meilleur client (depense totale)
SELECT c.id_client, c.nom, ROUND(SUM(cl.prix_facture), 2) AS depense_totale
FROM client c
JOIN commande co ON co.id_client = c.id_client
JOIN commande_ligne cl ON cl.id_commande = co.id_commande
GROUP BY c.id_client, c.nom
ORDER BY depense_totale DESC
LIMIT 1;

-- 5) Mauvais livreur (retard moyen le plus eleve) - minutes
SELECT l.id_livreur, l.nom,
       v.type_vehicule,
       ROUND(AVG(GREATEST(TIMESTAMPDIFF(MINUTE, co.date_livraison_prevue, co.date_livraison_reelle), 0)), 2) AS retard_moyen_minutes
FROM livreur l
LEFT JOIN vehicule v ON v.id_vehicule = l.id_vehicule
JOIN commande co ON co.id_livreur = l.id_livreur
WHERE co.date_livraison_reelle IS NOT NULL
GROUP BY l.id_livreur, l.nom, v.type_vehicule
ORDER BY retard_moyen_minutes DESC
LIMIT 1;

-- 6) Pizza la plus et la moins demandee
SELECT p.nom, COUNT(*) AS nb_lignes
FROM commande_ligne cl
JOIN pizza p ON p.id_pizza = cl.id_pizza
GROUP BY p.nom
ORDER BY nb_lignes DESC
LIMIT 1;

SELECT p.nom, COUNT(*) AS nb_lignes
FROM commande_ligne cl
JOIN pizza p ON p.id_pizza = cl.id_pizza
GROUP BY p.nom
ORDER BY nb_lignes ASC
LIMIT 1;

-- 7) Ingredient favori (apparition dans pizzas vendues)
SELECT i.nom, COUNT(*) AS occurrences
FROM commande_ligne cl
JOIN pizza_ingredient pi ON pi.id_pizza = cl.id_pizza
JOIN ingredient i ON i.id_ingredient = pi.id_ingredient
GROUP BY i.nom
ORDER BY occurrences DESC
LIMIT 1;

-- 8) Vehicule jamais utilise
SELECT v.*
FROM vehicule v
LEFT JOIN livreur l ON l.id_vehicule = v.id_vehicule
LEFT JOIN commande c ON c.id_livreur = l.id_livreur
GROUP BY v.id_vehicule
HAVING COUNT(c.id_commande) = 0;

-- 9) Nombre de commandes par client
SELECT c.id_client, c.nom, COUNT(co.id_commande) AS nb_commandes
FROM client c
LEFT JOIN commande co ON co.id_client = c.id_client
GROUP BY c.id_client, c.nom
ORDER BY nb_commandes DESC;

-- 10) Moyenne de commandes par client
SELECT ROUND(AVG(nb), 2) AS moyenne_commandes
FROM (
    SELECT COUNT(*) AS nb
    FROM commande
    GROUP BY id_client
) x;

-- 11) Clients au-dessus de la moyenne
WITH stats AS (
    SELECT id_client, COUNT(*) AS nb
    FROM commande
    GROUP BY id_client
),
moy AS (
    SELECT AVG(nb) AS moyenne FROM stats
)
SELECT c.id_client, c.nom, s.nb
FROM stats s
JOIN moy m ON s.nb > m.moyenne
JOIN client c ON c.id_client = s.id_client
ORDER BY s.nb DESC;
