package com.example.campus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "tsuki_resume")
public class TsukiResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private TsukiStudent student;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "education_experience")
    private String educationExperience;

    @Column(name = "work_experience")
    private String workExperience;

    @Column(name = "skills")
    private String skills;

    @Column(name = "self_evaluation")
    private String selfEvaluation;

    @Column(name = "attachment", length = 255)
    private String attachment;

    @Builder.Default
    @OneToMany(mappedBy = "resume", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TsukiResumeAttachment> attachments = new ArrayList<>();

    @Column(name = "create_time", updatable = false, insertable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;
}
