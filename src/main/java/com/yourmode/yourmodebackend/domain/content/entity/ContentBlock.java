package com.yourmode.yourmodebackend.domain.content.entity;

import jakarta.persistence.*;
import lombok.*;


import java.util.List;

@Entity
@Table(name = "content_blocks")
@Getter
@Setter
@NoArgsConstructor
public class ContentBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 1: image, 2: text, 3: image_group
     */
    @Column(name = "block_type", nullable = false)
    private Integer blockType;

    @Column(name = "content_data", nullable = false, columnDefinition = "TEXT")
    private String contentData;

    @Column(name = "block_order", nullable = false)
    private Integer blockOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @OneToOne(mappedBy = "contentBlock", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContentBlockStyle contentBlockStyle;

    @OneToMany(mappedBy = "contentBlock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentBlockImage> images;
}