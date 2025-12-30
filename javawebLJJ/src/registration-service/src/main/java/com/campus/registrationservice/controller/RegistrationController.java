package com.campus.registrationservice.controller;

import com.campus.registrationservice.dto.RegistrationDTO;
import com.campus.registrationservice.entity.Registration;
import com.campus.registrationservice.exception.RegistrationException;
import com.campus.registrationservice.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 报名控制器
 */
@RestController
@RequestMapping("/api/registrations")
@Validated
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 创建报名
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRegistration(@Valid @RequestBody RegistrationDTO registrationDTO) {
        // 检查是否已报名
        if (registrationService.isUserRegistered(registrationDTO.getActivityId(), registrationDTO.getUserId())) {
            throw new RegistrationException("您已报名该活动，无需重复报名");
        }

        Registration registration = new Registration();
        registration.setActivityId(registrationDTO.getActivityId());
        registration.setUserId(registrationDTO.getUserId());
        registration.setUsername(registrationDTO.getUsername());
        registration.setRealName(registrationDTO.getRealName());
        registration.setStudentId(registrationDTO.getStudentId());
        registration.setEmail(registrationDTO.getEmail());
        registration.setPhone(registrationDTO.getPhone());

        Registration savedRegistration = registrationService.createRegistration(registration);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "报名成功");
        response.put("data", savedRegistration);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 根据ID获取报名记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRegistrationById(@PathVariable Long id) {
        Optional<Registration> optionalRegistration = registrationService.getRegistrationById(id);
        if (!optionalRegistration.isPresent()) {
            throw new RegistrationException("报名记录不存在");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", optionalRegistration.get());

        return ResponseEntity.ok(response);
    }

    /**
     * 根据活动ID和用户ID获取报名记录
     */
    @GetMapping("/activity/{activityId}/user/{userId}")
    public ResponseEntity<Map<String, Object>> getRegistrationByActivityIdAndUserId(
            @PathVariable Long activityId, @PathVariable Long userId) {
        Optional<Registration> optionalRegistration = 
            registrationService.getRegistrationByActivityIdAndUserId(activityId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", optionalRegistration.orElse(null));

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户报名记录列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getRegistrationsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Registration> registrations = registrationService.getRegistrationsByUserId(userId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", registrations.getContent());
        response.put("pagination", Map.of(
            "currentPage", registrations.getNumber(),
            "totalPages", registrations.getTotalPages(),
            "totalElements", registrations.getTotalElements(),
            "size", registrations.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * 获取活动报名记录列表
     */
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<Map<String, Object>> getRegistrationsByActivityId(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Registration> registrations = registrationService.getRegistrationsByActivityId(activityId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", registrations.getContent());
        response.put("pagination", Map.of(
            "currentPage", registrations.getNumber(),
            "totalPages", registrations.getTotalPages(),
            "totalElements", registrations.getTotalElements(),
            "size", registrations.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * 获取指定状态的报名记录列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getRegistrationsByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Registration> registrations = registrationService.getRegistrationsByStatus(status, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", registrations.getContent());
        response.put("pagination", Map.of(
            "currentPage", registrations.getNumber(),
            "totalPages", registrations.getTotalPages(),
            "totalElements", registrations.getTotalElements(),
            "size", registrations.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * 更新报名状态（审核）
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateRegistrationStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        
        Registration updatedRegistration = registrationService.updateRegistrationStatus(id, status, reason);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "报名状态更新成功");
        response.put("data", updatedRegistration);

        return ResponseEntity.ok(response);
    }

    /**
     * 取消报名
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelRegistration(@PathVariable Long id, 
                                                                 @RequestParam Long userId) {
        registrationService.cancelRegistration(id, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "报名已取消");

        return ResponseEntity.ok(response);
    }

    /**
     * 删除报名记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRegistration(@PathVariable Long id) {
        registrationService.deleteRegistration(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "报名记录删除成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 统计活动报名人数
     */
    @GetMapping("/activity/{activityId}/count")
    public ResponseEntity<Map<String, Object>> countRegistrationsByActivityId(@PathVariable Long activityId) {
        long pendingCount = registrationService.countRegistrationsByActivityIdAndStatus(activityId, 1); // 待审核
        long approvedCount = registrationService.countRegistrationsByActivityIdAndStatus(activityId, 2); // 已通过
        long rejectedCount = registrationService.countRegistrationsByActivityIdAndStatus(activityId, 3); // 已拒绝
        long cancelledCount = registrationService.countRegistrationsByActivityIdAndStatus(activityId, 4); // 已取消

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of(
            "pending", pendingCount,
            "approved", approvedCount,
            "rejected", rejectedCount,
            "cancelled", cancelledCount,
            "total", pendingCount + approvedCount + rejectedCount + cancelledCount
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * 检查用户是否已报名活动
     */
    @GetMapping("/check/{activityId}/user/{userId}")
    public ResponseEntity<Map<String, Object>> checkUserRegistration(@PathVariable Long activityId, 
                                                                    @PathVariable Long userId) {
        boolean isRegistered = registrationService.isUserRegistered(activityId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of("isRegistered", isRegistered));

        return ResponseEntity.ok(response);
    }

    /**
     * 公共端点 - 获取用户报名活动列表
     */
    @GetMapping("/public/user/{userId}")
    public ResponseEntity<Map<String, Object>> getPublicRegistrationsByUserId(@PathVariable Long userId) {
        List<Registration> registrations = registrationService.getRegistrationsByUserId(userId);

        // 只返回必要的信息
        List<Map<String, Object>> publicRegistrations = registrations.stream().map(reg -> {
            Map<String, Object> regData = new HashMap<>();
            regData.put("id", reg.getId());
            regData.put("activityId", reg.getActivityId());
            regData.put("status", reg.getStatus());
            regData.put("createdAt", reg.getCreatedAt());
            return regData;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", publicRegistrations);

        return ResponseEntity.ok(response);
    }
}