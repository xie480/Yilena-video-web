package com.yilena.service.service.impl;

import com.alibaba.fastjson2.JSON;
import com.yilena.service.constant.*;
import com.yilena.service.dao.FavoritesFolderMapper;
import com.yilena.service.dao.VideoFavoritesMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.FavoritesFolderDoOrUndoDTO;
import com.yilena.service.entity.dto.FavoritesFolderVideosDTO;
import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.entity.po.FavoritesFolder;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.po.VideoFavorites;
import com.yilena.service.entity.vo.FavoritesFolderVO;
import com.yilena.service.entity.vo.VideoVO;
import com.yilena.service.es.VideoES;
import com.yilena.service.service.FavoritesFolderService;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.webSocket.ChatEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FavoritesFolderServiceImpl implements FavoritesFolderService {

    private final FavoritesFolderMapper favoritesFolderMapper;
    private final VideoMapper videoMapper;
    private final VideoES videoES;
    private final SnowFlake snowFlake;
    private final RabbitTemplate rabbitTemplate;
    private final VideoFavoritesMapper videoFavoritesMapper;
    private final ChatEndpoint chatEndpoint;
    private final RedisTemplate redisTemplate;

    @Override
    public void addFavoritesFolder(FavoritesFolder favoritesFolder) {
        favoritesFolder.setId(snowFlake.getID());
        favoritesFolder.setUpdatedTime(LocalDateTime.now());
        favoritesFolder.setCreatedTime(LocalDateTime.now());
        favoritesFolder.setUserId(CurrentHolder.getCurrent());
        favoritesFolder.setIsDefault(StatusConstant.STATUS_NO);
        favoritesFolder.setVideoIdsJson("[]");
        favoritesFolder.setVideoCount(0);
        favoritesFolder.setSubscribersJson("[]");
        favoritesFolder.setClicks(0);
        // 判断是否自定义封面
        String coverUrl = favoritesFolder.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            favoritesFolder.setIsCoverAuto(StatusConstant.STATUS_NO);
            favoritesFolder.setCoverUrl(coverUrl);
        } else {
            favoritesFolder.setIsCoverAuto(StatusConstant.STATUS_YES);
            favoritesFolder.setCoverUrl(CoverUrlConstant.NULL);
        }

        favoritesFolderMapper.addFavoritesFolder(favoritesFolder);
    }

    @Override
    public List<FavoritesFolderVO> getFavoritesFolderByList() {
        FavoritesFolder favoritesFolder = new FavoritesFolder();
        favoritesFolder.setUserId(CurrentHolder.getCurrent());
        return favoritesFolderMapper.getFavoritesFolderByList(favoritesFolder);
    }

    @Override
    public void updateFavoritesFolder(FavoritesFolder favoritesFolder) {
        favoritesFolder.setUpdatedTime(LocalDateTime.now());
        // 判断是否自定义封面
        String coverUrl = favoritesFolder.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            favoritesFolder.setIsCoverAuto(StatusConstant.STATUS_NO);
        } else{
            favoritesFolder.setIsCoverAuto(StatusConstant.STATUS_YES);

            // 更换最新封面
            FavoritesFolder oldFavoritesFolder = favoritesFolderMapper.getFavoritesFolderById(favoritesFolder.getId());
            List<Long> videoIds = JSON.parseArray(oldFavoritesFolder.getVideoIdsJson(), Long.class);
            Video video = videoMapper.getVideoById(videoIds.get(videoIds.size() - 1));
            favoritesFolder.setCoverUrl(video.getCoverUrl());
        }
        favoritesFolderMapper.updateFavoritesFolder(favoritesFolder);
    }

    @Transactional
    @Override
    public void doFavoritesFolder(FavoritesFolderDoOrUndoDTO favoritesFolderDoOrUndoDTO) {
        FavoritesFolder favoritesFolder = favoritesFolderMapper.getFavoritesFolderById(favoritesFolderDoOrUndoDTO.getFavoritesId());
        // 判断视频是否存在
        Video video = videoMapper.getVideoById(favoritesFolderDoOrUndoDTO.getId());
        if(video == null){
            throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
        }
        // 将json转化为list
        String videoIdsJson = favoritesFolder.getVideoIdsJson();
        List<Long> videoIds = JSON.parseArray(videoIdsJson, Long.class);
        // 判断是否已经收藏过
        videoIds.forEach(videoId -> {
            if (Objects.equals(videoId, favoritesFolderDoOrUndoDTO.getId())) {
                throw new RuntimeException(FavoritesConstant.VIDEO_ALREADY_FAVORITES);
            }
        });
        videoIds.add(favoritesFolderDoOrUndoDTO.getId());

        // 更新视频收藏数
        video.setFavorites(video.getFavorites() + 1);
        videoMapper.updateVideo(video);

        favoritesFolder.setVideoIdsJson(JSON.toJSONString(videoIds));
        favoritesFolder.setVideoCount(videoIds.size());
        if(Objects.equals(favoritesFolder.getIsCoverAuto(), StatusConstant.STATUS_YES)){
            favoritesFolder.setCoverUrl(video.getCoverUrl());
        }
        favoritesFolderMapper.updateFavoritesFolder(favoritesFolder);


        // 添加视频收藏记录
        VideoFavorites videoFavorites = new VideoFavorites();
        videoFavorites.setId(snowFlake.getID());
        videoFavorites.setVideoId(favoritesFolderDoOrUndoDTO.getId());
        videoFavorites.setFolderId(favoritesFolder.getId());
        videoFavorites.setUserId(CurrentHolder.getCurrent());
        videoFavorites.setCreatedTime(LocalDateTime.now());
        videoFavorites.setUpdatedTime(LocalDateTime.now());
        videoFavorites.setType(MqConstant.SEND_ADD);
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.VIDEO_FAVORITES_BINDING_KEY, videoFavorites);

        //群发给订阅者
        if(favoritesFolder.getVisibility().equals(StatusConstant.STATUS_YES)){
            List<Long> subscribers = JSON.parseArray(favoritesFolder.getSubscribersJson(), Long.class);
            subscribers.forEach(subscriber -> {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setCreatedTime(LocalDateTime.now());
                chatMessage.setSendUserId(favoritesFolder.getUserId());
                chatMessage.setReceiveUserId(subscriber);
                chatMessage.setType(MessageTypeConstant.FAVORITES);
                chatMessage.setVisibilityBySender(StatusConstant.STATUS_NO);
                chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
                chatMessage.setContent("你订阅的收藏夹" + favoritesFolder.getTitle() + "有新的视频，快去查看吧！");
                chatMessage.setId(snowFlake.getID());
                String json = com.alibaba.fastjson.JSON.toJSONString(chatMessage);
                chatEndpoint.sendMessage(json);
            });
        }
    }

    @Transactional
    @Override
    public void unDoFavoritesFolder(FavoritesFolderDoOrUndoDTO favoritesFolderDoOrUndoDTO) {
        // 判断视频是否存在
        Video video = videoMapper.getVideoById(favoritesFolderDoOrUndoDTO.getId());
        if(video == null){
            throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
        }

        // 判断是否已经收藏过
        VideoFavorites videoFavorites = videoFavoritesMapper.isFavoritesFolder(favoritesFolderDoOrUndoDTO.getId(), CurrentHolder.getCurrent());
        FavoritesFolder favoritesFolder = favoritesFolderMapper.getFavoritesFolderById(videoFavorites.getFolderId());
        // 将json转化为list
        String videoIdsJson = favoritesFolder.getVideoIdsJson();
        List<Long> videoIds = JSON.parseArray(videoIdsJson, Long.class);
        // 判断是否已经收藏过，并安全移除元素
        boolean isFavorites = false;
        Iterator<Long> iterator = videoIds.iterator(); // 使用 Iterator 遍历
        while (iterator.hasNext()) {
            Long id = iterator.next();
            if (Objects.equals(id, favoritesFolderDoOrUndoDTO.getId())) {
                iterator.remove(); // 通过 Iterator 的 remove 方法安全移除
                isFavorites = true;
            }
        }
        if(!isFavorites){
            throw new RuntimeException(FavoritesConstant.VIDEO_NOT_ALREADY_FAVORITES);
        }

        // 更新视频收藏数
        video.setFavorites(video.getFavorites() - 1);
        videoMapper.updateVideo(video);

        // 设置封面
        if(!videoIds.isEmpty()) {
            Video lastVideo = videoMapper.getVideoById(videoIds.get(videoIds.size() - 1));
            if(Objects.equals(favoritesFolder.getIsCoverAuto(), StatusConstant.STATUS_NO)){
                favoritesFolder.setCoverUrl(lastVideo.getCoverUrl());
            }
            favoritesFolder.setVideoIdsJson(JSON.toJSONString(videoIds));
            favoritesFolder.setVideoCount(videoIds.size());
        }else{
            favoritesFolder.setCoverUrl(CoverUrlConstant.NULL);
            favoritesFolder.setVideoIdsJson("[]");
            favoritesFolder.setVideoCount(videoIds.size());
        }
        // 更新收藏夹视频数
        favoritesFolder.setVideoCount(videoIds.size());

        favoritesFolderMapper.updateFavoritesFolder(favoritesFolder);

        videoFavoritesMapper.deleteVideoFavorites(videoFavorites);
    }

    @Override
    public List<FavoritesFolderVO> getFavoritesFolderById(Long userId) {
        FavoritesFolder favoritesFolder = new FavoritesFolder();
        favoritesFolder.setUserId(userId);
        List<FavoritesFolderVO> favoritesFolderVOS = favoritesFolderMapper.getOthersFavoritesFolderByList(favoritesFolder);
        if(favoritesFolderVOS.isEmpty()){
            return List.of();
        }

        // 移除不可见收藏夹
            for (int i = 0; i < favoritesFolderVOS.size(); i++) {
                if (Objects.equals(favoritesFolderVOS.get(i).getVisibility(), StatusConstant.STATUS_NO)) {
                    favoritesFolderVOS.remove(i);
                    i--;
                }
            }
        return favoritesFolderVOS;
    }

    @Override
    public PageResult<VideoVO> getVideosFromFavoritesFolderByPage(FavoritesFolderVideosDTO favoritesFolderVideosDTO, Long userId) {
        String videoIdsJson = favoritesFolderMapper.getFavoritesFolderById(favoritesFolderVideosDTO.getId()).getVideoIdsJson();
        List<Long> videoIds = JSON.parseArray(videoIdsJson, Long.class);
        if(videoIds.isEmpty()){
            return new PageResult<>();
        }
        favoritesFolderVideosDTO.setVideoIds(videoIds);
        PageResult<VideoVO> videos = videoES.getVideosByIds(favoritesFolderVideosDTO);

        // 更新收藏夹点击量
        FavoritesFolder favoritesFolder = favoritesFolderMapper.getFavoritesFolderById(favoritesFolderVideosDTO.getId());
        if(!Objects.equals(userId, favoritesFolder.getUserId())) {
            favoritesFolder.setClicks(favoritesFolder.getClicks() + 1);
            favoritesFolderMapper.updateFavoritesFolder(favoritesFolder);
        }
        return videos;
    }

    @Override
    public void deleteFavoritesFolder(Long id) {
        favoritesFolderMapper.deleteFavoritesFolder(id);
    }

    @Override
    public void subscribeOthersFavoritesFolder(Long id) {
        FavoritesFolder favoritesFolder = favoritesFolderMapper.getFavoritesFolderById(id);
        // 判断是否存在
        if(favoritesFolder == null){
            throw new RuntimeException(FavoritesConstant.FAVORITES_NOT_EXIST);
        }
        // 判断是否是默认收藏夹
        if(Objects.equals(favoritesFolder.getIsDefault(), StatusConstant.STATUS_YES)){
            throw new RuntimeException(FavoritesConstant.FAVORITES_NOT_DEFAULT);
        }
        // 判断是否是自己
        if(Objects.equals(favoritesFolder.getUserId(), CurrentHolder.getCurrent())){
            throw new RuntimeException(FavoritesConstant.FAVORITES_CANNOT_SUBSCRIBE_SELF);
        }
        // 判断是否已经订阅过
        String subscribersJson = favoritesFolder.getSubscribersJson();
        List<Long> subscribers = JSON.parseArray(subscribersJson, Long.class);
        if(subscribers.contains(CurrentHolder.getCurrent())){
            throw new RuntimeException(FavoritesConstant.FAVORITES_ALREADY_SUBSCRIBE);
        }
        subscribers.add(CurrentHolder.getCurrent());
        favoritesFolder.setSubscribersJson(JSON.toJSONString(subscribers));
        favoritesFolderMapper.updateFavoritesFolder(favoritesFolder);
    }

    @Override
    public VideoFavorites isFavoritesFolder(Long videoId, Long userId) {
        VideoFavorites videoFavorites =  videoFavoritesMapper.isFavoritesFolder(videoId, userId);
//        if(videoFavorites == null){
//            Video video = videoMapper.getVideoById(videoId);
//            if(video != null){
//                Long oldId = video.getLastId();
//                videoFavorites = videoFavoritesMapper.isFavoritesFolder(oldId, userId);
//                if(videoFavorites != null){
//                    // 删除旧记录
//                    VideoFavorites videoFavorites1 = new VideoFavorites();
//                    videoFavorites1.setVideoId(oldId);
//                    videoFavorites1.setUserId(userId);
//                    videoFavoritesMapper.deleteVideoFavorites(videoFavorites1);
//
//                    videoFavorites1.setId(snowFlake.getID());
//                    videoFavorites1.setFolderId(videoFavorites.getFolderId());
//                    videoFavorites1.setCreatedTime(videoFavorites.getCreatedTime());
//                    videoFavorites1.setUpdatedTime(videoFavorites.getUpdatedTime());
//                    videoFavoritesMapper.addVideoFavorites(videoFavorites1);
//                }
//            }
//        }
        return videoFavorites;
    }

    @Override
    public List<FavoritesFolderVO> getSubscribeFavoritesFolder(Long userId) {
        List<FavoritesFolderVO> favoritesFolderVOS = favoritesFolderMapper.getSubscribeFavoritesFolder(userId);
        if(favoritesFolderVOS.isEmpty()){
            return List.of();
        }
        return favoritesFolderVOS;
    }

    @Override
    public FavoritesFolder isSubscribe(Long userId, Long favoritesFolderId) {
        FavoritesFolder favoritesFolder = favoritesFolderMapper.getFavoritesFolderById(favoritesFolderId);
        List<Long> subscribers = JSON.parseArray(favoritesFolder.getSubscribersJson(), Long.class);
        if(subscribers.contains(userId)){
            return favoritesFolder;
        }else{
            return null;
        }
    }

    @Override
    public void unsubscribeOthersFavoritesFolder(Long id) {
        FavoritesFolder favoritesFolder = favoritesFolderMapper.getFavoritesFolderById(id);
        if(favoritesFolder == null){
            throw new RuntimeException(FavoritesConstant.FAVORITES_NOT_EXIST);
        }
        List<Long> subscribers = JSON.parseArray(favoritesFolder.getSubscribersJson(), Long.class);
        if(!subscribers.contains(CurrentHolder.getCurrent())){
            throw new RuntimeException(FavoritesConstant.FAVORITES_NOT_SUBSCRIBE);
        }
        subscribers.remove(CurrentHolder.getCurrent());
        favoritesFolder.setSubscribersJson(JSON.toJSONString(subscribers));
        favoritesFolderMapper.updateFavoritesFolder(favoritesFolder);
    }
}
