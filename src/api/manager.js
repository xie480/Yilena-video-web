import request from "../utils/request";

export const getManagerList = (username,beginTime,endTime,status,page, pageSize) => request.get(`/manager/page?username=${username}&beginTime=${beginTime}&endTime=${endTime}&status=${status}&page=${page}&pageSize=${pageSize}`);

export const updateManagerStatus = (id,status) => request.put(`/manager/status/${status}/${id}`);