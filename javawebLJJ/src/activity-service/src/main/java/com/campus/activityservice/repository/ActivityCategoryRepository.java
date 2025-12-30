package com.campus.activityservice.repository;

import com.campus.activityservice.entity.ActivityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 活动分类Repository
 */
@Repository
public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Integer> {
    
    // 根据父分类ID查找子分类
    List<ActivityCategory> findByParentId(Integer parentId);
    
    // 根据状态查找分类
    List<ActivityCategory> findByStatus(Integer status);
    
    // 根据父分类ID和状态查找分类
    List<ActivityCategory> findByParentIdAndStatus(Integer parentId, Integer status);
    
    // 检查分类是否存在
    boolean existsByName(String name);
}