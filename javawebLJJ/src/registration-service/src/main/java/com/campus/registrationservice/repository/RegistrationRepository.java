package com.campus.registrationservice.repository;

import com.campus.registrationservice.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 报名Repository
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    
    // 根据活动ID和用户ID查找报名记录
    Optional<Registration> findByActivityIdAndUserId(Long activityId, Long userId);
    
    // 根据活动ID查找报名记录
    List<Registration> findByActivityId(Long activityId);
    
    // 根据用户ID查找报名记录
    List<Registration> findByUserId(Long userId);
    
    // 根据活动ID和状态查找报名记录
    List<Registration> findByActivityIdAndStatus(Long activityId, Integer status);
    
    // 根据用户ID和状态查找报名记录
    List<Registration> findByUserIdAndStatus(Long userId, Integer status);
    
    // 根据状态查找报名记录
    List<Registration> findByStatus(Integer status);
    
    // 根据活动ID和状态统计报名人数
    long countByActivityIdAndStatus(Long activityId, Integer status);
    
    // 检查用户是否已报名某个活动
    boolean existsByActivityIdAndUserIdAndStatusIn(Long activityId, Long userId, List<Integer> statuses);
    
    // 分页查找报名记录
    Page<Registration> findByActivityId(Long activityId, Pageable pageable);
    
    Page<Registration> findByUserId(Long userId, Pageable pageable);
    
    Page<Registration> findByStatus(Integer status, Pageable pageable);
    
    // 更新报名状态
    @Modifying
    @Query("UPDATE Registration r SET r.status = :status, r.reason = :reason, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("reason") String reason);
    
    // 根据活动ID和用户ID更新报名状态
    @Modifying
    @Query("UPDATE Registration r SET r.status = :status, r.reason = :reason, r.updatedAt = CURRENT_TIMESTAMP WHERE r.activityId = :activityId AND r.userId = :userId")
    int updateStatusByActivityIdAndUserId(@Param("activityId") Long activityId, 
                                         @Param("userId") Long userId, 
                                         @Param("status") Integer status, 
                                         @Param("reason") String reason);
}