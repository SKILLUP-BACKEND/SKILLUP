package com.example.skillup.domain.admin.service;

import com.example.skillup.domain.admin.dto.AdminLoginRequest;
import com.example.skillup.domain.admin.dto.AdminResponse;
import com.example.skillup.domain.admin.dto.SynonymRequest.AddTermsReq;
import com.example.skillup.domain.admin.dto.SynonymRequest.CreateSynonymRequest;
import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.exception.AdminException;
import com.example.skillup.domain.admin.mapper.AdminMapper;
import com.example.skillup.domain.admin.mapper.SynonymMapper;
import com.example.skillup.domain.admin.repository.AdminRepository;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.repository.EventActionRepository;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.exception.UserErrorCode;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.mappers.UserMapper;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import com.example.skillup.global.common.BaseEntity;
import com.example.skillup.global.component.Calculator;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final UserRepository userRepository;
    private final AdminMapper adminMapper;
    private final UserMapper  userMapper;
    private final EventActionRepository eventActionRepository;


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

    public AdminResponse.AdminUserPageResponse getUsersBySearch(String keyWard, boolean deleted,int page)
    {
        Pageable pageable = PageRequest.of(page, 20);
        List<Users> users= userRepository.findUsersByKeyWardAndDeleted(keyWard, deleted, pageable);
        List<UserResponse.AdminUserResponse> adminUserResponse = new ArrayList<>();

        for(Users user : users)
            adminUserResponse.add(userMapper.toAdminUserResponse(user));

        return adminMapper.toAdminUserPageResponse(adminUserResponse, pageable, page,
        users.size());

    }

    @ConvertNotFound(
            exception = UserException.class,
            errorCodeEnum = UserErrorCode.class,
            errorCodeName = "USER_ENTITY_NOT_FOUND"
    )
    public UserResponse.AdminUserDetailPageResponse getUsersDetail(Long userId)
    {
        Users user = userRepository.findById(userId).orElseThrow();
        return userMapper.toAdminUserDetailPageResponse(user);
    }

    public UserResponse.AdminUserEventActionCountsResponse getUserActionCounts(Long userId)
    {
        UserRepository.EventActionCountProjection usersActionCounts = userRepository.getUserActionCounts(userId);
        return userMapper.toAdminUserEventActionResponse(usersActionCounts.getViewCnt()
                ,usersActionCounts.getSaveCnt(),usersActionCounts.getApplyCnt());
    }

    @Transactional(readOnly = true)
    public AdminResponse.eventActionAnalyticsResponse getUserEventActionAnalytics(String userId,String actionType)
    {
        LocalDateTime since = LocalDate.now()
                .withDayOfMonth(1)
                .minusMonths(5)
                .atStartOfDay();

        List<EventActionRepository.EventActionAnalyticsProjection> eventActionAnalytics
                =  eventActionRepository.findEventActionsBySinceAndActionType(since, actionType);

        List<EventActionRepository.EventActionAnalyticsProjection> usersEventActionAnalytics =
                eventActionAnalytics.stream()
                        .filter(a -> Objects.equals(a.getActorId(), userId))
                        .toList();

        List<EventActionRepository.EventActionAnalyticsProjection> othersEventActionAnalytics =
                eventActionAnalytics.stream()
                        .filter(a -> !Objects.equals(a.getActorId(), userId))
                        .toList();




        Map<String, Integer> roleCountMap = new HashMap<>();
        int totalRoleCount = 0;

        for (EventActionRepository.EventActionAnalyticsProjection action : usersEventActionAnalytics) {
            String roles = action.getTargetRoles();
            if (roles == null || roles.isBlank()) continue;

            for (String roleName : roles.split(",")) {
                roleCountMap.merge(roleName, 1, Integer::sum);
                totalRoleCount++;
            }
        }

        Map<String, Integer> rolePercentageMap =
                Calculator.calculateWithHamilton(
                        roleCountMap,
                        totalRoleCount
                );

        Map<YearMonth, Integer> userMonthlyCountMap =
                Calculator.countByMonth(usersEventActionAnalytics);

        Map<YearMonth, Integer> othersMonthlyCountMap  =
                Calculator.countByMonth(othersEventActionAnalytics);

        int totalUserCount = userRepository.getTotalCount();

        othersMonthlyCountMap.replaceAll((month, count) -> {
            if (totalUserCount == 0) return 0;
            return count / totalUserCount;
        });


        return adminMapper.toEventActionAnalyticsResponse(rolePercentageMap,userMonthlyCountMap,othersMonthlyCountMap,since);

    }
}
