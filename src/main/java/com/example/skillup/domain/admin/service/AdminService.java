package com.example.skillup.domain.admin.service;

import com.example.skillup.domain.admin.dto.AdminLoginRequest;
import com.example.skillup.domain.admin.dto.SynonymRequest.AddTermsReq;
import com.example.skillup.domain.admin.dto.SynonymRequest.CreateSynonymRequest;
import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.exception.AdminException;
import com.example.skillup.domain.admin.mapper.SynonymMapper;
import com.example.skillup.domain.admin.repository.AdminRepository;
import com.example.skillup.global.common.BaseEntity;
import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.exception.GlobalException;
import com.example.skillup.global.search.component.ElasticsearchAdminClient;
import com.example.skillup.global.search.component.SynonymExporter;
import com.example.skillup.global.search.entity.SynonymGroup;
import com.example.skillup.global.search.entity.SynonymTerm;
import com.example.skillup.global.search.enums.SynonymStatus;
import com.example.skillup.global.search.repository.SynonymGroupRepository;
import com.example.skillup.global.search.repository.SynonymTermRepository;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final SynonymExporter synonymExporter;
    private final ElasticsearchAdminClient elasticsearchAdminClient;
    private final SynonymMapper synonymMapper;
    private final SynonymGroupRepository synonymGroupRepository;
    private final SynonymTermRepository synonymTermRepository;


    public Admin login(AdminLoginRequest request) {

        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(
                        () -> new AdminException(CommonErrorCode.DATA_NOT_FOUND, "Email이 " + request.email() + "인"));

        if (admin.isPasswordMatch(request.password())) {
            return admin;
        } else {
            throw new AdminException(CommonErrorCode.INVALID_PASSWORD);
        }
    }

    @Transactional
    public String publish(String locale) {
        Path file = synonymExporter.exportActiveToFile(locale);
        String reload = elasticsearchAdminClient.reloadSearchAnalyzers();
        return file.toString() + reload;
        //new PublishResult(true, file.toString(), reload);
    }

    @Transactional
    public String createSynonymGroupAndTerm(CreateSynonymRequest request) {
        SynonymGroup synonymGroup = synonymMapper.toGroupEntity(request);

        synonymGroup = synonymGroupRepository.save(synonymGroup);

        List<SynonymTerm> synonymTermList = synonymMapper.toTermEntity(synonymGroup, request.terms());

        try {
            synonymTermRepository.saveAll(synonymTermList);
        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(CommonErrorCode.INVALID_INPUT_VALUE, "동일 그룹 내 중복 용어가 있습니다.");
        }

        final String locale = synonymGroup.getLocale();

        //정보 업데이트 후 동의어 사전 변경해주기
        publish(locale);

        return String.join(", ", request.terms()) + publish(locale);
    }

    @Transactional
    public String addTermsToSynonymGroup(Long groupId, AddTermsReq request) {
        SynonymGroup synonymGroup = synonymGroupRepository.getGroup(groupId);

        List<SynonymTerm> synonymTermList = synonymMapper.toTermEntity(synonymGroup, request.terms());

        try {
            synonymTermRepository.saveAll(synonymTermList);
        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(CommonErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 용어가 포함되어 있습니다.");
        }

        final String locale = synonymGroup.getLocale();

        //정보 업데이트 후 동의어 사전 변경해주기
        publish(locale);

        synonymTermList = synonymTermRepository.findAllByGroupAndStatus(synonymGroup, SynonymStatus.ACTIVE);
        return String.join(", ", synonymTermList.stream().map(SynonymTerm::getTerm).toList());
    }

    @Transactional
    public String deleteGroup(Long groupId) {
        SynonymGroup synonymGroup = synonymGroupRepository.getGroup(groupId);

        List<SynonymTerm> synonymTermList = synonymTermRepository.findAllByGroup(synonymGroup);

        synonymTermList.forEach(BaseEntity::delete);
        String deletedTerms = String.join(", ", synonymTermList.stream().map(SynonymTerm::getTerm).toList());
        synonymGroup.delete();

        final String locale = synonymGroup.getLocale();
        publish(locale);

        return deletedTerms;
    }
}
