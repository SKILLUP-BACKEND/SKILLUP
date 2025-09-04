package com.example.skillup.domain.admin.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AdminTest
{
    @Test
    void builder로_Admin_생성()
    {

        Admin admin = Admin.builder().email("aasdsd").password("ssdsdssd").build();

        assertThat(admin).isNotNull();
    }

}
