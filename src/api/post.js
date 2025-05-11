import request from "../utils/request";

export const getUserFollowingPosts = (userId) => request.get(`/post/following/${userId}`);

export const getMyPosts = () => request.get(`/post/myPosts`);

export const addPost = (data) => request.post(`/post`,data);

export const getAnyonePosts = (id) => request.get(`/post/search/byUserId/${id}`);