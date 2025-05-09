package com.yilena.service.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yilena.service.constant.*;
import com.yilena.service.dao.FavoritesFolderMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.dao.VideoCollectionMapper;
import com.yilena.service.entity.LoginInfo;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.UserDTO;
import com.yilena.service.entity.dto.UserPendingPageQueryDTO;
import com.yilena.service.entity.dto.UserPendingStatusDTO;
import com.yilena.service.entity.po.FavoritesFolder;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.po.VideoCollection;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.es.UserES;
import com.yilena.service.exception.LoginException;
import com.yilena.service.exception.RegisterException;
import com.yilena.service.service.UserService;
import com.yilena.service.utils.JwtUtils;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RabbitTemplate rabbitTemplate;
    private final UserES UserES;
    private final FavoritesFolderMapper favoritesFolderMapper;
    private final PasswordEncoder passwordEncoder;
    private final SnowFlake snowFlake;
    private final VideoCollectionMapper videoCollectionMapper;
    private final RedisTemplate redisTemplate;

    @Override
    public LoginInfo login(User user) {
        // 校验用户名密码
        if(user.getUsername() == null || user.getPassword() == null){
            throw new LoginException(UserConstant.USER_EMPTY_FAIL);
        }
        User dbUser = userMapper.getByUsername(user.getUsername());
        // 没找到用户
        if(dbUser == null){
            throw new LoginException(UserConstant.USER_LOGIN_INEXIST_FAIL);
        } else {

            if(dbUser.getStatus().equals(StatusConstant.STATUS_NO)){
                throw new LoginException(UserConstant.USER_CAN_NOT_LOGIN);
            }

            // 密码解密后进行匹配
            if(passwordEncoder.matches(user.getPassword(), dbUser.getPassword())){
                // 生成jwt令牌
                Map<String, Object> claims = Map.of("id",dbUser.getId(),"username", dbUser.getUsername());
                String jwt = JwtUtils.generateToken(claims);
                return new LoginInfo(dbUser.getUsername(), dbUser.getPassword(), jwt);
            } else {
                throw new LoginException(UserConstant.USER_LOGIN_FAIL);
            }
        }
    }

    @Transactional
    @Override
    public void register(User user) {
        // 校验用户名密码
        if(user.getUsername() == null || user.getPassword() == null){
            throw new RegisterException(UserConstant.USER_EMPTY_FAIL);
        }
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置默认值
        user.setStatus(StatusConstant.STATUS_YES);
        user.setUpdatedTime(LocalDateTime.now());
        user.setCreatedTime(LocalDateTime.now());
        user.setPostCount(0);
        user.setVideoCount(0);
        user.setImage("https://yilena-practice-project.oss-cn-beijing.aliyuncs.com/Yilena-video-web/25/04/05-8746f4d1a8404cc5a05c50a4079eda37.jpg");
        user.setFollowersCount(0);
        user.setFollowingCount(0);
        user.setCoins(0);
        user.setDescription("这家伙很懒，还没有简介……");
        user.setIsRecord(StatusConstant.STATUS_YES);

        Long id = snowFlake.getID();
        user.setId(id);
        userMapper.addUser(user);

        // 异步发送消息到队列，添加到es索引库
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setType(MqConstant.SEND_ADD);
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.USER_BINDING_KEY, userVO);

        // 新增默认收藏夹
        FavoritesFolder favoritesFolder = new FavoritesFolder();
        favoritesFolder.setId(snowFlake.getID());
        favoritesFolder.setTitle(DefaultConstant.DEFAULT_FAVORITES_TITLE);
        favoritesFolder.setUserId(id);
        favoritesFolder.setVideoIdsJson("[]");
        favoritesFolder.setSubscribersJson("[]");
        favoritesFolder.setVisibility(StatusConstant.STATUS_YES);
        favoritesFolder.setCreatedTime(LocalDateTime.now());
        favoritesFolder.setIsCoverAuto(StatusConstant.STATUS_YES);
        favoritesFolder.setCoverUrl(CoverUrlConstant.NULL);
        favoritesFolder.setVideoCount(0);
        favoritesFolder.setClicks(0);
        favoritesFolder.setUpdatedTime(LocalDateTime.now());
        // 设置为默认
        favoritesFolder.setIsDefault(StatusConstant.STATUS_YES);
        favoritesFolderMapper.addFavoritesFolder(favoritesFolder);

        //新增默认投稿合集
        VideoCollection videoCollection = new VideoCollection();
        videoCollection.setId(snowFlake.getID());
        videoCollection.setTitle(DefaultConstant.DEFAULT_COLLECTION_TITLE);
        videoCollection.setUserId(id);
        videoCollection.setVideoIdsJson("[]");
        videoCollection.setVisibility(StatusConstant.STATUS_YES);
        videoCollection.setIsDefault(StatusConstant.STATUS_YES);
        videoCollection.setCreatedTime(LocalDateTime.now());
        videoCollection.setUpdatedTime(LocalDateTime.now());
        videoCollectionMapper.addVideoCollection(videoCollection);
    }

    @Override
    public PageResult<User> getUserByPage(UserDTO userDTO) {
        // mysql查询
//        PageHelper.startPage(userDTO.getPage(), userDTO.getPageSize());
//        List<User> users = userMapper.getUserByPage(userDTO);
//        Page<User> p = (Page<User>) users;

        // 全局模糊查询用es，id具体查询再用sql
        return UserES.getUserByPage(userDTO);
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    @Transactional
    @Override
    public void updateUserById(User user) {

        User dbUser = userMapper.getUserById(user.getId());
        if(dbUser == null){
            throw new RuntimeException(UserConstant.USER_LOGIN_INEXIST_FAIL);
        }
        // 删除redis缓存
        redisTemplate.delete("user::" + dbUser.getUsername());
        redisTemplate.delete("user::" + dbUser.getId());


        if(user.getOldPassword() != null){
            if(user.getOldPassword().equals(dbUser.getPassword())){
                throw new RuntimeException(UserConstant.USER_PASSWORD_SAME_FAIL);
            }
            if(passwordEncoder.matches(user.getOldPassword(), dbUser.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }else{
                throw new RuntimeException(UserConstant.USER_PASSWORD_WRONG);
            }
        }else{
            // 密码为空，不修改
            user.setPassword(null);
        }

        user.setUpdatedTime(LocalDateTime.now());
        userMapper.updateUserById(user);

        // 异步发送消息到队列
        // 这里依旧需要转化成UserVO，因为我们需要展示的数据并没有那么多
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setType(MqConstant.SEND_PUT);
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.USER_BINDING_KEY, userVO);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    @Transactional
    @Override
    public void updateUserStatus(UserPendingStatusDTO userPendingStatusDTO) {
        User user = userMapper.getUserById(userPendingStatusDTO.getId());
        if(userPendingStatusDTO.getStatus().equals(StatusConstant.STATUS_YES)){
            user.setStatus(StatusConstant.STATUS_YES);
            // 移除原因
            user.setReason("");
            userMapper.updateUserById(user);
        } else {
            user.setStatus(StatusConstant.STATUS_NO);
            // 设置原因
            user.setReason(userPendingStatusDTO.getReason());
            userMapper.updateUserById(user);
        }

        // 异步发送消息到队列
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setType(MqConstant.SEND_PUT);
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.USER_BINDING_KEY, userVO);
    }

    @Override
    public LoginInfo getToken(Long id) {
        User dbUser = userMapper.getUserById(id);
        // 生成jwt令牌
        Map<String, Object> claims = Map.of("id",dbUser.getId(),"username", dbUser.getUsername());
        String jwt = JwtUtils.generateToken(claims);
        return new LoginInfo(dbUser.getUsername(), dbUser.getPassword(), jwt);
    }

    @Override
    public PageResult<User> getUserPendingByPage(UserPendingPageQueryDTO userPendingPageQueryDTO) {
        PageHelper.startPage(userPendingPageQueryDTO.getPage(), userPendingPageQueryDTO.getPageSize());
        List<User> users = userMapper.getUserPendingByPage(userPendingPageQueryDTO);
        Page<User> p = (Page<User>) users;
        return new PageResult<>(p.getTotal(),p.getResult());
    }
}
