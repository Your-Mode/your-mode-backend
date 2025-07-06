package com.yourmode.yourmodebackend.domain.content.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "content_categories")
@Getter @Setter @NoArgsConstructor
public class ContentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "contentCategories")
    private Set<Content> contents;
}