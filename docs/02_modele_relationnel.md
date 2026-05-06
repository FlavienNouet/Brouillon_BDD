# Modele Relationnel

## Tables

- client(
  id_client PK,
  nom,
  email UNIQUE,
  date_abonnement,
  solde CHECK >= 0
)

- pizza(
  id_pizza PK,
  nom UNIQUE,
  prix_base CHECK > 0
)

- ingredient(
  id_ingredient PK,
  nom UNIQUE
)

- pizza_ingredient(
  id_pizza FK -> pizza,
  id_ingredient FK -> ingredient,
  PK(id_pizza, id_ingredient)
)

- taille(
  code_taille PK,
  coefficient_prix CHECK > 0
)

- vehicule(
  id_vehicule PK,
  type_vehicule CHECK IN (voiture, moto, aucun),
  immatriculation UNIQUE NULL,
  actif
)

- livreur(
  id_livreur PK,
  nom,
  id_vehicule FK -> vehicule NULL
)

- commande(
  id_commande PK,
  id_client FK -> client,
  id_livreur FK -> livreur,
  date_commande,
  date_livraison_prevue,
  date_livraison_reelle NULL,
  statut CHECK IN (cree, preparee, livree, refusee)
)

- commande_ligne(
  id_ligne PK,
  id_commande FK -> commande,
  id_pizza FK -> pizza,
  code_taille FK -> taille,
  quantite CHECK > 0,
  prix_unitaire_base CHECK > 0,
  prix_facture CHECK >= 0,
  est_gratuite
)

- compte_transaction(
  id_transaction PK,
  id_client FK -> client,
  type_transaction CHECK IN (recharge, debit_commande, remboursement_retard),
  montant CHECK > 0,
  date_transaction,
  commentaire
)

- refus_commande(
  id_refus PK,
  id_client FK -> client,
  date_refus,
  montant_requis,
  solde_disponible,
  motif
)

## Index utiles

- idx_commande_client_date(id_client, date_commande)
- idx_commande_ligne_pizza(id_pizza)
- idx_commande_livreur(id_livreur)
- idx_transaction_client_date(id_client, date_transaction)
