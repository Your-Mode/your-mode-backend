package com.yourmode.yourmodebackend.domain.content.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "content_block_images")
@Getter @Setter @NoArgsConstructor
public class ContentBlockImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "image_order", nullable = false)
    private Integer imageOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_block_id", nullable = false)
    private ContentBlock contentBlock;
}