package com.example.skillup.domain.admin.mapper;

import com.example.skillup.domain.admin.dto.AdminResponse;
import com.example.skillup.domain.admin.enums.AdminRole;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Inquiry;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.global.common.CommonMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AdminMapper {

    public AdminResponse.AdminUserPageResponse toAdminUserPageResponse
            (List<UserResponse.AdminUserResponse> adminUserResponse)
    {


        List<UserResponse.AdminUserResponse> devUsers = new ArrayList<>();
        List<UserResponse.AdminUserResponse> designerUsers = new ArrayList<>();
        List<UserResponse.AdminUserResponse> pmUsers = new ArrayList<>();

        for (UserResponse.AdminUserResponse u : adminUserResponse) {
            switch (u.getRole()) {
                case "개발" -> devUsers.add(u);
                case "디자인" -> designerUsers.add(u);
                default -> pmUsers.add(u);
            }
        }
        return AdminResponse.AdminUserPageResponse.builder()
                .users(adminUserResponse)
                .devUsers(devUsers)
                .designerUsers(designerUsers)
                .pmUsers(pmUsers)
                .usersCount(adminUserResponse.size())
                .devUsersCount(devUsers.size())
                .designerUsersCount(designerUsers.size())
                .pmUsersCount(pmUsers.size())
                .build();
    }

    public AdminResponse.eventActionAnalyticsResponse toEventActionAnalyticsResponse
            (Map<String, Integer> rolePercentageMap,
             Map<YearMonth, Integer> userMonthlyCountMap,
             Map<YearMonth, Integer> othersMonthlyCountMap,
             LocalDateTime since
             )
    {
        List<AdminResponse.eventActionMonthlyCountResponse> eventActionMonthlyCountResponses
                = new ArrayList<>();
        YearMonth startMonth = YearMonth.from(since);

        for (int i = 0; i < 6; i++) {
            YearMonth month = startMonth.plusMonths(i);

            eventActionMonthlyCountResponses.add(
                    AdminResponse.eventActionMonthlyCountResponse.builder()
                            .monthLabels(month.getMonthValue() + "월")
                            .othersMonthlyCount(
                                    othersMonthlyCountMap.getOrDefault(month, 0)
                            )
                            .userMonthlyCount(
                                    userMonthlyCountMap.getOrDefault(month, 0)
                            )
                            .build()
            );
        }


        List<AdminResponse.rolePercentageResponse> rolePercentageResponses
                = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : rolePercentageMap.entrySet()) {
            rolePercentageResponses.add(
                    AdminResponse.rolePercentageResponse.builder()
                            .role(CommonMapper.convertRole(entry.getKey()))
                            .percentage(entry.getValue())
                            .build());
        }


        return AdminResponse.eventActionAnalyticsResponse.builder()
                .eventActionMonthlyCountResponses(eventActionMonthlyCountResponses)
                .rolePercentageResponses(rolePercentageResponses)
                .build();
    }
}
