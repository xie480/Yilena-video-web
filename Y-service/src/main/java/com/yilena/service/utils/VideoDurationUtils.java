package com.yilena.service.utils;

import com.aliyun.oss.ServiceException;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;

import java.net.MalformedURLException;
import java.net.URL;

public class VideoDurationUtils {

    /**
     * 获取视频时长
     */
    public static long getVideoDuration(String videoFilePath) {
        try {
            URL url = new URL(videoFilePath);
            MultimediaObject multimediaObject = new MultimediaObject(url);
            long duration = (multimediaObject.getInfo().getDuration()) / 1000;
            return duration;
        } catch (EncoderException | MalformedURLException e) {
            throw new ServiceException("" + e);
        }
    }
}
