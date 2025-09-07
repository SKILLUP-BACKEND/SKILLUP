package com.example.skillup.domain.admin.service;

import com.example.skillup.domain.admin.dto.AdminLoginRequest;
import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.exception.AdminException;
import com.example.skillup.domain.admin.repository.AdminRepository;
import com.example.skillup.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;


    public Admin login(AdminLoginRequest request)
    {

        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new AdminException(ErrorCode.DATA_NOT_FOUND,"Email이 "+request.email()+"인"));

        if(admin.isPasswordMatch(request.password()))
            return admin;
        else
            throw new AdminException(ErrorCode.INVALID_PASSWORD);
    }

}
