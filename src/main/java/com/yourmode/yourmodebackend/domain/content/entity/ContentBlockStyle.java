package com.yourmode.yourmodebackend.domain.content.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "content_block_styles")
@Getter @Setter @NoArgsConstructor
public class ContentBlockStyle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_block_id", nullable = false)
    private ContentBlock contentBlock;

    @Column(name = "font_family", length = 100)
    private String fontFamily;

    @Column(name = "font_size")
    private Integer fontSize;

    @Column(name = "font_weight", length = 20)
    private String fontWeight;

    @Column(name = "text_color", length = 20)
    private String textColor;

    @Column(name = "background_color", length = 20)
    private String backgroundColor;

    @Column(name = "text_align", length = 20)
    private String textAlign; // e.g. left, center, right, justify
}
