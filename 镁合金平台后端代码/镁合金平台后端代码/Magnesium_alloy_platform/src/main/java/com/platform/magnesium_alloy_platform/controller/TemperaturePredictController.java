package com.platform.magnesium_alloy_platform.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.platform.magnesium_alloy_platform.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import com.platform.magnesium_alloy_platform.mapper.TempPredictHistoryMapper;
import com.platform.magnesium_alloy_platform.pojo.TempPredictHistory;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RestController
public class TemperaturePredictController {

    @Autowired
    private TempPredictHistoryMapper historyMapper;

    @PostMapping("/predictTemperature")
    public Result predictTemperature(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Received file for temperature prediction: {}", file.getOriginalFilename());
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            // 调用 Python 接口
            String pythonUrl = "http://127.0.0.1:5000/predict_temperature";
            ResponseEntity<String> response = restTemplate.postForEntity(pythonUrl, requestEntity, String.class);
            
            // 解析 Python 返回的 JSON
            JSONObject jsonObject = JSON.parseObject(response.getBody());
            if (jsonObject != null && jsonObject.getInteger("code") == 1) {
                Double temp = jsonObject.getJSONObject("data").getDouble("temperature");
                
                // 记录到数据库
                TempPredictHistory history = new TempPredictHistory();
                history.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
                history.setTemp(temp);
                history.setTime(LocalDateTime.now());
                try {
                    historyMapper.insert(history);
                } catch (Exception e) {
                    log.error("Failed to save history: ", e);
                }
                
                return Result.success(jsonObject.getJSONObject("data"));
            } else {
                return Result.error(jsonObject != null ? jsonObject.getString("msg") : "调用模型预测失败");
            }
        } catch (Exception e) {
            log.error("温度预测异常", e);
            return Result.error("预测失败: " + e.getMessage());
        }
    }

    @GetMapping("/tempHistory/list")
    public Result listHistory() {
        try {
            List<TempPredictHistory> list = historyMapper.listAll();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取历史记录异常", e);
            return Result.error("获取历史记录失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/tempHistory/delete/{id}")
    public Result deleteHistory(@PathVariable Integer id) {
        try {
            historyMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除历史记录异常", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/tempHistory/batchDelete")
    public Result batchDeleteHistory(@RequestBody List<Integer> ids) {
        try {
            if (ids != null && !ids.isEmpty()) {
                historyMapper.deleteByIds(ids);
            }
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除历史记录异常", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }
}
