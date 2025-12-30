package com.campus.registrationservice.message;

import com.campus.registrationservice.entity.Registration;
import com.campus.registrationservice.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 报名消息处理器
 */
@Component
public class RegistrationMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationMessageHandler.class);

    @Autowired
    private RegistrationService registrationService;

    /**
     * 处理报名消息
     */
    @KafkaListener(topics = "registration-requests", groupId = "registration-group")
    public void handleRegistrationRequest(Map<String, Object> message) {
        logger.info("收到报名请求消息: {}", message);

        try {
            // 从消息中提取报名信息
            Long activityId = (Long) message.get("activityId");
            Long userId = (Long) message.get("userId");
            String username = (String) message.get("username");
            String realName = (String) message.get("realName");
            String studentId = (String) message.get("studentId");
            String email = (String) message.get("email");
            String phone = (String) message.get("phone");

            // 创建报名记录
            Registration registration = new Registration();
            registration.setActivityId(activityId);
            registration.setUserId(userId);
            registration.setUsername(username);
            registration.setRealName(realName);
            registration.setStudentId(studentId);
            registration.setEmail(email);
            registration.setPhone(phone);
            registration.setStatus(1); // 待审核

            // 保存报名记录
            registrationService.createRegistrationAsync(registration);
            
            logger.info("异步报名处理完成，活动ID: {}, 用户ID: {}", activityId, userId);
        } catch (Exception e) {
            logger.error("处理报名消息失败", e);
            // 可以发送错误消息到死信队列或重试
        }
    }

    /**
     * 处理活动人数更新消息
     */
    @KafkaListener(topics = "activity-participant-updates", groupId = "registration-group")
    public void handleActivityParticipantUpdate(Map<String, Object> message) {
        logger.info("收到活动人数更新消息: {}", message);

        try {
            Long activityId = (Long) message.get("activityId");
            Integer increment = (Integer) message.get("increment");

            // 更新活动参与人数
            registrationService.updateActivityParticipantCount(activityId, increment);
            
            logger.info("活动人数更新完成，活动ID: {}, 增量: {}", activityId, increment);
        } catch (Exception e) {
            logger.error("处理活动人数更新消息失败", e);
        }
    }
}