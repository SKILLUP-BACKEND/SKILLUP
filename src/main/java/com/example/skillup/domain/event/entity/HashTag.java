package com.example.skillup.domain.event.entity;

import com.example.skillup.domain.event.enums.HashTagCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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

    @Builder.Default
    @ManyToMany(mappedBy = "hashTags")
    private Set<Event> events = new HashSet<>();


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HashTagCategory category;
}