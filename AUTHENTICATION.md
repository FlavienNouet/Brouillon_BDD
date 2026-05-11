# Authentification et Utilisateurs

## Démarrage

L'application démarre maintenant avec un écran de connexion (LoginFrame).

## Identifiants de test

Avant de utiliser l'application, il faut initialiser les utilisateurs de test dans la base de données.

### 1. Créer la base et les tables

Exécute d'abord les scripts :
1. `sql/01_create_schema.sql` (crée la table utilisateur)
2. `sql/02_business_logic.sql`
3. `sql/03_seed_data.sql`

### 2. Initialiser les utilisateurs

L'application propose deux options :

#### Option A : S'inscrire comme CLIENT

Au démarrage, clique sur **S'inscrire** et crée un compte CLIENT :
- Identifiant: `client1`
- Mot de passe: `password123`

L'application crée automatiquement un utilisateur avec le rôle `CLIENT`.

#### Option B : Initialiser manuellement via SQL

Ouvre une console MySQL et exécute les commandes suivantes pour insérer les utilisateurs de test.

```sql
INSERT INTO utilisateur (login, password_hash, role, actif)
VALUES ('admin', '$2a$10$yl2tI5ss3DpDVTeCnaUMZ.Xgwyd/EMwrl2n3lKUXPVuiI2BMLlrBy', 'ADMIN', TRUE);

INSERT INTO utilisateur (login, password_hash, role, id_client, actif)
VALUES ('client1', '$2a$10$aIYmUDtXP0DtyKmO1W.OI.oXcG.rrqq2PfkpvQKgopL.YSn.ANX2.', 'CLIENT', 1, TRUE);

INSERT INTO utilisateur (login, password_hash, role, id_livreur, actif)
VALUES ('livreur1', '$2a$10$/Xom55qfi67R.ycdyG8zYuUZaMHOz8bmUlVlX1EoDApz/5.JW68Z6', 'LIVREUR', 1, TRUE);
```

## Rôles et interfaces

### CLIENT
- **Onglet "Commande"** : Ajouter des pizzas, gérer le panier, valider une commande (livreur sélectionné automatiquement)
- **Onglet "Mon compte"** : Voir solde, recharger le compte

### LIVREUR
- **Onglet "Commandes à livrer"** : Voir les commandes assignées et leur statut

### ADMIN
- **Onglet "Commande"** : Interface complète pour gérer les commandes (sélection client/livreur)
- **Onglet "Mon compte"** : Recharge des comptes clients
- **Onglet "Tableau de bord"** : Statistiques générales
- **Onglet "Gestion utilisateurs"** : Liste des utilisateurs et leurs rôles

## Générer des hashes BCrypt

Si tu veux créer d'autres utilisateurs, tu dois générer des hashes BCrypt. 

### Approche simple (interactif) :

La prochaine fois que tu lances l'app, tu peux créer un compte directement depuis l'écran d'inscription. Les mots de passe sont hachés automatiquement.

#### Approche avancée (si tu as besoin de créer des utilisateurs via SQL) :

Crée une classe utilitaire Java pour générer les hashes :

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Utilisation: java HashGenerator <password>");
            return;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(args[0]);
        System.out.println("Hash pour '" + args[0] + "': " + hash);
    }
}
```

Lance-la avec :
```bash
cd java
..\tools\apache-maven-3.9.9\bin\mvn.cmd clean compile exec:java -Dexec.mainClass="com.projetbdd.HashGenerator" -Dexec.args="mon_mot_de_passe"
```

## Authentification

- **Connexion** : Saisis login et mot de passe
- **Inscription CLIENT** : Crée un compte automatiquement en tant que CLIENT (l'admin peut créer d'autres rôles manuellement)
- **Déconnexion** : Bouton "Déconnexion" en haut à droite

## Notes de sécurité

- Les mots de passe sont hachés avec BCrypt (10 rounds)
- L'application ne stocke jamais le mot de passe en clair
- La session est stockée en mémoire (Session singleton)
