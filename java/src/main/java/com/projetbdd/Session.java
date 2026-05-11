package com.projetbdd;

public final class Session {
    private static Session instance;

    private long idUtilisateur;
    private String login;
    private String role;
    private Long idClient;
    private Long idLivreur;

    private Session() {
    }

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void login(long idUtilisateur, String login, String role, Long idClient, Long idLivreur) {
        this.idUtilisateur = idUtilisateur;
        this.login = login;
        this.role = role;
        this.idClient = idClient;
        this.idLivreur = idLivreur;
    }

    public void logout() {
        this.idUtilisateur = 0;
        this.login = null;
        this.role = null;
        this.idClient = null;
        this.idLivreur = null;
    }

    public boolean isLoggedIn() {
        return login != null && idUtilisateur > 0;
    }

    public long getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    public Long getIdClient() {
        return idClient;
    }

    public Long getIdLivreur() {
        return idLivreur;
    }

    public boolean isClient() {
        return "CLIENT".equals(role);
    }

    public boolean isLivreur() {
        return "LIVREUR".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
