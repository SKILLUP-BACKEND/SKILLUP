package com.example.skillup.domain.user.entity;

import static lombok.AccessLevel.PROTECTED;

import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.dto.request.UserRequest;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Users extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    private String age;

    @Column(length = 1)
    private String gender;

    @Column(nullable = false)
    private LocalDateTime regDatetime;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TargetRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private UserStatus status;

    @Column(length = 1, nullable = false)
    private String notificationFlag;

    private LocalDateTime lastLoginAt;

    private String socialId;
    
    @Enumerated(EnumType.STRING)
    private SocialLoginType socialLoginType;

    @Builder.Default
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "users_interest",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<Interest> interests = new HashSet<>();

    private String profileImageUrl;

    private boolean marketingAgreement;

    public void update(UserRequest.UserUpdateRequest dto,TargetRole role,Set<Interest> interests, String userProfileImageUrl) {
        if (dto.getName() != null) this.name = dto.getName();
        if (userProfileImageUrl != null) this.profileImageUrl = userProfileImageUrl;
        if (dto.getAge() != null) this.age = dto.getAge();
        if (dto.getMarketingAgreement() != null) this.marketingAgreement = dto.getMarketingAgreement();
        if (dto.getGender() != null) this.gender = dto.getGender();
        if (dto.getRole() != null) this.role = role;
        if (dto.getInterests() != null) this.interests = interests;
    }

}
