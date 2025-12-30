package com.campus.activityservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 活动数据传输对象
 */
@Data
public class ActivityDTO {
    private Long id;

    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String description;

    private String posterUrl;

    @NotNull(message = "组织者ID不能为空")
    private Long organizerId;

    @NotBlank(message = "组织者姓名不能为空")
    private String organizerName;

    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;

    @NotBlank(message = "活动地点不能为空")
    private String location;

    private String addressDetail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "报名开始时间不能为空")
    private LocalDateTime registrationStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "报名结束时间不能为空")
    private LocalDateTime registrationEndTime;

    private Integer maxParticipants = 0; // 0表示无限制

    private Integer status = 1; // 1-草稿,2-已发布,3-进行中,4-已结束,5-已取消
}