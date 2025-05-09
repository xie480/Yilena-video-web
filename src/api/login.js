import request from "../utils/request";

export const login = (data) => request.post("/manager/login", data);