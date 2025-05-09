package com.yilena.service.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig {

    private final RestHighLevelClient client;

    //根据spring生命周期进行销毁
    @PreDestroy
    public void destroy() {
        try {
            if (client != null) {
                client.close();
                log.info("ES连接已关闭");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct // 在 Bean 初始化后执行
    public void initIndices() {
        //post 索引
        try {
            // 1. 创建索引请求
            CreateIndexRequest request = new CreateIndexRequest("post"); // 确保与添加视频时的索引名一致

            request.settings(Settings.builder()
                    .put("analysis.analyzer.default.type", "ik_max_word") // 默认分词器
                    .put("analysis.analyzer.my_custom_analyzer.tokenizer", "ik_max_word")
                    .putList("analysis.analyzer.my_custom_analyzer.filter", "lowercase")
            );


            // 2. 定义映射结构
            Map<String, Object> properties = new HashMap<>();

            // title 字段（text类型，使用ik_max_word分词）
            Map<String, Object> titleMapping = new HashMap<>();
            titleMapping.put("type", "text");
            titleMapping.put("analyzer", "ik_max_word");
            titleMapping.put("search_analyzer", "ik_smart"); // 可选：搜索分词器
            properties.put("title", titleMapping);

            // username 字段映射
            Map<String, Object> usernameMapping = new HashMap<>();
            usernameMapping.put("type", "text");
            titleMapping.put("analyzer", "ik_max_word");
            titleMapping.put("search_analyzer", "ik_smart"); // 可选：搜索分词器
            properties.put("username", usernameMapping);

            // tags 字段映射
            Map<String, Object> tagsMapping = new HashMap<>();
            tagsMapping.put("type", "keyword");
            properties.put("tags", tagsMapping);

            // imageUrl 字段映射
            Map<String, Object> imageUrlMapping = new HashMap<>();
            imageUrlMapping.put("type", "keyword");
            properties.put("imageUrl", imageUrlMapping);

            // image 字段映射
            Map<String, Object> imageMapping = new HashMap<>();
            imageMapping.put("type", "keyword");
            properties.put("username", imageMapping);

            // symbol 字段映射
            Map<String, Object> symbolMapping = new HashMap<>();
            symbolMapping.put("type", "keyword");
            properties.put("symbol", symbolMapping);

            // 其他字段映射（按需添加）
            Map<String, Object> mapping = new HashMap<>();
            mapping.put("properties", properties);
            request.mapping(mapping);

            // 3. 执行创建索引
            client.indices().create(request, RequestOptions.DEFAULT);
            log.info("Elasticsearch 索引 post 创建成功");
        } catch (Exception e) {
            if (e.getMessage().contains("resource_already_exists_exception")) {
                log.warn("post索引已存在，无需重复创建");
            } else {
                log.error("索引创建失败", e);
            }
        }

        //user 索引
        try {
            // 1. 创建索引请求
            CreateIndexRequest request = new CreateIndexRequest("user"); // 确保与添加视频时的索引名一致

            request.settings(Settings.builder()
                    .put("analysis.analyzer.default.type", "ik_max_word") // 默认分词器
                    .put("analysis.analyzer.my_custom_analyzer.tokenizer", "ik_max_word")
                    .putList("analysis.analyzer.my_custom_analyzer.filter", "lowercase")
            );

            // 2. 定义映射结构
            Map<String, Object> properties = new HashMap<>();

            // username 字段映射
            Map<String, Object> usernameMapping = new HashMap<>();
            usernameMapping.put("type", "keyword");
            properties.put("username", usernameMapping);

            // image 字段映射
            Map<String, Object> imageMapping = new HashMap<>();
            imageMapping.put("type", "keyword");
            properties.put("username", imageMapping);

            // 其他字段映射（按需添加）
            Map<String, Object> mapping = new HashMap<>();
            mapping.put("properties", properties);
            request.mapping(mapping);

            // 3. 执行创建索引
            client.indices().create(request, RequestOptions.DEFAULT);
            log.info("Elasticsearch 索引 user 创建成功");
        } catch (Exception e) {
            if (e.getMessage().contains("resource_already_exists_exception")) {
                log.warn("user索引已存在，无需重复创建");
            } else {
                log.error("索引创建失败", e);
            }
        }


        //video 索引
        try {
            // 1. 创建索引请求
            CreateIndexRequest request = new CreateIndexRequest("video"); // 确保与添加视频时的索引名一致

            request.settings(Settings.builder()
                    .put("analysis.analyzer.default.type", "ik_max_word") // 默认分词器
                    .put("analysis.analyzer.my_custom_analyzer.tokenizer", "ik_max_word")
                    .putList("analysis.analyzer.my_custom_analyzer.filter", "lowercase")
            );

            // 2. 定义映射结构
            Map<String, Object> properties = new HashMap<>();

            // tags 字段映射
            Map<String, Object> tagsMapping = new HashMap<>();
            tagsMapping.put("type", "keyword");
            properties.put("tags", tagsMapping);

            // title 字段（text类型，使用ik_max_word分词）
            Map<String, Object> titleMapping = new HashMap<>();
            titleMapping.put("type", "text");
            titleMapping.put("analyzer", "ik_max_word");
            titleMapping.put("search_analyzer", "ik_smart"); // 可选：搜索分词器
            properties.put("title", titleMapping);

            // 其他字段映射（按需添加）
            Map<String, Object> mapping = new HashMap<>();
            mapping.put("properties", properties);
            request.mapping(mapping);

            // symbol 字段映射
            Map<String, Object> symbolMapping = new HashMap<>();
            symbolMapping.put("type", "keyword");
            properties.put("symbol", symbolMapping);

            // 3. 执行创建索引
            client.indices().create(request, RequestOptions.DEFAULT);
            log.info("Elasticsearch 索引 video 创建成功");
        } catch (Exception e) {
            if (e.getMessage().contains("resource_already_exists_exception")) {
                log.warn("video索引已存在，无需重复创建");
            } else {
                log.error("索引创建失败", e);
            }
        }
    }
}