package com.yourmode.yourmodebackend.domain.request.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "item_categories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    // N:M content_requests_item_categories 양방향 관계
    @ManyToMany(mappedBy = "itemCategories")
    private Set<ContentRequest> contentRequests = new HashSet<>();
}
