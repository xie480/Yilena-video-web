import request from "../utils/request";

export const getAllViews = (userId) => request.get(`/video/allViews/${userId}`);

export const getAllCoins = (userId) => request.get(`/video/allCoins/${userId}`);

export const getAllLikes = (userId) => request.get(`/video/allLikes/${userId}`);

export const getAllFavorites = (userId) => request.get(`/video/allFavorites/${userId}`);

export const getAllComments = (userId) => request.get(`/video/allComments/${userId}`);

export const getAllShares = (userId) => request.get(`/video/allShares/${userId}`);

export const getBarrages = (videoId) => request.get(`/video/allBarrages/${videoId}`);

export const getVideoPendings = (userId,status) => request.get(`/videoPending/list/${userId}/${status}`);

export const updateVideoPending = (data) => request.put(`/videoPending`,data);

export const deleteVideoPending = (id) => request.delete(`/videoPending/${id}`);

export const reuploadVideoPeing = (id) => request.put(`/videoPending/reupload/${id}`);

export const getVideoPendingById = (id) => request.get(`/videoPending/get/${id}`);

export const getVideoYesByUserId = (userId) => request.get(`/video/search/user/${userId}`);

export const deleteVideoById = (id) => request.delete(`/video/${id}`);

export const getUserCollection = (userId,currentUserId) => request.get(`/collection/search/list/${userId}/${currentUserId}`);

export const updateVideoPendingTo = (data) => request.put(`/videoPending`,data);

export const updateVideoYesTo = (data) => request.put(`/video`,data);

export const addVideo = (data) => request.post(`/video`,data);

export const getReportFollowers = (userId,type) => request.get(`/userReport/followers/${userId}/${type}`);

export const getReportLikes = (userId,type) => request.get(`/userReport/likes/${userId}/${type}`);

export const getReportFavorites = (userId,type) => request.get(`/userReport/favorites/${userId}/${type}`);

export const getReportComments = (userId,type) => request.get(`/userReport/comments/${userId}/${type}`);

export const getReportCoins = (userId,type) => request.get(`/userReport/coins/${userId}/${type}`);

export const getCommentQurey = (videoTitle,sortType) => request.get(`/comment/query?videoTitle=${videoTitle}&sortType=${sortType}`);

export const deleteComment = (ids) => request.delete(`/comment?ids=${ids}`);