import request from "../utils/request";

export const getVideoSearchList = (id,title,description,tags,username,beginDate,endDate,beginTime,endTime,sortType,page,pageSize) => request.get(`/video/search/page?id=${id}&title=${title}&description=${description}&tags=${tags}&username=${username}&beginDate=${beginDate}&endDate=${endDate}&beginTime=${beginTime}&endTime=${endTime}&sortType=${sortType}&page=${page}&pageSize=${pageSize}`);

export const getUserSearchList = (id,username,sortType,page,pageSize) => request.get(`/user/search/page?id=${id}&username=${username}&sortType=${sortType}&page=${page}&pageSize=${pageSize}`)

export const addSearch = (content) => request.post(`/hotSearch?content=${content}`)