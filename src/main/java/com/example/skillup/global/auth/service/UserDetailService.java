package com.example.skillup.global.auth.service;


import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.exception.AdminException;
import com.example.skillup.domain.admin.repository.AdminRepository;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{


        //일단은 임시로 계정이 하나 뿐이라 이렇게 하드코딩으로 설정 했는데
        // 나중에 여러개가 설정 될 때는 Set이나 Map같은 곳에 어드민 이메일 넣어서 사용 할 예정입니다
        if(email.equals("skillup02.official@gmail.com"))
        {
            Admin admin = adminRepository.findByEmail(email)
                    .orElseThrow(()->new AdminException(CommonErrorCode.DATA_NOT_FOUND, email+ "에 해당되는 어드민이 존재 하지 않습니다."));

            return new UsersDetails(admin);
        }

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(CommonErrorCode.DATA_NOT_FOUND , email + "에 해당하는 유저가 존재하지 않습니다."));

        return new UsersDetails(user);

    }
}
