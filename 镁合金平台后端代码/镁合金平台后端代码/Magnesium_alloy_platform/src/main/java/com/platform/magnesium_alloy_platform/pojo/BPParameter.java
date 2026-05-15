package com.platform.magnesium_alloy_platform.pojo;

import java.time.LocalDateTime;

public class BPParameter {
    private Integer id;
    private Integer inputLayer;
    private Integer outputLayer;
    private Integer intermediateLayer;
    private String options;
    private Integer numberOfCycles;
    private Double learningRate;
    private Double errorTargetValue;
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

    public Integer getInputLayer() {
        return inputLayer;
    }

    public void setInputLayer(Integer inputLayer) {
        this.inputLayer = inputLayer;
    }

    public Integer getOutputLayer() {
        return outputLayer;
    }

    public void setOutputLayer(Integer outputLayer) {
        this.outputLayer = outputLayer;
    }

    public Integer getIntermediateLayer() {
        return intermediateLayer;
    }

    public void setIntermediateLayer(Integer intermediateLayer) {
        this.intermediateLayer = intermediateLayer;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public Integer getNumberOfCycles() {
        return numberOfCycles;
    }

    public void setNumberOfCycles(Integer numberOfCycles) {
        this.numberOfCycles = numberOfCycles;
    }

    public Double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(Double learningRate) {
        this.learningRate = learningRate;
    }

    public Double getErrorTargetValue() {
        return errorTargetValue;
    }

    public void setErrorTargetValue(Double errorTargetValue) {
        this.errorTargetValue = errorTargetValue;
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
