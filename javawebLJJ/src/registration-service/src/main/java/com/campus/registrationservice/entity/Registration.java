package com.campus.registrationservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 报名实体类
 */
@Data
@Entity
@Table(name = "registration")
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long activityId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 50)
    private String realName;

    @Column(length = 20)
    private String studentId;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column
    private Integer status = 1; // 1-待审核,2-已通过,3-已拒绝,4-已取消

    @Column(length = 500)
    private String reason; // 审核理由或备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}