package com.campus.activityservice.controller;

import com.campus.activityservice.dto.ActivityDTO;
import com.campus.activityservice.entity.Activity;
import com.campus.activityservice.exception.ActivityException;
import com.campus.activityservice.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/api/activities")
@Validated
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 创建活动
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createActivity(@Valid @RequestBody ActivityDTO activityDTO) {
        Activity activity = activityService.createActivity(activityDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活动创建成功");
        response.put("data", activity);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 根据ID获取活动
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getActivityById(@PathVariable Long id) {
        Optional<Activity> optionalActivity = activityService.getActivityById(id);
        if (!optionalActivity.isPresent()) {
            throw new ActivityException("活动不存在");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", optionalActivity.get());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有活动（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<Activity> activities = activityService.getAllActivities(page, size, sortBy, direction);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities.getContent());
        response.put("pagination", Map.of(
            "currentPage", activities.getNumber(),
            "totalPages", activities.getTotalPages(),
            "totalElements", activities.getTotalElements(),
            "size", activities.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * 根据状态获取活动（分页）
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getActivitiesByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<Activity> activities = activityService.getActivitiesByStatus(status, page, size, sortBy, direction);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities.getContent());
        response.put("pagination", Map.of(
            "currentPage", activities.getNumber(),
            "totalPages", activities.getTotalPages(),
            "totalElements", activities.getTotalElements(),
            "size", activities.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * 根据标题搜索活动
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchActivities(
            @RequestParam String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Activity> activities = activityService.searchActivitiesByTitle(title, status, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities.getContent());
        response.put("pagination", Map.of(
            "currentPage", activities.getNumber(),
            "totalPages", activities.getTotalPages(),
            "totalElements", activities.getTotalElements(),
            "size", activities.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * 根据分类获取活动
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getActivitiesByCategory(@PathVariable Integer categoryId) {
        List<Activity> activities = activityService.getActivitiesByCategory(categoryId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities);

        return ResponseEntity.ok(response);
    }

    /**
     * 根据组织者获取活动
     */
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<Map<String, Object>> getActivitiesByOrganizer(@PathVariable Long organizerId) {
        List<Activity> activities = activityService.getActivitiesByOrganizer(organizerId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取热门活动
     */
    @GetMapping("/hot")
    public ResponseEntity<Map<String, Object>> getHotActivities() {
        List<Activity> activities = activityService.getHotActivities();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取推荐活动
     */
    @GetMapping("/recommended")
    public ResponseEntity<Map<String, Object>> getRecommendedActivities() {
        List<Activity> activities = activityService.getRecommendedActivities();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取报名开放的活动
     */
    @GetMapping("/registration-open")
    public ResponseEntity<Map<String, Object>> getRegistrationOpenActivities() {
        List<Activity> activities = activityService.getRegistrationOpenActivities();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities);

        return ResponseEntity.ok(response);
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateActivity(@PathVariable Long id, 
                                                              @Valid @RequestBody ActivityDTO activityDTO) {
        Activity updatedActivity = activityService.updateActivity(id, activityDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活动更新成功");
        response.put("data", updatedActivity);

        return ResponseEntity.ok(response);
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活动删除成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 检查活动是否已满员
     */
    @GetMapping("/{id}/is-full")
    public ResponseEntity<Map<String, Object>> checkActivityFull(@PathVariable Long id) {
        boolean isFull = activityService.isActivityFull(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of("isFull", isFull));

        return ResponseEntity.ok(response);
    }

    /**
     * 公共端点 - 获取活动列表
     */
    @GetMapping("/public/list")
    public ResponseEntity<Map<String, Object>> getPublicActivityList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "2") Integer status, // 默认只获取已发布的活动
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<Activity> activities = activityService.getActivitiesByStatus(status, page, size, sortBy, direction);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", activities.getContent());
        response.put("pagination", Map.of(
            "currentPage", activities.getNumber(),
            "totalPages", activities.getTotalPages(),
            "totalElements", activities.getTotalElements(),
            "size", activities.getSize()
        ));

        return ResponseEntity.ok(response);
    }
}