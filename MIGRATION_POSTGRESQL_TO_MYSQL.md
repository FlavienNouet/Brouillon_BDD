# Migration PostgreSQL → MySQL

## Changements effectues

Ce projet a été converti de PostgreSQL à MySQL. Voici ce qui a changé:

### 1. Driver JDBC (pom.xml)

**Avant (PostgreSQL):**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.4</version>
</dependency>
```

**Après (MySQL):**
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

### 2. Configuration JDBC (DatabaseConfig.java)

**Avant (PostgreSQL):**
```
jdbc:postgresql://localhost:5432/projet_bdd
```

**Après (MySQL):**
```
jdbc:mysql://localhost:3306/projet_bdd?useSSL=false&serverTimezone=UTC
```

### 3. Scripts SQL

Les scripts SQL PostgreSQL ont été convertis en scripts MySQL:

- [sql/01_create_schema_mysql.sql](../sql/01_create_schema_mysql.sql): schema et tables MySQL
- [sql/02_business_logic_mysql.sql](../sql/02_business_logic_mysql.sql): procédures et triggers MySQL
- [sql/03_seed_data_mysql.sql](../sql/03_seed_data_mysql.sql): données de test MySQL

**Conversions principales:**
- `BIGSERIAL` → `BIGINT AUTO_INCREMENT`
- `NUMERIC(10,2)` → `DECIMAL(10,2)`
- Procédures PL/pgSQL → Procédures MySQL avec `BEGIN...END` et `DELIMITER`
- Fonctions SQL avec `SELECT` → Procédures avec `CALL` et paramètres `OUT`
- `RAISE EXCEPTION` → `SIGNAL SQLSTATE`
- `JSONB` → `JSON` (avec opérateurs `JSON_EXTRACT`, `JSON_LENGTH`)
- `EXTRACT(EPOCH FROM ...)` → `TIMESTAMPDIFF(MINUTE, ...)`

### 4. Couche Java (PizzaService.java)

**Avant (PostgreSQL):**
```java
// Fonction SQL appelée via SELECT
final String sql = "SELECT fn_passer_commande(?, ?, ?::jsonb, ?)";
PreparedStatement ps = cn.prepareStatement(sql);
```

**Après (MySQL):**
```java
// Procédure SQL appelée via CALL avec paramètre OUT
final String sql = "CALL fn_passer_commande(?, ?, ?, ?, ?)";
CallableStatement cs = cn.prepareCall(sql);
cs.registerOutParameter(5, java.sql.Types.BIGINT);
```

## Utilisation

### Prerequis

- MySQL 8.0+
- Java 17+
- Maven 3.9+

### Lancer le projet avec MySQL

1. **Creer la base:**
   ```sql
   CREATE DATABASE projet_bdd;
   ```

2. **Executer les scripts SQL dans cet ordre:**
   ```
   1. sql/01_create_schema_mysql.sql
   2. sql/02_business_logic_mysql.sql
   3. sql/03_seed_data_mysql.sql
   ```

3. **Lancer l'application:**
   ```powershell
   .\run_project.ps1
   ```

### Variables d'environnement

Si ta configuration MySQL est different, configure les variables:
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/projet_bdd"
$env:DB_USER = "root"
$env:DB_PASSWORD = "ton_mot_de_passe"
```

## Notes

- Les requtes de reporting dans `sql/04_reporting_queries.sql` restent valides pour MySQL aussi
- La logique métier (procédures, triggers) est équivalente entre PostgreSQL et MySQL
- L'interface Swing et l'API JDBC restent inchangées (seul le driver et les appels changent)

