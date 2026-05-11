package com.projetbdd;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Utilisation: java HashGenerator <password>");
            System.out.println("\nExemple:");
            System.out.println("  java HashGenerator password123");
            System.out.println("\nCela affichera le hash BCrypt du mot de passe.");
            return;
        }

        String password = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hash = encoder.encode(password);

        System.out.println("\n=== Hash BCrypt ===");
        System.out.println("Mot de passe : " + password);
        System.out.println("Hash        : " + hash);
        System.out.println("\nUtilise ce hash dans une requête SQL INSERT pour créer un utilisateur.");
    }
}
