package com.yilena.service.es;

import com.yilena.service.constant.EsConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.UserDTO;
import com.yilena.service.entity.po.User;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.exception.EsException;
import lombok.RequiredArgsConstructor;
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
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserES {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    public void addUser(UserVO userVO) {
        log.info("添加用户到ES");
        // 使用 Jackson 将对象转换为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String doc = null;
        try {
            doc = objectMapper.writeValueAsString(userVO);
        } catch (IOException e) {
            throw new EsException(EsConstant.TOJSON_FAIL);
        }

        IndexRequest indexRequest = new IndexRequest("user")
                .id(String.valueOf(userVO.getId()))
                .source(doc, XContentType.JSON);

        try {
            client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.SEND_FAIL);
        }
    }

    public PageResult<User> getUserByPage(UserDTO userDTO){
        log.info("用户查询用户分页信息");
        try {
            // 1. 构建搜索请求
            SearchRequest searchRequest = new SearchRequest("user"); // ES 索引名
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 2. 构建查询条件（根据 UserDTO 参数动态添加）
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().minimumShouldMatch(1);
            if (userDTO.getId() != null) {
                boolQuery.should(QueryBuilders.termQuery("id", userDTO.getId()));
            }
            if (StringUtils.isNotEmpty(userDTO.getUsername())) {
                boolQuery.should(QueryBuilders.matchQuery("username", userDTO.getUsername()));
            }
            sourceBuilder.query(boolQuery);

            // 3. 分页参数
            int from = (userDTO.getPage() - 1) * userDTO.getPageSize();
            sourceBuilder.from(from);
            sourceBuilder.size(userDTO.getPageSize());

            // 排序方式
            switch (userDTO.getSortType()) {
                case 1: // 相关度倒序
                    sourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));
                    break;
                case 2: // 粉丝量倒序
                    sourceBuilder.sort(SortBuilders.fieldSort("followersCount").order(SortOrder.DESC));
                    break;
                case 3: // 粉丝量正序
                    sourceBuilder.sort(SortBuilders.fieldSort("followersCount").order(SortOrder.ASC));
                    break;
                default: // 默认相关度
                    sourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));
            }

            // 4. 执行查询
            searchRequest.source(sourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // 5. 解析结果
            List<User> userList = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                User user = objectMapper.readValue(hit.getSourceAsString(), User.class); // 使用 JSON 工具类反序列化
                userList.add(user);
            }

            // 6. 获取总记录数
            long totalHits = response.getHits().getTotalHits().value;

            return new PageResult<>(totalHits, userList);
        } catch (IOException e) {
            throw new EsException(EsConstant.FIND_FAIL);
        }
    }

    public void updateUser(UserVO userVO){
        log.info("更新用户信息");
        User user = userMapper.getUserById(userVO.getId());
        if (user == null) {
            throw new EsException(EsConstant.USER_EMPTY_FAIL);
        }
        BeanUtils.copyProperties(user, userVO);
        UpdateRequest updateRequest = new UpdateRequest("user", String.valueOf(userVO.getId()));
        try {
            String doc = objectMapper.writeValueAsString(userVO);
            updateRequest.doc(doc, XContentType.JSON);
            client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.UPDATE_FAIL);
        }
    }
}
