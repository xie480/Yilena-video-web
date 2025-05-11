import request from "../utils/request";

export const getDanmuEach5s = (DanmuDTO) => request.post(`/danmu/list`,DanmuDTO);

export const getVideo = (videoId) => request.get(`/video/search/${videoId}`);

export const getDanmuCount = (videoId) => request.get(`/danmu/count/${videoId}`);

export const like = (data) => request.put(`/like/do`,data);

export const dislike = (data) => request.put(`/like/undo`,data);

export const getIsLike = (userId,entityId,entityType) => request.get(`/like/search/${userId}/${entityId}/${entityType}`);

export const getIsCoins = (videoId,userId) => request.get(`/coins/search/${videoId}/${userId}`);

export const giveCoins = (data) => request.put(`/coins`,data);

export const getFavorite = (videoId,userId) => request.get(`/favorites/search/${videoId}/${userId}`);

export const giveToFavorites = (data) => request.put(`/favorites/video/do`,data);

export const giveToUnFavorites = (data) => request.put(`/favorites/video/undo`,data);

export const getUserFavoritesList = () => request.get(`/favorites/list`);

export const getOtherUserFavoritesList = (id) => request.get(`/favorites/search/${id}`);

export const getAuthorInfo = (userId) => request.get(`/user/search/${userId}`);

export const giveAuthorFollow = (data) => request.post(`/follow`,data);

export const giveAuthorUnfollow = (followedId,followerId,isSpecial) => request.delete(`/follow/${followedId}/${followerId}/${isSpecial}`);

export const getAuthorIsFollow = (userId,authorId) => request.get(`/follow/${userId}/${authorId}`);

export const getVideoComments = (entityId,entityType,type) => request.get(`/comment/search/hotOrNew?entityId=${entityId}&entityType=${entityType}&type=${type}`);

export const giveComment = (data) => request.post(`/comment`,data);

export const getUserFollow = (id,username,page,pageSize) => request.get(`/follow/search/page?id=${id}&username=${username}&page=${page}&pageSize=${pageSize}`);

export const getRecommendVideo = () => request.get(`/video/search/leftRecommend`);

export const getIsShare = (userId,entityId,entityType) => request.get(`/share/search/${userId}/${entityId}/${entityType}`);

export const giveShare = (data) => request.post(`/share`,data);
