package com.example.skillup.domain.user.entity;


import com.example.skillup.domain.admin.entity.Admin;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsersDetails implements UserDetails {

    private final Users user;

    private final Admin admin;

    public UsersDetails(Users user) {
        this.user = user;
        this.admin = null;
    }

    public UsersDetails(Admin admin) {
        this.admin = admin;
        this.user = null;
    }

    public Users getUser() {
        return user;
    }

    public boolean isAdmin() {
        return admin != null;
    }

    public boolean isUser() {
        return user != null;
    }

    public Admin getAdmin() {
        return admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (admin != null) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())
            );
        }

        return List.of(new SimpleGrantedAuthority("ROLE_USER" ));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        if (admin != null) return admin.getEmail();
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
