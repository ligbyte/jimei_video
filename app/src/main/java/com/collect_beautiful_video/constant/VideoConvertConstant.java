package com.collect_beautiful_video.constant;


public interface VideoConvertConstant {

    public static final String EXT_ERROR = "视频文件后缀名错误";

    public static final String VIDEO_TYPE_ERROR = "当前不支持此类视频格式，请上传mp4、avi、mov格式视频";

    public static final String VIDEO_EMPTY_ERROR = "请上传正确的视频";

    public static final String MP4 = "mp4";

    public static final String AVI = "AVI";

    public static final String MOV = "mov";

    public static final String VIDEO = "video";

    public static final String IMAGE = "image";

    /**
     * 清洗
     */
    public static final String CONVERT_CLEANING = "CLEANING";
    public static final String CONVERT_CLEANING_ABBREVIATION = "CLE";
    public static final String CONVERT_CLEANING_COMMANDLINE = "ffmpeg -i {} -qscale 0 -r {} {}";

    public static final String CONVERT_CONVERT = "CONVERT_CODE";
    public static final String CONVERT_CONVERT_ABBREVIATION = "CODE";
    public static final String CONVERT_CONVERT_COMMANDLINE = "ffmpeg -i {} -vcodec h264 {}";

    // 封面图片
    public static final String CONVERT_VIDEO_COVER_COMMANDLINE = "ffmpeg -i {} -ss 1 -f image2 {}";

    public static final String CONVERT_VIDEO_FPS = "FPS";
    public static final String CONVERT_VIDEO_FPS_COMMANDLINE = "ffmpeg -i {}";


    /**
     * 模糊/也是加背景图
     */
    public static final String CONVERT_DIM = "DIM";
    public static final String CONVERT_DIM_ABBREVIATION = "DIM";
//    public static final String CONVERT_DIM_COMMANDLINE = "ffmpeg -loop 1 -i {} -i  {} -filter_complex \"'overlay={}:{}:shortest=1,format=yuv420p'\" -c:a copy {}";
    public static final String CONVERT_DIM_COMMANDLINE = "ffmpeg -loop 1 -i {} -i  {} -filter_complex overlay={}:{}:shortest=1,format=yuv420p -c:a copy {}";

    /**
     * 裁剪
     */
    public static final String CONVERT_CUT_VIDEO = "CUT";
    public static final String CONVERT_CUT_VIDEO_ABBREVIATION = "CUT";
    public static final String CONVERT_CUT_VIDEO_COMMANDLINE = "ffmpeg -i {} -vf crop={}:{}:{}:{} -y {}";

    /**
     * 去除边框？
     */
    public static final String CONVERT_REMOVE_RIM = "REMOVE_RIM";

    /**
     * 获取视频音频
     */
    public static final String CONVERT_EXIST_AUDIT_COMMANDLINE = "ffmpeg -i {} -af volumedetect -f null /dev/null";

    /**
     * 视频加速
     */
    public static final String CONVERT_ACCELERATE = "ACCELERATE";
    public static final String CONVERT_ACCELERATE_ABBREVIATION = "AC";
    public static final String CONVERT_ACCELERATE_COMMANDLINE = "ffmpeg -i {} -filter_complex [0:v]setpts=0.95*PTS[v];[0:a]atempo=1.15[a] -map [v] -map [a] {} -y";
    public static final String CONVERT_ACCELERATE_NOT_EXIST_AUDIO_COMMANDLINE = "ffmpeg -i {} -filter_complex [0:v]setpts=0.95*PTS[v] -map [v]  {} -y";
//    public static final String CONVERT_ACCELERATE_COMMANDLINE = "ffmpeg -i {} -filter_complex \"[0:v]setpts=0.95*PTS[v];[0:a]atempo=1.15[a]\" -map \"[v]\" -map \"[a]\" {} -y";

    /**
     * 减速
     */
    public static final String CONVERT_DECELERATE = "DECELERATE";
    public static final String CONVERT_DECELERATE_ABBREVIATION = "DE";
    public static final String CONVERT_DECELERATE_COMMANDLINE =  "ffmpeg -i {} -filter_complex [0:v]setpts=1.05*PTS[v];[0:a]atempo=0.85[a] -map [v] -map [a] {} -y";
    public static final String CONVERT_DECELERATE_NOT_EXIST_AUDIO_COMMANDLINE =  "ffmpeg -i {} -filter_complex [0:v]setpts=1.05*PTS[v] -map [v] {} -y";
//    public static final String CONVERT_DECELERATE_COMMANDLINE =  "ffmpeg -i {} -filter_complex \"[0:v]setpts=1.05*PTS[v];[0:a]atempo=0.85[a]\" -map \"[v]\" -map \"[a]\" {} -y";

    /**
     * 压缩
     */
    public static final String CONVERT_COMPRESSION = "COMPRESSION";

    /**
     * 倒放
     */
    public static final String CONVERT_UPEND = "UPEND";
    public static final String CONVERT_UPEND_ABBREVIATION = "UD";
    public static final String CONVERT_UPEND_COMMANDLINE = "ffmpeg -i {} -vf reverse -af areverse -preset superfast {}";

    /**
     * 镜像
     */
    public static final String CONVERT_MIRROR_IMAGE = "MIRROR_IMAGE";
    public static final String CONVERT_MIRROR_IMAGE_ABBREVIATION = "MI";
    public static final String CONVERT_MIRROR_IMAGE_COMMANDLINE = "ffmpeg -i {} -vf hflip {}";
//    public static final String CONVERT_MIRROR_IMAGE_COMMANDLINE = "ffmpeg -i {} -vf \"hflip\" {}";

    /**
     * 去除声音
     */
    public static final String CONVERT_REMOVE_VOICE = "REMOVE_VOICE";
    public static final String CONVERT_REMOVE_VOICE_ABBREVIATION = "RV";
    public static final String CONVERT_REMOVE_VOICE_COMMANDLINE = "ffmpeg -i {} -vcodec copy -an {}";
}
