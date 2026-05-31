# 🍕 Projet BDD — Pizzeria Prépayée

Application complète de gestion d'une pizzeria en mode prépayé, couvrant la conception de la base de données, les règles métier SQL, une couche JDBC et une interface graphique Java Swing.

---

## Sommaire

- [Aperçu](#aperçu)
- [Fonctionnalités](#fonctionnalités)
- [Architecture technique](#architecture-technique)
- [Prérequis](#prérequis)
- [Installation et configuration](#installation-et-configuration)
- [Initialisation de la base de données](#initialisation-de-la-base-de-données)
- [Lancement de l'application](#lancement-de-lapplication)
- [Comptes de démonstration](#comptes-de-démonstration)
- [Profils utilisateurs](#profils-utilisateurs)
- [Règles métier](#règles-métier)
- [Couche JDBC](#couche-jdbc)
- [Scripts SQL](#scripts-sql)
- [Structure du projet](#structure-du-projet)
- [Documentation](#documentation)
- [Dépannage](#dépannage)

---

## Aperçu

Ce projet simule la gestion complète d'une pizzeria prépayée de bout en bout :

- création et authentification d'utilisateurs par rôle (client, livreur, admin) ;
- consultation du catalogue de pizzas et de leurs ingrédients ;
- composition de commandes multi-pizzas avec choix de taille et de quantité ;
- calcul du montant selon la taille de la pizza ;
- vérification et débit du solde prépayé avant validation ;
- suivi et historique des commandes avec export de facture PDF ;
- tableau de bord administratif et espace livreur.

---

## Fonctionnalités

| Domaine | Fonctionnalité |
|---|---|
| Authentification | Connexion et création de compte depuis l'interface |
| Commande | Choix de pizzas, de taille (`naine` / `humaine` / `ogresse`) et de quantité |
| Compte prépayé | Rechargement du solde, vérification avant paiement |
| Suivi | État de la commande en temps réel côté client |
| Historique | Liste des commandes passées + téléchargement de facture PDF |
| Espace livreur | Affichage des commandes à livrer et de leur fiche |
| Espace admin | Tableau de bord, statistiques, gestion des utilisateurs |

---

## Architecture technique

```
MySQL 8          →  base de données principale
SQL              →  schéma, procédures, fonctions, données, reporting
Java + JDBC      →  appels aux fonctions métier de la base
Java Swing       →  interface graphique
iText            →  génération des factures PDF
```

---

## Prérequis

- **Java 17+**
- **MySQL 8+**
- **Windows PowerShell** (si vous utilisez le script `run_project.ps1`)
- **Maven** — non obligatoire, une version locale est incluse dans `tools/apache-maven-3.9.9/`

---

## Installation et configuration

### 1. Variables de connexion

Par défaut, l'application se connecte à :

| Variable | Valeur par défaut |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/projet_bdd?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | *(vide)* |

Pour utiliser des valeurs différentes, définissez les variables d'environnement correspondantes avant de lancer l'application :

```powershell
$env:DB_USER     = "mon_utilisateur"
$env:DB_PASSWORD = "mon_mot_de_passe"
```

---

## Initialisation de la base de données

1. Créer une base MySQL nommée `projet_bdd` :

```sql
CREATE DATABASE projet_bdd;
```

2. Exécuter les scripts SQL dans l'ordre suivant :

```
sql/schema.sql    -- création des tables et contraintes
sql/logic.sql     -- procédures, fonctions et règles métier
sql/seeds.sql     -- données initiales (catalogue pizzas, ingrédients…)
sql/reports.sql   -- requêtes de reporting
sql/tests.sql     -- requêtes de vérification et de contrôle
sql/users.sql     -- comptes et données de démonstration
```

> **Note :** Au premier démarrage, la classe `AuthBootstrap` crée automatiquement la table `utilisateur` si elle est absente et insère les comptes de test de base. Les scripts SQL et le bootstrap sont donc complémentaires.

---

## Lancement de l'application

Depuis la racine du projet :

```powershell
.\run_project.ps1
```

Le script utilise le Maven embarqué et exécute `clean compile exec:java` dans le module `java/`.

L'application démarre sur l'écran de connexion. Le point d'entrée principal est [java/src/main/java/com/projetbdd/App.java](java/src/main/java/com/projetbdd/App.java) : il initialise l'authentification puis ouvre l'interface de login.

---

## Identifiants de connexion

Ces identifiants sont créés automatiquement au premier démarrage si la table `utilisateur` est vide, et ils sont aussi fournis dans [sql/users.sql](sql/users.sql).

| Identifiant | Mot de passe | Rôle |
|---|---|---|
| `admin` | `admin123` | Administrateur |
| `client1` | `client123` | Client |
| `livreur1` | `livreur123` | Livreur |

Ils permettent de tester directement les trois parcours principaux de l'application.

---

## Profils utilisateurs

### CLIENT
- Passer une commande (pizzas, taille, quantité)
- Recharger son compte prépayé
- Consulter son solde
- Suivre l'état de ses commandes
- Accéder à l'historique avec téléchargement de facture PDF

### LIVREUR
- Consulter la liste des commandes à livrer
- Accéder à la fiche de livraison
- Suivre l'état des livraisons

### ADMIN
- Accéder au tableau de bord et aux statistiques
- Consulter toutes les commandes
- Gérer les utilisateurs

---

## Règles métier

### Tarification par taille

| Taille | Multiplicateur |
|---|---|
| `naine` | × 2/3 du prix de base |
| `humaine` | × 1 (prix normal) |
| `ogresse` | × 4/3 du prix de base |

### Modèle prépayé
- Le solde est vérifié avant toute validation de commande.
- Si le solde est insuffisant, la commande est refusée et l'événement est journalisé.

### Fidélité
- 1 pizza offerte toutes les 10 pizzas commandées.

### Livraison
- Si la livraison dépasse **30 minutes**, le client peut demander un remboursement.

---

## Couche JDBC

La couche Java appelle principalement des fonctions SQL via JDBC :

| Fonction SQL | Rôle |
|---|---|
| `fn_recharger_compte(id_client, montant)` | Crédite le compte d'un client |
| `fn_passer_commande(id_client, id_livreur, lignes_json, minutes_livraison)` | Valide et enregistre une commande |

### Format JSON des lignes de commande

```json
[
  { "pizza_id": 1, "taille": "humaine",  "quantite": 2 },
  { "pizza_id": 3, "taille": "ogresse",  "quantite": 1 }
]
```

---

## Scripts SQL

| Fichier | Contenu |
|---|---|
| `sql/schema.sql` | Création des tables et contraintes |
| `sql/logic.sql` | Procédures, fonctions et règles métier |
| `sql/seeds.sql` | Données initiales (pizzas, ingrédients…) |
| `sql/reports.sql` | Requêtes de reporting et tableaux de bord |
| `sql/tests.sql` | Requêtes de vérification et de contrôle |
| `sql/users.sql` | Comptes et données de démonstration |

---

## Structure du projet

```
Projet_BDD/
├── README.md                       # documentation principale du projet
├── run_project.ps1                 # script de lancement PowerShell
├── docs/                           # modelisation et architecture
│   ├── 01_modele_entite_association.md
│   ├── 02_modele_relationnel.md
│   └── 03_architecture_technique.md
├── sql/                            # schema, regles metier, donnees et reporting
│   ├── schema.sql
│   ├── logic.sql
│   ├── seeds.sql
│   ├── reports.sql
│   ├── tests.sql
│   └── users.sql
├── java/                           # module Java Swing + JDBC
│   ├── pom.xml
│   ├── src/main/java/com/projetbdd/
│   │   ├── App.java                # point d'entree
│   │   ├── AuthBootstrap.java      # bootstrap des comptes de base
│   │   ├── Database.java           # acces a la base de donnees
│   │   ├── DatabaseConfig.java     # configuration de connexion
│   │   ├── LoginFrame.java         # ecran de connexion
│   │   ├── MainFrame.java          # interface principale
│   │   ├── PizzaService.java       # requetes et logique metier JDBC
│   │   ├── Session.java            # session utilisateur
│   │   └── UserService.java        # gestion des comptes
│   └── target/                     # fichiers generes a la compilation
└── tools/
    └── apache-maven-3.9.9/         # Maven embarque
```

---

## Documentation

| Document | Description |
|---|---|
| [docs/01_modele_entite_association.md](docs/01_modele_entite_association.md) | Diagramme entité-association |
| [docs/02_modele_relationnel.md](docs/02_modele_relationnel.md) | Modèle relationnel |
| [docs/03_architecture_technique.md](docs/03_architecture_technique.md) | Architecture technique détaillée |

---

## Dépannage

Si l'application ne démarre pas ou ne se connecte pas à la base, vérifiez dans cet ordre :

1. **Variables d'environnement** — `DB_URL`, `DB_USER`, `DB_PASSWORD` sont-elles correctes ?
2. **Base de données** — la base `projet_bdd` existe-t-elle dans votre instance MySQL ?
3. **Scripts SQL** — ont-ils été exécutés dans le bon ordre ?
4. **Comptes de test** — sont-ils bien présents dans la table `utilisateur` ?

> Le projet cible **MySQL 8** et utilise des fonctions spécifiques à ce SGBD. Il n'est pas compatible avec MariaDB ou d'autres moteurs sans adaptation.