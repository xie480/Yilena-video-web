import request from "../utils/request";

export const findAllVideos = () => request.get(`/video/search/list`)

export const getUserInfoByUsername = (username) => request.get(`/user/search/username/${username}`)

export const getHotSearch = () => request.get(`/hotSearch/search/top`)

export const getUserInfoById = (id) => request.get(`/user/search/${id}`)
