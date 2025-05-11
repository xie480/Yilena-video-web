import request from "../utils/request";

export const getUserInfoVideoList = (userId,sortType) => request.get(`/video/search/userInfo/${userId}/${sortType}`);

export const getUserInfoFavoritesList = (id) => request.get(`/favorites/search/${id}`);

export const getUserCoinsVideosList = (userId) => request.get(`/coins/search/video/list/${userId}`);

export const updateUserInfo = (data) => request.put(`/user`,data);

export const getnewToken = (id) => request.get(`/user/getToken/${id}`);

export const getCollectionVideos = (userId,collectionId) => request.get(`/collection/search/videos/${userId}/${collectionId}`);

export const addCollection = (data) => request.post(`/collection`,data);

export const updateCollection = (data) => request.put(`/collection`,data);

export const deleteCollection = (id) => request.delete(`/collection?id=${id}`);

export const getSubscribeFavoritesFolder = (userId) => request.get(`/favorites/search/subscribe/${userId}`);

export const getFavoritesFolderVideos = (id,title,page,pageSize,userId) => request.get(`/favorites/search/video/${userId}?id=${id}&title=${title}&page=${page}&pageSize=${pageSize}`)

export const updateFavoritesFolder = (data) => request.put(`/favorites`,data);

export const addFavoritesFolder = (data) => request.post(`/favorites`,data);

export const getOtherFavoritesFolder = (id) => request.get(`/favorites/search/${id}`);

export const subscribeOhterFavoritesFolder = (id) => request.put(`/favorites/others/do?id=${id}`);

export const notSubscribeOhterFavoritesFolder = (id) => request.put(`/favorites/others/undo?id=${id}`);

export const isCurrentFavoritesFolderSubscribe = (userId,favoritesFolderId) => request.get(`/favorites/others/isSubscribe/${userId}/${favoritesFolderId}`);

export const updatePost = (data) => request.put(`/post`,data);

export const deletePost = (id) => request.delete(`/post?id=${id}`);