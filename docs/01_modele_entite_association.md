# Modele Entite-Association

## Entites

- Client
  - id_client (PK)
  - nom
  - email
  - date_abonnement
  - solde

- Pizza
  - id_pizza (PK)
  - nom
  - prix_base

- Ingredient
  - id_ingredient (PK)
  - nom

- Taille
  - code_taille (PK): naine, humaine, ogresse
  - coefficient_prix

- Vehicule
  - id_vehicule (PK)
  - type_vehicule: voiture, moto, aucun
  - immatriculation
  - actif

- Livreur
  - id_livreur (PK)
  - nom
  - id_vehicule (FK nullable)

- Commande
  - id_commande (PK)
  - id_client (FK)
  - id_livreur (FK)
  - date_commande
  - date_livraison_prevue
  - date_livraison_reelle
  - statut

- LigneCommande
  - id_ligne (PK)
  - id_commande (FK)
  - id_pizza (FK)
  - code_taille (FK)
  - quantite
  - prix_unitaire_base
  - prix_facture
  - est_gratuite

- CompteTransaction
  - id_transaction (PK)
  - id_client (FK)
  - type_transaction: recharge, debit_commande, remboursement_retard
  - montant
  - date_transaction
  - commentaire

- RefusCommande
  - id_refus (PK)
  - id_client (FK)
  - date_refus
  - montant_requis
  - solde_disponible
  - motif

## Associations (cardinalites)

- Client 1,N Commande
- Commande 1,N LigneCommande
- Pizza 1,N LigneCommande
- Taille 1,N LigneCommande
- Pizza N,N Ingredient (via PizzaIngredient)
- Livreur 1,N Commande
- Vehicule 1,N Livreur (vehicule nullable si aucun)
- Client 1,N CompteTransaction
- Client 1,N RefusCommande

## Regles metier principales

- Prix par taille:
  - naine: prix_base * (2/3)
  - humaine: prix_base * 1
  - ogresse: prix_base * (4/3)
- Vente prepayee: verifier le solde avant validation de commande.
- Pizza gratuite toutes les 10 pizzas d un client.
- Si livraison en plus de 30 minutes: commande gratuite (remboursement).
- Refuser la commande si solde insuffisant (et journaliser le refus).
