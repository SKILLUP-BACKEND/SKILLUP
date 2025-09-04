package com.example.skillup.domain.admin.repository;


import com.example.skillup.domain.admin.entity.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

public class AdminRepositoryTest
{
    @Autowired
    private AdminRepository adminRepository;

    Admin admin;

    @BeforeEach
    void setUp()
    {
        admin = Admin.builder().email("kim").password("ssdsdssd").build();
    }
    @Test
    void admin_저장_조회_테스트()
    {
        Admin savedAdmin=adminRepository.save(admin);
        Admin foundAdmin =adminRepository.findById(admin.getId()).orElseThrow();

        assertThat(savedAdmin.getId()).isNotNull();
        assertThat(foundAdmin.getId()).isEqualTo(admin.getId());
    }

    @Test
    void admin_id값_테스트()
    {
        Admin admin2=Admin.builder().email("asdsdsss").password("ssdsdssd").build();

        adminRepository.save(admin);
        adminRepository.save(admin2);
        Admin foundAdmin1 =adminRepository.findById(admin2.getId()).orElseThrow();
        Admin foundAdmin2 =adminRepository.findById(admin.getId()).orElseThrow();


        assertThat(foundAdmin1.getId()).isNotEqualTo(foundAdmin2.getId());
    }

    @Test
    void admin_이메일_중복저장_테스트()
    {
        Admin admin2=Admin.builder().email("kim").password("ssdsdssd").build();

        adminRepository.save(admin);

        assertThrows(Exception.class, () -> {
            adminRepository.save(admin2);
        });
    }

    @Test
    void admin_email_null값_테스트()
    {
        Admin admin2=Admin.builder().email(null).password("ssdsdssd").build();

        assertThrows(Exception.class, () -> {
            adminRepository.save(admin2);
        });
    }

}
