import request from "../utils/request";

export const getVideoByPage = (title,beginTime,endTime,status,page,pageSize) => request.get(`/manager/pending/page?title=${title}&beginTime=${beginTime}&endTime=${endTime}&status=${status}&page=${page}&pageSize=${pageSize}`);

export const updateVideoStatus = (data) => request.put(`/manager/pending/status`,data);

export const NoPassVideo = (data) => request.put(`/manager/pending/video`,data)