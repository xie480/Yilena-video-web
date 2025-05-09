package com.yilena.service.controller.user;

import com.yilena.service.log.LogOperation;
import com.yilena.service.entity.Result;
import com.yilena.service.utils.AliyunOSSOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final AliyunOSSOperator aliyunOSSOperator;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {
        // 记录文件上传日志
        log.info("文件上传：{}", file.getOriginalFilename());
        // 检查文件是否为空
        if (!file.isEmpty()) {
            // 获取文件原始名称
            String originalFilename = file.getOriginalFilename();
            // 获取文件扩展名
            String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 调用阿里云OSS操作器上传文件，并获取文件的访问URL
            String url = aliyunOSSOperator.upload(file.getBytes(), AliyunOSSOperator.generateUniqueName(extName));
            // 返回文件上传成功的结果，包含文件的访问URL
            return Result.success(url);
        }
        // 如果文件为空，返回上传失败的结果
        return Result.error("上传失败");
    }
}
