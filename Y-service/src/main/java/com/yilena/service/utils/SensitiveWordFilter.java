package com.yilena.service.utils;

import com.github.houbb.sensitive.word.core.SensitiveWordHelper;

public class SensitiveWordFilter {
    public static String filterSensitiveWords(String content) {
        if(SensitiveWordHelper.contains(content)) {
            return SensitiveWordHelper.replace(content, '喵');
        }
        return content;
    }
}
