package com.example.skillup.global.auth.service;


import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.DATA_NOT_FOUND));

        return new UsersDetails(user);

    }

}
