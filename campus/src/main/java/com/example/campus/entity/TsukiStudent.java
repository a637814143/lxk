package com.example.campus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tsuki_student")
public class TsukiStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private TsukiUser user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "school", length = 100)
    private String school;

    @Column(name = "major", length = 100)
    private String major;

    @Column(name = "grade", length = 20)
    private String grade;

    @Column(name = "education", length = 50)
    private String education;

    @Column(name = "avatar", length = 255)
    private String avatar;
}
