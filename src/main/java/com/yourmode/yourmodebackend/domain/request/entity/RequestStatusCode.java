package com.yourmode.yourmodebackend.domain.request.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_status_codes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestStatusCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code_name", nullable = false, length = 50)
    private String codeName;

    @Column(name = "status_order", nullable = false)
    @Builder.Default
    private Integer statusOrder = 0;

}