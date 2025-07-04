package com.yourmode.yourmodebackend.domain.user.entity;

import com.yourmode.yourmodebackend.domain.user.enums.Gender;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Float height;
    private Float weight;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne
    @JoinColumn(name = "body_type_id")
    private BodyType bodyType;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
