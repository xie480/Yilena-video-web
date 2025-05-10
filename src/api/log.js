import request from "../utils/request";

export const getLogList = (beginTime,endTime,page,pageSize,type) => request.get(`/manager/log/page?beginTime=${beginTime}&endTime=${endTime}&page=${page}&pageSize=${pageSize}&type=${type}`);