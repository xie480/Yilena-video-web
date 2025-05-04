package com.yilena.service.utils;

public class CurrentHolder {

    private static final ThreadLocal<Long> CURRENT_LOCAL = new ThreadLocal<>();

    public static void setCurrent(Long userIdD) {
        CURRENT_LOCAL.set(userIdD);
    }

    public static Long getCurrent() {
        return CURRENT_LOCAL.get();
    }

    public static void removeCurrent() {
        CURRENT_LOCAL.remove();
    }
}