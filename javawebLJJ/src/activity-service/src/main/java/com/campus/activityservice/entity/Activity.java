package com.campus.activityservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 活动实体类
 */
@Data
@Entity
@Table(name = "activity")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String posterUrl;

    @Column(nullable = false)
    private Long organizerId;

    @Column(nullable = false, length = 100)
    private String organizerName;

    @Column(nullable = false)
    private Integer categoryId;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(length = 500)
    private String addressDetail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationEndTime;

    @Column
    private Integer maxParticipants = 0; // 0表示无限制

    @Column
    private Integer currentParticipants = 0;

    @Column
    private Integer status = 1; // 1-草稿,2-已发布,3-进行中,4-已结束,5-已取消

    @Column
    private Integer isHot = 0; // 0-否,1-是

    @Column
    private Integer isRecommended = 0; // 0-否,1-是

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}