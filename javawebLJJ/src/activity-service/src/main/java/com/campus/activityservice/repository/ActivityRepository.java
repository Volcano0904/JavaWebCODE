package com.campus.activityservice.repository;

import com.campus.activityservice.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动Repository
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    // 根据状态查找活动
    List<Activity> findByStatus(Integer status);
    
    // 根据分类ID查找活动
    List<Activity> findByCategoryId(Integer categoryId);
    
    // 根据组织者ID查找活动
    List<Activity> findByOrganizerId(Long organizerId);
    
    // 查找热门活动
    List<Activity> findByIsHotAndStatus(Integer isHot, Integer status);
    
    // 查找推荐活动
    List<Activity> findByIsRecommendedAndStatus(Integer isRecommended, Integer status);
    
    // 根据时间段查找活动
    List<Activity> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // 分页查找活动
    Page<Activity> findByStatus(Integer status, Pageable pageable);
    
    // 根据活动标题模糊查询
    Page<Activity> findByTitleContainingAndStatus(String title, Integer status, Pageable pageable);
    
    // 查找报名时间段内的活动
    @Query("SELECT a FROM Activity a WHERE a.registrationStartTime <= :currentTime AND a.registrationEndTime >= :currentTime AND a.status = 2")
    List<Activity> findRegistrationOpenActivities(@Param("currentTime") LocalDateTime currentTime);
    
    // 查找即将开始的活动
    @Query("SELECT a FROM Activity a WHERE a.startTime > :currentTime AND a.startTime <= :timeLimit AND a.status = 2")
    List<Activity> findUpcomingActivities(@Param("currentTime") LocalDateTime currentTime, 
                                         @Param("timeLimit") LocalDateTime timeLimit);
    
    // 检查活动是否已满员
    @Query("SELECT CASE WHEN a.maxParticipants = 0 THEN false ELSE a.currentParticipants >= a.maxParticipants END FROM Activity a WHERE a.id = :id")
    boolean isActivityFull(@Param("id") Long id);
    
    // 更新活动参与人数
    @Query(value = "UPDATE activity SET current_participants = current_participants + :increment WHERE id = :id", nativeQuery = true)
    void updateParticipantCount(@Param("id") Long id, @Param("increment") int increment);
}