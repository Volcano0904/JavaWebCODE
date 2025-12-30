package com.campus.activityservice.service;

import com.campus.activityservice.dto.ActivityDTO;
import com.campus.activityservice.entity.Activity;
import com.campus.activityservice.entity.ActivityCategory;
import com.campus.activityservice.exception.ActivityException;
import com.campus.activityservice.repository.ActivityCategoryRepository;
import com.campus.activityservice.repository.ActivityRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 活动服务类
 */
@Service
@Transactional
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityCategoryRepository activityCategoryRepository;

    /**
     * 创建活动
     */
    public Activity createActivity(ActivityDTO activityDTO) {
        // 验证分类是否存在
        Optional<ActivityCategory> categoryOpt = activityCategoryRepository.findById(activityDTO.getCategoryId());
        if (!categoryOpt.isPresent()) {
            throw new ActivityException("活动分类不存在");
        }

        // 验证时间逻辑
        validateActivityTime(activityDTO);

        Activity activity = new Activity();
        BeanUtils.copyProperties(activityDTO, activity);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        return activityRepository.save(activity);
    }

    /**
     * 更新活动
     */
    public Activity updateActivity(Long id, ActivityDTO activityDTO) {
        Optional<Activity> optionalActivity = activityRepository.findById(id);
        if (!optionalActivity.isPresent()) {
            throw new ActivityException("活动不存在");
        }

        Activity activity = optionalActivity.get();

        // 验证分类是否存在
        Optional<ActivityCategory> categoryOpt = activityCategoryRepository.findById(activityDTO.getCategoryId());
        if (!categoryOpt.isPresent()) {
            throw new ActivityException("活动分类不存在");
        }

        // 验证时间逻辑
        validateActivityTime(activityDTO);

        BeanUtils.copyProperties(activityDTO, activity, "id", "createdAt", "currentParticipants");
        activity.setUpdatedAt(LocalDateTime.now());

        return activityRepository.save(activity);
    }

    /**
     * 根据ID获取活动
     */
    public Optional<Activity> getActivityById(Long id) {
        return activityRepository.findById(id);
    }

    /**
     * 获取所有活动（分页）
     */
    public Page<Activity> getAllActivities(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return activityRepository.findAll(pageable);
    }

    /**
     * 根据状态获取活动（分页）
     */
    public Page<Activity> getActivitiesByStatus(Integer status, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return activityRepository.findByStatus(status, pageable);
    }

    /**
     * 根据标题搜索活动（分页）
     */
    public Page<Activity> searchActivitiesByTitle(String title, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return activityRepository.findByTitleContainingAndStatus(title, status, pageable);
        } else {
            return activityRepository.findByTitleContaining(title, pageable);
        }
    }

    /**
     * 根据分类获取活动
     */
    public List<Activity> getActivitiesByCategory(Integer categoryId) {
        return activityRepository.findByCategoryId(categoryId);
    }

    /**
     * 根据组织者获取活动
     */
    public List<Activity> getActivitiesByOrganizer(Long organizerId) {
        return activityRepository.findByOrganizerId(organizerId);
    }

    /**
     * 获取热门活动
     */
    public List<Activity> getHotActivities() {
        return activityRepository.findByIsHotAndStatus(1, 2); // 热门且已发布的活动
    }

    /**
     * 获取推荐活动
     */
    public List<Activity> getRecommendedActivities() {
        return activityRepository.findByIsRecommendedAndStatus(1, 2); // 推荐且已发布的活动
    }

    /**
     * 获取报名开放的活动
     */
    public List<Activity> getRegistrationOpenActivities() {
        return activityRepository.findRegistrationOpenActivities(LocalDateTime.now());
    }

    /**
     * 删除活动
     */
    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new ActivityException("活动不存在");
        }
        activityRepository.deleteById(id);
    }

    /**
     * 更新活动参与人数
     */
    public void updateParticipantCount(Long activityId, int increment) {
        activityRepository.updateParticipantCount(activityId, increment);
    }

    /**
     * 检查活动是否已满员
     */
    public boolean isActivityFull(Long activityId) {
        return activityRepository.isActivityFull(activityId);
    }

    /**
     * 验证活动时间逻辑
     */
    private void validateActivityTime(ActivityDTO activityDTO) {
        if (activityDTO.getStartTime().isAfter(activityDTO.getEndTime())) {
            throw new ActivityException("活动开始时间不能晚于结束时间");
        }

        if (activityDTO.getRegistrationStartTime().isAfter(activityDTO.getRegistrationEndTime())) {
            throw new ActivityException("报名开始时间不能晚于报名结束时间");
        }

        if (activityDTO.getRegistrationEndTime().isAfter(activityDTO.getStartTime())) {
            throw new ActivityException("报名结束时间不能晚于活动开始时间");
        }
    }
}