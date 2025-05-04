package com.yilena.service.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yilena.service.constant.EsConstant;
import com.yilena.service.dao.FollowMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.FollowPageDTO;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.po.UserFollowing;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.exception.EsException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class FollowES {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    public void add(UserFollowing userFollowing) {
        log.info("添加关注");
        // 更新被关注用户粉丝数
        User followed = userMapper.getUserById(userFollowing.getFollowedId());
        UserVO u = new UserVO();
        if (followed != null) {
            Integer followersCount = followed.getFollowersCount();
            followed.setFollowersCount(followersCount + 1);
            userMapper.updateUserById(followed);
            BeanUtils.copyProperties(followed, u);
        } else {
            log.info(EsConstant.USER_EMPTY_FAIL);
        }
        UpdateRequest updateRequest = new UpdateRequest("user", String.valueOf(u.getId()));
        try {
            String doc = objectMapper.writeValueAsString(u);
            updateRequest.doc(doc, XContentType.JSON);
            client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.UPDATE_FAIL);
        }

        // 更新关注用户关注数
        User follower = userMapper.getUserById(userFollowing.getFollowerId());
        UserVO u2 = new UserVO();
        if (follower != null) {
            Integer followingCount = follower.getFollowingCount();
            follower.setFollowingCount(followingCount + 1);
            userMapper.updateUserById(follower);
            BeanUtils.copyProperties(follower, u2);
        } else {
            log.info(EsConstant.USER_EMPTY_FAIL);
        }
        UpdateRequest updateRequest2 = new UpdateRequest("user", String.valueOf(u2.getId()));
        try {
            String doc = objectMapper.writeValueAsString(u2);
            updateRequest2.doc(doc, XContentType.JSON);
            client.update(updateRequest2, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.UPDATE_FAIL);
        }
    }

    public void delete(UserFollowing userFollowing) {
        log.info("取消关注");
        // 更新被关注用户粉丝数
        User followed = userMapper.getUserById(userFollowing.getFollowedId());
        UserVO u = new UserVO();
        if (followed != null) {
            Integer followersCount = followed.getFollowersCount();
            followed.setFollowersCount(followersCount - 1);
            userMapper.updateUserById(followed);
            BeanUtils.copyProperties(followed, u);
        } else {
            log.info(EsConstant.USER_EMPTY_FAIL);
        }
        UpdateRequest updateRequest = new UpdateRequest("user", String.valueOf(u.getId()));
        try {
            String doc = objectMapper.writeValueAsString(u);
            updateRequest.doc(doc, XContentType.JSON);
            client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.UPDATE_FAIL);
        }

        // 更新关注用户关注数
        User follower = userMapper.getUserById(userFollowing.getFollowerId());
        UserVO u2 = new UserVO();
        if (follower != null) {
            Integer followingCount = follower.getFollowingCount();
            follower.setFollowingCount(followingCount - 1);
            userMapper.updateUserById(follower);
            BeanUtils.copyProperties(follower, u2);
        } else {
            log.info(EsConstant.USER_EMPTY_FAIL);
        }
        UpdateRequest updateRequest2 = new UpdateRequest("user", String.valueOf(u2.getId()));
        try {
            String doc = objectMapper.writeValueAsString(u2);
            updateRequest2.doc(doc, XContentType.JSON);
            client.update(updateRequest2, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException(EsConstant.UPDATE_FAIL);
        }
    }
}
