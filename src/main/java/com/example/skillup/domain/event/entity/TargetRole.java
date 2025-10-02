package com.example.skillup.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    @ManyToMany(mappedBy = "targetRoles")
    private Set<Event> events = new HashSet<>();

    public TargetRole(String name) {
        this.name = name;
    }
}
