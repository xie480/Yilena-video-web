package com.yilena.service.es;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yilena.service.constant.EsConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.CommentMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.FavoritesFolderVideosDTO;
import com.yilena.service.entity.dto.VideoDTO;
import com.yilena.service.entity.po.Comment;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.vo.VideoVO;
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
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoES {

    private final RestHighLevelClient client;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final CommentMapper commentMapper;
    private final RedisTemplate redisTemplate;

    public PageResult<VideoVO> getVideoByPage(VideoDTO videoDTO) {
        log.info("查询视频分页信息");
        try {
            // 1. 构建搜索请求
            SearchRequest searchRequest = new SearchRequest("video"); // ES 索引名
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 2. 构建查询条件（根据 UserDTO 参数动态添加）
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().minimumShouldMatch(1);
            if (videoDTO.getId() != null) {
                boolQuery.should(QueryBuilders.termQuery("id", videoDTO.getId()));
            }
            if (StringUtils.isNotEmpty(videoDTO.getUsername())) {
                boolQuery.should(QueryBuilders.matchQuery("username", videoDTO.getUsername()));
            }
            if (StringUtils.isNotEmpty(videoDTO.getTitle())) {
                boolQuery.should(QueryBuilders.matchQuery("title", videoDTO.getTitle()));
            }
            if (StringUtils.isNotEmpty(videoDTO.getDescription())) {
                boolQuery.should(QueryBuilders.matchQuery("description", videoDTO.getDescription()));
            }
            if (videoDTO.getTags() != null && !videoDTO.getTags().isEmpty()) {
                boolQuery.should(QueryBuilders.termsQuery("tags", videoDTO.getTags()));
            }
            if (videoDTO.getBeginDate() != null || videoDTO.getEndDate() != null) {
                RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("createdTime");
                if (videoDTO.getBeginDate() != null) {
                    rangeQuery.gte(videoDTO.getBeginDate()); // beginDate可以是Date或时间戳
                }
                if (videoDTO.getEndDate() != null) {
                    rangeQuery.lte(videoDTO.getEndDate());
                }
                boolQuery.filter(rangeQuery);
            }
            if (videoDTO.getBeginTime() != null || videoDTO.getEndTime() != null) {
                RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("time");
                if (videoDTO.getBeginTime() != null) {
                    rangeQuery.gte(videoDTO.getBeginTime()); // beginDate可以是Date或时间戳
                }
                if (videoDTO.getEndTime() != null) {
                    rangeQuery.lte(videoDTO.getEndTime());
                }
                boolQuery.filter(rangeQuery);
            }
            sourceBuilder.query(boolQuery);

            // 3. 分页参数
            int from = (videoDTO.getPage() - 1) * videoDTO.getPageSize();
            sourceBuilder.from(from);
            sourceBuilder.size(videoDTO.getPageSize());

            // 排序方式
            switch (videoDTO.getSortType()) {
                case 1: // 相关度倒序
                    sourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));
                    break;
                case 2: // 播放量倒序
                    sourceBuilder.sort(SortBuilders.fieldSort("views").order(SortOrder.DESC));
                    break;
                case 3: // 创建时间倒序
                    sourceBuilder.sort(SortBuilders.fieldSort("createdTime").order(SortOrder.DESC));
                    break;
                case 4: // 弹幕量倒序
                    sourceBuilder.sort(SortBuilders.fieldSort("barrages").order(SortOrder.DESC));
                    break;
                case 5: // 收藏量倒序
                    sourceBuilder.sort(SortBuilders.fieldSort("favorites").order(SortOrder.DESC));
                    break;
                default: // 默认相关度
                    sourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));
            }

            // 4. 执行查询
            searchRequest.source(sourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // 5. 解析结果
            List<VideoVO> videoList = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                VideoVO videoVO1 = objectMapper.readValue(hit.getSourceAsString(), VideoVO.class); // 使用 JSON 工具类反序列化
                videoList.add(videoVO1);
            }

            // 6. 获取总记录数
            long totalHits = response.getHits().getTotalHits().value;

            return new PageResult<>(totalHits, videoList);
        } catch (IOException e) {
            throw new EsException(EsConstant.FIND_FAIL);
        }
    }

    public void addVideo(VideoVO videoVO) {
        log.info("添加视频{}", videoVO);

        //检测视频是否是二次上架，如果是则加载之前的评论
        List<Comment> comments = commentMapper.getCommentByLastId(videoVO.getLastId());
        if(comments != null){
            for (Comment comment : comments) {
                comment.setEntityId(videoVO.getId());
                commentMapper.updateComment(comment);
            }
        }

        User u = userMapper.getUserById(videoVO.getUserId());

        videoVO.setUsername(u.getUsername());

        //用键值对来传入数组
        Map<String, Object> doc = new HashMap<>();
        try {
            doc.put("id", videoVO.getId());
            doc.put("title", videoVO.getTitle());
            doc.put("description", videoVO.getDescription());
            doc.put("coverUrl", videoVO.getCoverUrl());
            doc.put("tags", videoVO.getTags());
            doc.put("barrages", videoVO.getBarrages());
            doc.put("views", videoVO.getViews());
            doc.put("time", videoVO.getTime());
            doc.put("userId", videoVO.getUserId());
            doc.put("username", videoVO.getUsername());
            doc.put("createdTime", videoVO.getCreatedTime());
            doc.put("favorites", videoVO.getFavorites());
            doc.put("type", videoVO.getType());
            doc.put("visibility", videoVO.getVisibility());
        } catch (NullPointerException e) {
            throw new EsException(EsConstant.SEND_FAIL);
        }

        IndexRequest indexRequest = new IndexRequest("video")
                .id(String.valueOf(videoVO.getId()))
                .source(doc, XContentType.JSON);

        try {
            client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.SEND_FAIL);
        }
    }

    public void updateVideo(VideoVO videoVO) {
        log.info("更新视频{}", videoVO);
        Video v = videoMapper.getVideoById(videoVO.getId());
        if (v == null) {
            log.warn("未找到视频记录");
        }
        //转换
        BeanUtils.copyProperties(v,videoVO);
        String tagsJson = v.getTagsJson();
        videoVO.setTags(JSON.parseArray(tagsJson, String.class));

        User u = userMapper.getUserById(videoVO.getUserId());
        videoVO.setUsername(u.getUsername());

        UpdateRequest updateRequest = new UpdateRequest("video", String.valueOf(videoVO.getId()));

        try {
            String doc = objectMapper.writeValueAsString(videoVO);
            updateRequest.doc(doc, XContentType.JSON);
            client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.UPDATE_FAIL);
        }
    }

    public void deleteVideo(VideoVO videoVO) {
        log.info("删除视频{}", videoVO);

        // 同时需要删除原视频弹幕
        String pattern = "videoDanmu:" + videoVO.getId();
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        try {
            DeleteRequest deleteRequest = new DeleteRequest("video", String.valueOf(videoVO.getId()));
            client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.DELETE_FAIL);
        }
    }

    public PageResult<VideoVO> getVideosByIds(FavoritesFolderVideosDTO favoritesFolderVideosDTO) {
        log.info("分页查询视频信息");
        try {
            SearchRequest searchRequest = new SearchRequest("video");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();


            if (favoritesFolderVideosDTO.getVideoIds() != null) {
                boolQuery.filter(QueryBuilders.termsQuery("id", favoritesFolderVideosDTO.getVideoIds()));
            }
            if (StringUtils.isNotEmpty(favoritesFolderVideosDTO.getTitle())) {
                boolQuery.must(QueryBuilders.matchQuery("title", favoritesFolderVideosDTO.getTitle())
                        .analyzer("ik_max_word"));
            }
            sourceBuilder.query(boolQuery);

            // 3. 分页参数
            int from = (favoritesFolderVideosDTO.getPage() - 1) * favoritesFolderVideosDTO.getPageSize();
            sourceBuilder.from(from);
            sourceBuilder.size(favoritesFolderVideosDTO.getPageSize());

            System.out.println("标题：" + favoritesFolderVideosDTO.getTitle());
            // 如果不是搜索就按收藏顺序排序
            if(Objects.equals(favoritesFolderVideosDTO.getTitle(), "")) {
                // 在构建查询后添加自定义排序逻辑
                Script script = new Script(
                        ScriptType.INLINE, "painless",
                        "params.videoIds.indexOf(doc['id'].value)",  // 根据ID在数组中的位置排序
                        Collections.singletonMap("videoIds", favoritesFolderVideosDTO.getVideoIds())
                );
                ScriptSortBuilder sortBuilder = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER)
                        .order(SortOrder.DESC);  // 按ID在数组中的升序排列
                sourceBuilder.sort(sortBuilder);
            }


            // 4. 执行查询
            searchRequest.source(sourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // 5. 解析结果
            List<VideoVO> videoList = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                VideoVO videoVO1 = objectMapper.readValue(hit.getSourceAsString(), VideoVO.class); // 使用 JSON 工具类反序列化
                videoList.add(videoVO1);
            }

            // 6. 获取总记录数
            long totalHits = response.getHits().getTotalHits().value;

            return new PageResult<>(totalHits, videoList);
        } catch (IOException e) {
            throw new EsException(EsConstant.FIND_FAIL);
        }
    }

    public List<VideoVO> getAllVideos() {
        log.info("获取所有视频");
        try {
            // 1. 构建搜索请求
            SearchRequest searchRequest = new SearchRequest("video"); // ES 索引名
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 2. 构建查询条件（根据 UserDTO 参数动态添加）
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

           boolQuery.must(QueryBuilders.termQuery("visibility", StatusConstant.STATUS_YES));

            sourceBuilder.query(boolQuery);
            sourceBuilder.size(1000);

            // 4. 执行查询
            searchRequest.source(sourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // 5. 解析结果
            List<VideoVO> videoList = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                VideoVO videoVO1 = objectMapper.readValue(hit.getSourceAsString(), VideoVO.class); // 使用 JSON 工具类反序列化
                videoList.add(videoVO1);
            }

            return videoList;
        } catch (IOException e) {
            throw new EsException(EsConstant.FIND_FAIL);
        }
    }
}
