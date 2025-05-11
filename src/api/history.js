import request from "../utils/request";

export const addHistory = (userId,videoId) => request.get(`/history/video/${userId}/${videoId}`);

export const getHistory = (userId) => request.get(`/history/${userId}`);

export const isRecordUser = (data) => request.put(`/history/isRecord`,data);

export const deleteAllHistory = (userId) => request.delete(`/history/delAll/${userId}`);

export const deleteOneHistory = (id) => request.delete(`/history/${id}`);