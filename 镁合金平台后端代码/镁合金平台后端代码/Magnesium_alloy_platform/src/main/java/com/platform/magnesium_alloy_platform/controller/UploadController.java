package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
public class UploadController {
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @PostMapping("/user/upload")
    public Result upload(@RequestParam("file") MultipartFile image) throws IOException {
        String url = aliOSSUtils.upload(image);
        log.info("文件上传完成，文件访问url：{}",url);
        return Result.success(url);
    }
}
