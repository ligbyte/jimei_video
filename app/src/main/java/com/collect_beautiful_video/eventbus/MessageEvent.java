package com.collect_beautiful_video.eventbus;

/**
 * Copyright (C), 2015-2019, suntront
 * FileName: TT
 * Author: Jeek
 * Date: 2019/11/20 9:21
 * Description: ${DESCRIPTION}
 */
public class MessageEvent {

    public final static String EVENT_BUS_VIDEO_PATH = "event_bus_video_path";
    private String message;
    private String path;


    public MessageEvent(String message) {
        this.path = path;
    }

    public MessageEvent(String message, String path) {
        this.message = message;
        this.path = path;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
