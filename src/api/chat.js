import request from "../utils/request";

export const getUserFollowingUsers = (userId) => request.get(`/message/userList/${userId}`);

export const getChatHistory = (currentUserId,targetUserId) => request.get(`/message/history?currentUserId=${currentUserId}&targetUserId=${targetUserId}`);

export const sendMessage = (data) => request.post(`/message/send`,data);

export const setMessageRead = (data) => request.post(`/message/mark-read`,data);

export const deleteMessage = (messageId,type) => request.delete(`/message?messageId=${messageId}&type=${type}`);

export const getAnswerMeMsg = (userId) => request.get(`/message/answerMe/${userId}`);

export const getAtMeMsg = (userId) => request.get(`/message/atMe/${userId}`);

export const getLikeMeMsg = (userId) => request.get(`/message/likeMe/${userId}`);

export const getSystemMsg = (userId) => request.get(`/message/systemMessage/${userId}`);

export const getOtherMsg = (userId) => request.get(`/message/otherMessage/${userId}`);