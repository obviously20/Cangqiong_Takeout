package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @ApiOperation(value = "文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        // 上传文件到OSS
        log.info("开始上传文件{}到OSS",file);

        try {
            //获取初始文件名
            String originalFilename = file.getOriginalFilename();
            //将源文件名的后缀名提取出来
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            //生成随机文件名
            String randomFilename = UUID.randomUUID().toString().replaceAll("-","") + suffix;
            //上传文件到OSS
            String url = aliOssUtil.upload(file.getBytes(), randomFilename);
            //返回文件上传成功后的URL
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);

    }

}
