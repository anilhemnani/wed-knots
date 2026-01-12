package com.wedknots.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles_tbl")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long user_id;
    private Long role_id;

    public UserRole() {}
    public UserRole(Long id, Long user_id, Long role_id) {
        this.id = id;
        this.user_id = user_id;
        this.role_id = role_id;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return user_id; }
    public void setUserId(Long user_id) { this.user_id = user_id; }
    public Long getRoleId() { return role_id; }
    public void setRoleId(Long role_id) { this.role_id = role_id; }
}
