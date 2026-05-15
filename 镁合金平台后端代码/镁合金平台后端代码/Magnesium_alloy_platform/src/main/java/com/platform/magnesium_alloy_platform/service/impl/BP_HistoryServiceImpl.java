package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.BP_HistoryMapper;
import com.platform.magnesium_alloy_platform.service.BP_HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BP_HistoryServiceImpl implements BP_HistoryService {

    @Autowired
    private BP_HistoryMapper historyBP;

    /**
     * 根据ID列表删除记录
     *
     * @param ids ID列表
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteByIds(List<Integer> ids) {
        // 健壮性检查：如果列表为空或为null，直接返回true或false，避免执行无效的SQL
        if (ids == null || ids.isEmpty()) {
            // 这里的返回值取决于你的业务约定：
            // 返回true表示"没有需要删除的东西，所以任务成功完成"
            // 返回false表示"删除操作未执行"
            return true;
        }

        // 检查 ids 列表中的元素是否为 null，并移除
        ids.removeIf(id -> id == null);

        // 如果过滤后列表为空，也直接返回
        if (ids.isEmpty()) {
            return true;
        }

        // 执行删除操作，并判断是否有行数受影响
        return historyBP.deleteByIds(ids) > 0;
    }

    /**
     * 根据条件筛选血压历史记录
     *
     * @param uid             用户ID
     * @param minAveLength    最小平均长度
     * @param maxAveLength    最大平均长度
     * @param minLisanValue   最小离散值
     * @param maxLisanValue   最大离散值
     * @return 筛选后的记录列表
     */
    @Override
    public List<Map<String, Object>> filterBPHistory(
            String uid,
            Double minAveLength,
            Double maxAveLength,
            Double minLisanValue,
            Double maxLisanValue) {
        return historyBP.filterBPHistory(uid, minAveLength, maxAveLength, minLisanValue, maxLisanValue);
    }
}