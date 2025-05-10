import request from "../utils/request";

export const getVideoUploadList = (type) => request.get(`/manager/report/video/do/${type}`);

export const getPostList = (type) => request.get(`/manager/report/post/do/${type}`);

export const getUserList = (type) => request.get(`/manager/report/user/${type}`);

export const getVideoNoPassList = (type) => request.get(`/manager/report/video/undo/${type}`);