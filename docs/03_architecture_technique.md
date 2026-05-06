# Architecture Technique

## Objectif

Ce projet de pizzeria prepayee est organise autour de 4 couches:

1. la conception des donnees dans Looping,
2. la logique metier dans PostgreSQL,
3. l'acces a la base via JDBC,
4. l'interface utilisateur en Swing.

## Role de Looping

Looping sert a poser le modele entite-association puis le modele relationnel.

Le modele couvre:

- client,
- pizza,
- ingredient,
- taille,
- vehicule,
- livreur,
- commande,
- commande_ligne,
- compte_transaction,
- refus_commande.

## Role des procedures et triggers

La base PostgreSQL porte la logique metier principale:

- fn_recharger_compte: recharge du compte client,
- fn_passer_commande: creation d'une commande prepayee,
- fn_calc_prix_ligne: calcul du prix selon la taille et la gratuitite,
- trigger sur commande_ligne: calcul automatique du prix facture,
- trigger sur commande: remboursement automatique en cas de retard superieur a 30 minutes.

## Role de JDBC

La couche Java utilise JDBC pour appeler les fonctions SQL au lieu de recalculer les regles metier cote Java.

Cela permet de centraliser les regles dans la base et de garder la couche Java simple.

## Role de Swing

L'interface Swing expose les actions principales:

- recharger un compte,
- passer une commande,
- consulter le solde client.

## Flux global

1. Le modele est d'abord defini dans Looping.
2. Le schema SQL est cree dans PostgreSQL.
3. Les procedures, fonctions et triggers appliquent les regles metier.
4. Java JDBC appelle la base.
5. Swing affiche les actions disponibles a l'utilisateur.
