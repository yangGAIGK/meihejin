package com.platform.magnesium_alloy_platform.pojo;

import java.time.LocalDateTime;

public class GAParameter {
    private Integer id;
    private Double crossoverProbability;
    private Double variationProbability;
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

    public Double getCrossoverProbability() {
        return crossoverProbability;
    }

    public void setCrossoverProbability(Double crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    public Double getVariationProbability() {
        return variationProbability;
    }

    public void setVariationProbability(Double variationProbability) {
        this.variationProbability = variationProbability;
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
