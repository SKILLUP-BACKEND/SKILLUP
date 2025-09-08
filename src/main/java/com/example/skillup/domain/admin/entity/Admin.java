package com.example.skillup.domain.admin.entity;


import com.example.skillup.domain.admin.enums.AdminRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Admin
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String email;

    @Column(nullable = false, length = 15)
    private String password;

    @Enumerated(EnumType.STRING)
    private AdminRole role;

    @Builder
    public Admin(String email, String password, AdminRole role)
    {
        this.email = email;
        this.password = password;
        this.role = role;

    }

    public boolean isPasswordMatch(String inputPassword) {
        return inputPassword.equals(this.password);
    }



}
