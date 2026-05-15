package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.pojo.Excel;
import com.platform.magnesium_alloy_platform.pojo.Pojo_Data;
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/upload")//上传
/*
* 这个Java类 ExcelController
* 是一个Spring MVC的控制器，它处理与Excel文件上传和解析的HTTP请求。
* */
public class ExcelController {

    private final ExcelService excelService;
    @Autowired
    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping
    public Result uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "skipHeader", defaultValue = "true") boolean skipHeader) {

        if (file == null || file.isEmpty()) {
            log.error("上传的文件为空");
            return Result.error("上传的文件为空，请重新上传");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xls") && !originalFilename.endsWith(".xlsx"))) {
            log.error("不支持的文件格式: {}", originalFilename);
            return Result.error("仅支持 .xls 或 .xlsx 格式的文件");
        }

        try {
            // 将上传的文件保存到临时文件
            String filePath = saveTempFile(file);
            // 调用 ExcelService 解析文件
            List<Excel> objects = excelService.parseExcel(filePath, skipHeader);
            log.info("解析后的数据集合: {}", objects);
            return Result.success(objects);
        } catch (IOException e) {
            log.error("文件解析失败", e);
            return Result.error("解析失败，请检查文件内容是否正确");
        }
    }

    /**
     * 将上传的文件保存到临时目录
     * @param file 上传的文件
     * @return 临时文件路径
     * @throws IOException 文件保存失败时抛出异常
     */
    private String saveTempFile(MultipartFile file) throws IOException {
        // 创建临时文件
        File tempFile = File.createTempFile("temp", ".xlsx");

        // 将上传的Excel文件内容复制到临时文件中
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            FileCopyUtils.copy(file.getInputStream(), fos);
        }

        // 返回临时文件的路径
        return tempFile.getAbsolutePath();
    }
    @GetMapping
    public Result dataPercent(){
        Pojo_Data percentData = excelService.count();
        return Result.success(percentData);
    }

}
