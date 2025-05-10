import request from "../utils/request";

export const getUserList = (username,beginTime,endTime,status,page,pageSize) => request.get(`/manager/user/page?username=${username}&beginTime=${beginTime}&endTime=${endTime}&status=${status}&page=${page}&pageSize=${pageSize}`);

export const updateUserStatus = (data) => request.put(`/manager/user/status`,data);