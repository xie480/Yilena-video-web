import request from "../utils/request";

export const register = (userInfo) => request.post(`/user/register`, userInfo);

export const login = (userInfo) => request.post(`/user/login`, userInfo);