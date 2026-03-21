package com.project.wms.auth.entity;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.project.wms.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * User entity representing an authenticated person in the system.
 * Implements {@link UserDetails} to integrate with Spring Security.
 */
@Entity
@Table(name = "auth_user")
@Getter
@Setter
public class User extends BaseEntity implements UserDetails {

    private String username;
    private String password;
    private String email;

    /**
     * Many-to-Many relationship with Role. 
     * Uses EAGER fetch because security roles are needed immediately upon authentication.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "auth_user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    /**
     * Converts the Set of Roles into a Collection of GrantedAuthority for Spring Security.
     * Uses Java Stream API to map each Role name to a SimpleGrantedAuthority.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet());
    }

    // --- UserDetails Interface Implementation ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // Simplified for now: account never expires
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Simplified for now: account is never locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Simplified for now: password never expires
    }

    @Override
    public boolean isEnabled() {
        return true; // Simplified for now: account is always enabled
    }

}
