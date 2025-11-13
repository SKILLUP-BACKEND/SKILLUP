package com.example.skillup.domain.user.service;

import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.user.dto.response.UserResponseDto;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.ThrowIfEmptyAspect;
import com.example.skillup.global.exception.GlobalException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({UserService.class, ThrowIfEmptyAspect.class})
class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;



    @Test
    void findByAll_ShouldReturnList_WhenUsersExist() {

        List<Users> users = new java.util.ArrayList<>();
        users.add(Users.builder().build());

        given(userRepository.findAll())
                .willReturn(users);
        List<UserResponseDto> result = userService.findAll();

        assertThat(result).hasSize(1);
    }


}
