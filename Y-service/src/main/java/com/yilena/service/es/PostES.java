package com.yilena.service.es;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yilena.service.constant.EsConstant;
import com.yilena.service.dao.PostMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.PostDTO;
import com.yilena.service.entity.po.Post;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.vo.PostVO;
import com.yilena.service.exception.EsException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class PostES {

    private final PostMapper postMapper;
    private final RestHighLevelClient client;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public void addPost(PostVO postVO) {
        log.info("添加动态");

        User user = userMapper.getUserById(postVO.getUserId());

        // 设置用户信息
        postVO.setUsername(user.getUsername());
        postVO.setImage(user.getImage());

        //用键值对来传入数组
        Map<String, Object> doc = new HashMap<>();
        try {
            doc.put("id", postVO.getId());
            doc.put("title", postVO.getTitle());
            doc.put("content", postVO.getContent());
            doc.put("imageUrl", postVO.getImageUrl());
            doc.put("tags", postVO.getTags());
            doc.put("videoId", postVO.getVideoId());
            doc.put("clicks", postVO.getClicks());
            doc.put("likes", postVO.getLikes());
            doc.put("favorites", postVO.getFavorites());
            doc.put("shares", postVO.getShares());
            doc.put("comments", postVO.getComments());
            doc.put("userId", postVO.getUserId());
            doc.put("username", postVO.getUsername());
            doc.put("image", postVO.getImage());
            doc.put("postType", postVO.getPostType());
            doc.put("visibility", postVO.getVisibility());
            doc.put("createdTime", postVO.getCreatedTime());
            doc.put("updatedTime", postVO.getUpdatedTime());
        } catch (NullPointerException e) {
            throw new EsException(EsConstant.SEND_FAIL);
        }

        IndexRequest indexRequest = new IndexRequest("post")
                .id(String.valueOf(postVO.getId()))
                .source(doc, XContentType.JSON);

        try {
            client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.SEND_FAIL);
        }
    }

    public void updatePost(PostVO postVO) {
        log.info("更新动态");
        Post p = postMapper.getPostById(postVO.getId());
        if (p == null) {
            throw new EsException(EsConstant.UPDATE_FAIL);
        }
        //转换
        BeanUtils.copyProperties(p,postVO);
        String tagsJson = p.getTagsJson();
        postVO.setTags(JSON.parseArray(tagsJson, String.class));
        String imageUrlJson = p.getImageUrlJson();
        postVO.setImageUrl(JSON.parseArray(imageUrlJson, String.class));

        User u = userMapper.getUserById(postVO.getUserId());
        postVO.setUsername(u.getUsername());
        postVO.setImage(u.getImage());

        UpdateRequest updateRequest = new UpdateRequest("post", String.valueOf(postVO.getId()));

        try {
            String doc = objectMapper.writeValueAsString(postVO);
            updateRequest.doc(doc, XContentType.JSON);
            client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.UPDATE_FAIL);
        }
    }

    public void deletePost(Long id) {
        log.info("删除动态");
        try {
            DeleteRequest deleteRequest = new DeleteRequest("post", String.valueOf(id));
            client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.DELETE_FAIL);
        }
    }

    public PageResult<PostVO> getPostByPage(PostDTO postDTO) {
        log.info("分页获取动态");
        try {
            // 1. 构建搜索请求
            SearchRequest searchRequest = new SearchRequest("post"); // ES 索引名
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 2. 构建查询条件（根据 UserDTO 参数动态添加）
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (postDTO.getId() != null) {
                boolQuery.must(QueryBuilders.termQuery("id", postDTO.getId()));
            }
            if (StringUtils.isNotEmpty(postDTO.getUsername())) {
                boolQuery.must(QueryBuilders.matchQuery("username", postDTO.getUsername()));
            }
            if (StringUtils.isNotEmpty(postDTO.getTitle())) {
                boolQuery.must(QueryBuilders.matchQuery("title", postDTO.getTitle()));
            }
            if (StringUtils.isNotEmpty(postDTO.getContent())) {
                boolQuery.must(QueryBuilders.matchQuery("content", postDTO.getContent()));
            }
            if (postDTO.getTags() != null && !postDTO.getTags().isEmpty()) {
                boolQuery.must(QueryBuilders.termsQuery("tags", postDTO.getTags()));
            }
            sourceBuilder.query(boolQuery);

            // 3. 分页参数
            int from = (postDTO.getPage() - 1) * postDTO.getPageSize();
            sourceBuilder.from(from);
            sourceBuilder.size(postDTO.getPageSize());

            // 4. 执行查询
            searchRequest.source(sourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // 5. 解析结果
            List<PostVO> postList = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                PostVO postVO1 = objectMapper.readValue(hit.getSourceAsString(), PostVO.class); // 使用 JSON 工具类反序列化
                postList.add(postVO1);
            }

            // 6. 获取总记录数
            long totalHits = response.getHits().getTotalHits().value;

            return new PageResult<>(totalHits, postList);
        } catch (IOException e) {
            throw new EsException(EsConstant.FIND_FAIL);
        }
    }
}
