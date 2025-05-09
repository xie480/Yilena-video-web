import request from "../utils/request";

export const getManagerInfo = (id) => request.get(`/manager/${id}`);

export const getVideoCount =  () => request.get(`/manager/report/video/status`);

export const getTotalUserCount =  () => request.get(`/manager/report/total/user`);

export const getTotalLikeCount =  () => request.get(`/manager/report/total/like`);

export const getTotalCommentCount =  () => request.get(`/manager/report/total/comment`);

export const getTotalPostCount =  () => request.get(`/manager/report/total/post`);

export const getTotalFavoriteCount =  () => request.get(`/manager/report/total/favorite`);

export const getManagerInfoByusername = (username) => request.get(`/manager/getByUsername/${username}`);

export const updatePassword = (data) => request.put(`/manager/password`, data);