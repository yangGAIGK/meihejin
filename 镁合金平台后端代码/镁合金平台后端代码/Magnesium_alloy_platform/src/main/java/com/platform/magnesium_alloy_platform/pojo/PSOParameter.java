package com.platform.magnesium_alloy_platform.pojo;

import java.time.LocalDateTime;

public class PSOParameter {
    private Integer id;
    private Integer maxNum;
    private Integer topValue;
    private Integer lowValue;
    private Integer swarmSize;
    private Integer individualFactor;
    private Integer groupFactor;
    private Double inertiaFactor;
    private String createUser;  // 修改为 String 类型
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(Integer maxNum) {
        this.maxNum = maxNum;
    }

    public Integer getTopValue() {
        return topValue;
    }

    public void setTopValue(Integer topValue) {
        this.topValue = topValue;
    }

    public Integer getLowValue() {
        return lowValue;
    }

    public void setLowValue(Integer lowValue) {
        this.lowValue = lowValue;
    }

    public Integer getSwarmSize() {
        return swarmSize;
    }

    public void setSwarmSize(Integer swarmSize) {
        this.swarmSize = swarmSize;
    }

    public Integer getIndividualFactor() {
        return individualFactor;
    }

    public void setIndividualFactor(Integer individualFactor) {
        this.individualFactor = individualFactor;
    }

    public Integer getGroupFactor() {
        return groupFactor;
    }

    public void setGroupFactor(Integer groupFactor) {
        this.groupFactor = groupFactor;
    }

    public Double getInertiaFactor() {
        return inertiaFactor;
    }

    public void setInertiaFactor(Double inertiaFactor) {
        this.inertiaFactor = inertiaFactor;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
