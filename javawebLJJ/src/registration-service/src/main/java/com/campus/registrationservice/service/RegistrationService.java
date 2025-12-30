package com.campus.registrationservice.service;

import com.campus.registrationservice.entity.Registration;
import com.campus.registrationservice.exception.RegistrationException;
import com.campus.registrationservice.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 报名服务类
 */
@Service
@Transactional
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 创建报名记录
     */
    public Registration createRegistration(Registration registration) {
        // 检查是否已报名
        if (registrationRepository.existsByActivityIdAndUserIdAndStatusIn(
                registration.getActivityId(), 
                registration.getUserId(), 
                List.of(1, 2))) { // 1-待审核, 2-已通过
            throw new RegistrationException("您已报名该活动，无需重复报名");
        }

        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());

        Registration savedRegistration = registrationRepository.save(registration);

        // 发送消息更新活动参与人数
        Map<String, Object> message = new HashMap<>();
        message.put("activityId", registration.getActivityId());
        message.put("increment", 1);
        kafkaTemplate.send("activity-participant-updates", message);

        return savedRegistration;
    }

    /**
     * 异步创建报名记录（用于消息队列处理）
     */
    public Registration createRegistrationAsync(Registration registration) {
        // 检查是否已报名
        if (registrationRepository.existsByActivityIdAndUserIdAndStatusIn(
                registration.getActivityId(), 
                registration.getUserId(), 
                List.of(1, 2))) { // 1-待审核, 2-已通过
            throw new RegistrationException("您已报名该活动，无需重复报名");
        }

        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    /**
     * 根据ID获取报名记录
     */
    public Optional<Registration> getRegistrationById(Long id) {
        return registrationRepository.findById(id);
    }

    /**
     * 根据活动ID和用户ID获取报名记录
     */
    public Optional<Registration> getRegistrationByActivityIdAndUserId(Long activityId, Long userId) {
        return registrationRepository.findByActivityIdAndUserId(activityId, userId);
    }

    /**
     * 获取用户报名记录列表
     */
    public List<Registration> getRegistrationsByUserId(Long userId) {
        return registrationRepository.findByUserId(userId);
    }

    /**
     * 获取活动报名记录列表
     */
    public List<Registration> getRegistrationsByActivityId(Long activityId) {
        return registrationRepository.findByActivityId(activityId);
    }

    /**
     * 获取活动报名记录列表（分页）
     */
    public Page<Registration> getRegistrationsByActivityId(Long activityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return registrationRepository.findByActivityId(activityId, pageable);
    }

    /**
     * 获取用户报名记录列表（分页）
     */
    public Page<Registration> getRegistrationsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return registrationRepository.findByUserId(userId, pageable);
    }

    /**
     * 获取指定状态的报名记录列表
     */
    public List<Registration> getRegistrationsByStatus(Integer status) {
        return registrationRepository.findByStatus(status);
    }

    /**
     * 获取指定状态的报名记录列表（分页）
     */
    public Page<Registration> getRegistrationsByStatus(Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return registrationRepository.findByStatus(status, pageable);
    }

    /**
     * 更新报名状态
     */
    public Registration updateRegistrationStatus(Long id, Integer status, String reason) {
        Optional<Registration> optionalRegistration = registrationRepository.findById(id);
        if (!optionalRegistration.isPresent()) {
            throw new RegistrationException("报名记录不存在");
        }

        Registration registration = optionalRegistration.get();
        int updated = registrationRepository.updateStatus(id, status, reason);
        if (updated == 0) {
            throw new RegistrationException("更新报名状态失败");
        }

        registration.setStatus(status);
        registration.setReason(reason);
        registration.setUpdatedAt(LocalDateTime.now());

        return registration;
    }

    /**
     * 根据活动ID和用户ID更新报名状态
     */
    public int updateRegistrationStatusByActivityIdAndUserId(Long activityId, Long userId, Integer status, String reason) {
        return registrationRepository.updateStatusByActivityIdAndUserId(activityId, userId, status, reason);
    }

    /**
     * 取消报名
     */
    public void cancelRegistration(Long id, Long userId) {
        Optional<Registration> optionalRegistration = registrationRepository.findById(id);
        if (!optionalRegistration.isPresent()) {
            throw new RegistrationException("报名记录不存在");
        }

        Registration registration = optionalRegistration.get();
        if (!registration.getUserId().equals(userId)) {
            throw new RegistrationException("无权限取消他人报名");
        }

        if (registration.getStatus() != 1 && registration.getStatus() != 2) { // 不是待审核或已通过状态不能取消
            throw new RegistrationException("当前状态不能取消报名");
        }

        int updated = registrationRepository.updateStatus(id, 4, "用户主动取消"); // 4-已取消
        if (updated == 0) {
            throw new RegistrationException("取消报名失败");
        }

        // 发送消息更新活动参与人数
        Map<String, Object> message = new HashMap<>();
        message.put("activityId", registration.getActivityId());
        message.put("increment", -1); // 减少人数
        kafkaTemplate.send("activity-participant-updates", message);
    }

    /**
     * 删除报名记录
     */
    public void deleteRegistration(Long id) {
        if (!registrationRepository.existsById(id)) {
            throw new RegistrationException("报名记录不存在");
        }
        registrationRepository.deleteById(id);
    }

    /**
     * 统计活动报名人数
     */
    public long countRegistrationsByActivityIdAndStatus(Long activityId, Integer status) {
        return registrationRepository.countByActivityIdAndStatus(activityId, status);
    }

    /**
     * 检查用户是否已报名活动
     */
    public boolean isUserRegistered(Long activityId, Long userId) {
        return registrationRepository.existsByActivityIdAndUserIdAndStatusIn(
                activityId, userId, List.of(1, 2)); // 1-待审核, 2-已通过
    }

    /**
     * 更新活动参与人数
     */
    public void updateActivityParticipantCount(Long activityId, int increment) {
        // 这里可以调用活动服务的API来更新参与人数
        // 为简化实现，这里直接记录日志
        System.out.println("更新活动参与人数: 活动ID=" + activityId + ", 增量=" + increment);
    }
}