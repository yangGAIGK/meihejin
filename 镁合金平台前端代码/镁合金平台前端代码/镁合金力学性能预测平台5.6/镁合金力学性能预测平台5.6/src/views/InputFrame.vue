<template>
    <div class="container">
        <div class="form-wrapper">
            <!-- 深度卷积网络配置表单（左侧） -->
            <div class="form-section dc">
                <div class="section-title">参数配置</div>
                <el-form :model="ruleForm" status-icon :rules="rules" ref="dcRuleForm" label-width="130px"
                    class="demo-ruleForm">
                    <!-- 网络架构参数 -->
                    <el-form-item label="编码器架构类型" prop="encoderType" class="form-item">
                        <el-select v-model="ruleForm.encoderType" placeholder="选择基础网络" class="custom-select">
                            <el-option label="SegNet_VGG16" value="SegNet_VGG16"></el-option>
                            <el-option label="ResNet50" value="ResNet50"></el-option>
                            <el-option label="MobileNetV3" value="MobileNetV3"></el-option>
                        </el-select>
                    </el-form-item>

                    <el-form-item label="解码器层数配置" prop="decoderLayers" class="form-item">
                        <el-input v-model="ruleForm.decoderLayers" type="number" min="0" step="1" placeholder="默认与编码器对称"
                            class="custom-input"></el-input>
                    </el-form-item>

                    <el-form-item label="跨层融合尺度数" prop="fusionScales" class="form-item">
                        <el-input v-model="ruleForm.fusionScales" type="number" min="1" max="5" step="1"
                            placeholder="推荐范围1-5" class="custom-input"></el-input>
                    </el-form-item>

                    <!-- 损失优化参数 -->
                    <el-form-item label="正样本权重系数" prop="posWeight" class="form-item">
                        <el-input v-model="ruleForm.posWeight" type="number" step="0.1" placeholder="裂缝权重平衡值(1.0-10.0)"
                            class="custom-input"></el-input>
                    </el-form-item>

                    <el-form-item label="优化器选择" prop="optimizerType" class="form-item">
                        <el-select v-model="ruleForm.optimizerType" placeholder="选择优化算法" class="custom-select">
                            <el-option label="Adam自适应优化" value="Adam"></el-option>
                            <el-option label="SGD随机梯度下降" value="SGD"></el-option>
                        </el-select>
                    </el-form-item>

                    <!-- 训练控制参数 -->
                    <el-form-item label="基础学习率设置" prop="baseLR" class="form-item">
                        <el-input v-model="ruleForm.baseLR" type="number" step="1e-4" placeholder="建议初始值0.0001"
                            class="custom-input"></el-input>
                    </el-form-item>

                    <el-form-item label="最大训练轮次" prop="maxEpochs" class="form-item">
                        <el-input v-model="ruleForm.maxEpochs" type="number" min="50" max="200" step="1"
                            placeholder="推荐范围50-200" class="custom-input"></el-input>
                    </el-form-item>

                    <!-- 高级功能配置 -->
                    <el-form-item label="早停机制" prop="earlyStop" class="form-item">
                        <el-switch v-model="ruleForm.earlyStop" active-text="启用" inactive-text="禁用"></el-switch>
                    </el-form-item>

                    <el-form-item class="button-group">
                        <el-button type="primary" @click="submitForm('dcRuleForm')">提交配置</el-button>
                        <el-button type="danger" @click="resetForm('dcRuleForm')">重置参数</el-button>
                        <el-button type="success" @click="loadLatestData('dcRuleForm', '/DCrecords')">
                            加载历史配置
                        </el-button>
                    </el-form-item>
                </el-form>
            </div>

            <!-- 图像上传中心（右侧） -->
            <div class="form-section pso">
                <div class="section-title">图像上传</div>
                <el-form label-width="130px" class="demo-ruleForm image-upload-form">
                    <!-- 上传区域1：裂纹样本 -->
                    <el-form-item label="裂纹样本上传" class="form-item image-upload-item">
                        <el-upload action="#" list-type="picture-card" :auto-upload="false" :limit="3"
                            accept=".jpg,.jpeg,.png,.bmp" class="custom-upload">
                            <i slot="default" class="el-icon-plus"></i>
                            <div slot="file" slot-scope="{file}">
                                <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
                                <span class="el-upload-list__item-actions">
                                    <span class="el-upload-list__item-preview" @click="handlePictureCardPreview(file)">
                                        <i class="el-icon-zoom-in"></i>
                                    </span>
                                    <span v-if="!disabled" class="el-upload-list__item-delete"
                                        @click="handleRemove(file)">
                                        <i class="el-icon-delete"></i>
                                    </span>
                                </span>
                            </div>
                        </el-upload>
                        <el-dialog :visible.sync="dialogVisible" size="tiny">
                            <img width="100%" :src="dialogImageUrl" alt="" />
                        </el-dialog>
                    </el-form-item>

                    <!-- 上传区域2：背景样本 -->
                    <el-form-item label="背景样本上传" class="form-item image-upload-item">
                        <el-upload action="#" list-type="picture-card" :auto-upload="false" :limit="5"
                            accept=".jpg,.jpeg,.png,.bmp" class="custom-upload">
                            <!-- 复用相同模板结构 -->
                        </el-upload>
                    </el-form-item>

                    <!-- 上传区域3：标注文件 -->
                    <el-form-item label="标注文件上传" class="form-item image-upload-item">
                        <el-upload action="#" class="custom-upload" :limit="1" accept=".json,.xml">
                            <el-button size="small" type="primary">
                                点击上传标注文件
                            </el-button>
                            <div slot="tip" class="el-upload__tip">
                                支持JSON/XML格式，单个文件不超过2MB
                            </div>
                        </el-upload>
                    </el-form-item>

                    <!-- 操作按钮组 -->
                    <el-form-item class="button-group">
                        <el-button type="primary" @click="handleStartProcessing">开始处理</el-button>

                        <el-button type="info" @click="handleClear">清空列表</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script>
