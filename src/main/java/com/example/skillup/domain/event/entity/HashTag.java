package com.example.skillup.domain.event.entity;

import com.example.skillup.domain.event.enums.HashTagCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HashTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100,unique = true)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HashTagCategory category;
}