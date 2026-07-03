package com.lab.equipment.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 任务分配统计VO
 */
@Data
public class TaskStatsVO {
    
    /**
     * 总报修任务数
     */
    private Long totalRepairTasks;
    
    /**
     * 待处理报修数
     */
    private Long pendingRepairTasks;
    
    /**
     * 处理中报修数
     */
    private Long processingRepairTasks;
    
    /**
     * 总维护任务数
     */
    private Long totalMaintenanceTasks;
    
    /**
     * 各老师的报修任务统计
     */
    private List<TeacherTaskCount> repairTaskCounts;
    
    /**
     * 各老师的维护任务统计
     */
    private List<TeacherTaskCount> maintenanceTaskCounts;
    
    @Data
    public static class TeacherTaskCount {
        private Long userId;
        private String userName;
        private String realName;
        private Long taskCount;
        
        public TeacherTaskCount(Long userId, String userName, String realName, Long taskCount) {
            this.userId = userId;
            this.userName = userName;
            this.realName = realName;
            this.taskCount = taskCount;
        }
    }
}