import axios from "axios";

export default {
    data() {
        return {
            ruleForm: {
                inputLayer: null,
                outputLayer: null,
                intermediateLayer: null,
                options: "",
                numberOfCycles: null,
                learningRate: null,
                errorTargetValue: null
            },
        };
    },
};
</script>

<style lang="less" scoped>
.container {
    width: 100%;
    max-width: none;
    min-height: 100%;
    padding: 20px 40px 40px;
    background-color: var(--bg-base);
    font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
    box-sizing: border-box;
}

.form-wrapper {
    display: flex;
    justify-content: flex-start;
    gap: 24px;
    height: 100%;
}

.form-section {
    width: 50%;
    margin-bottom: 0;
    display: flex;
    flex-direction: column;
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 16px;
    color: var(--accent-primary);
    text-align: center;
    position: relative;
    padding-bottom: 12px;
    letter-spacing: 1px;

    &::after {
        content: '';
        position: absolute;
        left: 50%;
        bottom: 0;
        transform: translateX(-50%);
        width: 80px;
        height: 2px;
        background: var(--accent-gradient);
        border-radius: 2px;
    }
}

/* 核心：白色圆角卡片 → 深色卡片 */
.demo-ruleForm {
    background: var(--bg-elevated);
    border: 1px solid var(--border-subtle);
    padding: 28px;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-card);
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    position: relative;

    &::before {
        content: '';
        position: absolute;
        top: 0; left: 10%; width: 80%; height: 1px;
        background: linear-gradient(90deg, transparent, var(--accent-primary), transparent);
        opacity: 0.35;
        border-radius: var(--radius-lg);
    }
}

.form-item {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    margin-bottom: 16px;
}

.custom-input {
    display: inline-block;
    width: 230px;
    flex-grow: 1;
}

.custom-select {
    width: 230px;
    flex-grow: 1;

    .el-input { font-size: 14px; }
}

.custom-select .el-input { width: 100%; flex-grow: 1; }
.custom-select .el-input__inner { width: 100%; font-size: 14px; }

.button-group {
    display: flex;
    justify-content: center;
    margin-top: auto;
}

/* upload 区域深色 */
/deep/ .el-upload--picture-card {
    background-color: var(--bg-overlay) !important;
    border-color: var(--border-default) !important;
    color: var(--text-muted) !important;

    &:hover {
        border-color: var(--accent-primary) !important;
        color: var(--accent-primary) !important;
    }
}

/deep/ .el-upload__tip {
    color: var(--text-muted) !important;
    font-size: 12px;
}

/deep/ .el-upload-list--picture-card .el-upload-list__item {
    background-color: var(--bg-overlay) !important;
    border-color: var(--border-subtle) !important;
}

/* el-switch 深色适配 */
/deep/ .el-switch__label {
    color: var(--text-secondary) !important;
    &.is-active { color: var(--accent-primary) !important; }
}
</style>

/*  浅灰色背景  */
