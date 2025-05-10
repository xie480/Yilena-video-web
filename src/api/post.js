import request from "../utils/request";

export const getPostList = (beginTime,endTime,page,pageSize) => request.get(`/manager/post/page?beginTime=${beginTime}&endTime=${endTime}&page=${page}&pageSize=${pageSize}`);

export const updatePostStatus = (data) => request.put(`/manager/post`,data);