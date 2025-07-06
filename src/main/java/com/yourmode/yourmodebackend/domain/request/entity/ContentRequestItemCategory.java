package com.yourmode.yourmodebackend.domain.request.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "content_requests_item_categories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequestItemCategory {

    @EmbeddedId
    private ContentRequestItemCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("contentRequestId")
    @JoinColumn(name = "content_request_id", nullable = false)
    private ContentRequest contentRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemCategoryId")
    @JoinColumn(name = "item_category_id", nullable = false)
    private ItemCategory itemCategory;

    // 복합키용 Embeddable 클래스
    @Embeddable
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ContentRequestItemCategoryId implements Serializable {
        private Integer contentRequestId;
        private Integer itemCategoryId;
    }
}

