package com.civicvote.auth;
import jakarta.persistence.*;
@Entity
@Table(name="users")
public class UserAccount {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=80) private String username;
    @Column(name="password_hash", nullable=false, length=100) private String passwordHash;
    @Column(nullable=false, length=16) private String role;
    @Column(nullable=false) private boolean enabled=true;
    protected UserAccount() {}
    public UserAccount(String username, String passwordHash, String role) { this.username=username; this.passwordHash=passwordHash; this.role=role; }
    public Long getId(){return id;} public String getUsername(){return username;} public String getPasswordHash(){return passwordHash;} public String getRole(){return role;} public boolean isEnabled(){return enabled;}
}
