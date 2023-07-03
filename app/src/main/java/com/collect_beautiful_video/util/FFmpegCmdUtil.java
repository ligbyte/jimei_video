package com.collect_beautiful_video.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.arch.core.executor.TaskExecutor;

import com.alibaba.fastjson.JSON;
import com.collect_beautiful_video.fragment.CollectRxFFmpegSubscriber;
import com.collect_beautiful_video.task.BaseSyncTask;
import com.collect_beautiful_video.task.TinySyncExecutor;
import com.collect_beautiful_video.task.TinyTaskExecutor;
import com.hjq.toast.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

import io.microshow.rxffmpeg.RxFFmpegCommandList;
import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

public class FFmpegCmdUtil {
  final static String TAG ="FFmpegCmdUtil";
  public static final String CMD_CHANGE_BACKGROUND = "CMD_CHANGE_BACKGROUND";
  public static final String CMD_DIM_BACKGROUND = "CMD_DIM_BACKGROUND";
  public static final String CMD_CLEAR = "CMD_CLEAR";
  public static final String CMD_COMPRESS = "CMD_COMPRESS";
  public static final String CMD_ACCELERATE = "CMD_ACCELERATE";
  public static final String CMD_DECELERATE = "CMD_DECELERATE";
  public static final String CMD_REMOVE_VOICE = "CMD_REMOVE_VOICE";
  public static final String CMD_UP_END = "CMD_UP_END";
  public static final String CMD_MIRROR_IMAGE = "CMD_MIRROR_IMAGE";
  public static final String CMD_CONVERT_VIDEO = "CMD_CONVERT_VIDEO";
  public static final String CMD_REMOVE_BORDER = "CMD_REMOVE_BORDER";


  private static LinkedHashMap<String, Object> cmdParamsHashMap = new LinkedHashMap<>();
  private static String mVideoSource;
  private static String mTargetVideoSource;
  private static String cmdExcString = "";
  private static WeakReference<Context> mContext;
  private static Handler handler = new Handler();

  /**
   * 裁剪视频
   *
   * @param sourcePath
   * @param outPath
   * @param width
   * @param height
   * @param x
   * @param y
   */
  public static void crop(String sourcePath, String outPath, int width, int height, int x, int y, RxFFmpegSubscriber rxFFmpegSubscriber) {
    RxFFmpegCommandList cmdlist = new RxFFmpegCommandList();
    cmdlist.clearCommands();
    cmdlist.append("ffmpeg");
    cmdlist.append("-i");
    cmdlist.append(sourcePath);
    cmdlist.append("-vf");
    cmdlist.append("crop=" + width + ":" + height + ":" + x + ":" + y);
    cmdlist.append("-preset");
    cmdlist.append("superfast");
    cmdlist.append("-y");
    cmdlist.append(outPath);

    RxFFmpegInvoke.getInstance()
      .runCommandRxJava(cmdlist.build(true))
      .subscribe(rxFFmpegSubscriber);
  }

  public static void generatePic(String sourcePath, String targetPath, RxFFmpegSubscriber rxFFmpegSubscriber) {
    RxFFmpegCommandList cmdlist = new RxFFmpegCommandList();
    cmdlist.clearCommands();
    cmdlist.append("ffmpeg");
    cmdlist.append("-i");
    cmdlist.append(sourcePath);
    cmdlist.append("-frames:v");
    cmdlist.append("1");
    cmdlist.append("-f");
    cmdlist.append("image2");
    cmdlist.append(targetPath);

    Log.d("TAG", "generatePic cmd: " + JSON.toJSONString(cmdlist));

    RxFFmpegInvoke.getInstance()
      .runCommandRxJava(cmdlist.build())
      .subscribe(rxFFmpegSubscriber);
  }


  public static void setVideoSource(String videoSource) {
    mVideoSource = videoSource;
  }


  /**
   * 更改背景
   *
   * @param pngSource
   * @param videoSource
   * @param width
   * @param height
   * @param outPath
   */
  private static void changeBackGround(String pngSource, String videoSource, String width, String height, String outPath) {
    String CONVERT_DIM_COMMANDLINE = "ffmpeg -loop 1 -i {} -i {} -filter_complex overlay={}:{}:shortest=1,format=yuv420p -c:a copy -preset superfast -threads 4 {}";
    cmdExcString = StringUtil.format(CONVERT_DIM_COMMANDLINE, pngSource, videoSource, convert(width), convert(height), outPath);

    Log.d("TAG", "changeBackGround cmd: " +cmdExcString);
  }

  public static String convert(String number) {
    try {
      int temp = Integer.parseInt(number);
      if (temp % 2 == 0) {
        return number;
      } else {
        return String.valueOf(temp + 1);
      }
    } catch (Exception e) {
      return number;
    }
  }

  public static void changeBackGround(String pngSource, int width, int height) {
    cmdParamsHashMap.put(CMD_CHANGE_BACKGROUND, pngSource + "&" + width + "&" + height);
  }

  public static void unChangeBackGround() {
    cmdParamsHashMap.remove(CMD_CHANGE_BACKGROUND);
  }


  private static void blurBackGround(String videoSource, String targetSource, String width, String height, String pngSource) {
    Bitmap bitmap = BitmapFactory.decodeFile(pngSource);
    if (bitmap != null) {
      String targetPng = pngSource.split(".png")[0] + "blur.png";
      if (mContext != null && mContext.get() != null) {
        ImageUtils.rsBlur(mContext.get(), bitmap, 20, targetPng);
      }
      changeBackGround(targetPng, videoSource, width, height, targetSource);
    }

  }

  public static void blurBackGround(Context context, String pngSource, int width, int height) {
    mContext = new WeakReference<>(context);
    cmdParamsHashMap.put(CMD_DIM_BACKGROUND, pngSource + "&" + width + "&" + height);
  }

  public static void unBlurBackGround() {
    cmdParamsHashMap.remove(CMD_DIM_BACKGROUND);
  }

  /**
   * 智能清洗
   *
   * @param videoSource
   * @param fps
   * @param outPath
   */
  private static void clear(String videoSource, Double fps, String outPath) {
    //ffmpeg -i {} -qscale 0 -r {} {}
    String CONVERT_CLEANING_COMMANDLINE = "ffmpeg -i {} -qscale 0 -preset superfast -r {} {}";
    cmdExcString = StringUtil.format(CONVERT_CLEANING_COMMANDLINE, videoSource, fps, outPath);

  }

  public static void clear(Double fps) {
    cmdParamsHashMap.put(CMD_CLEAR, fps);
  }

  public static void unClear() {
    cmdParamsHashMap.remove(CMD_CLEAR);
  }

  /**
   * 智能压塑
   *
   * @param videoSource
   * @param outPath
   */
  private static void compress(String videoSource, String outPath) {
    //ffmpeg  -i  Desktop/input.mp4  -s 1920x1080  -b:v 1M  -r 20  Desktop/output.mp4
    String COMPRESS = "ffmpeg -y -i {} -b 1M -r 20 -vcodec libx264 -preset superfast {}";
    cmdExcString = StringUtil.format(COMPRESS, videoSource, outPath);

  }

  public static void compress() {
    cmdParamsHashMap.put(CMD_COMPRESS, null);

  }

  public static void unCompress() {
    cmdParamsHashMap.remove(CMD_COMPRESS);
  }

  /**
   * 智能加速
   *
   * @param videoSource
   * @param outPath
   */
  private static void accelerate(String videoSource, String outPath) {
    String CONVERT_ACCELERATE_COMMANDLINE = "ffmpeg -i {} -filter_complex [0:v]setpts=0.95*PTS[v];[0:a]atempo=1.15[a] -map [v] -map [a] -preset superfast {} -y";
    String CONVERT_ACCELERATE_NOT_EXIST_AUDIO_COMMANDLINE = "ffmpeg -i {} -filter_complex [0:v]setpts=0.95*PTS[v] -map [v] -preset superfast {} -y";
    boolean isExitRemoveAudio = cmdParamsHashMap.containsKey(CMD_REMOVE_VOICE) || !FileUtil.isVideoHaveAudioTrack(videoSource);
    String cmd = isExitRemoveAudio ? CONVERT_ACCELERATE_NOT_EXIST_AUDIO_COMMANDLINE : CONVERT_ACCELERATE_COMMANDLINE;
    cmdExcString = StringUtil.format(cmd, videoSource, outPath);
  }

  public static void accelerate() {
    cmdParamsHashMap.put(CMD_ACCELERATE, null);
  }

  public static void unAccelerate() {
    cmdParamsHashMap.remove(CMD_ACCELERATE);
  }


  private static void decelerate(String videoSource, String outPath) {
    String CONVERT_DECELERATE_COMMANDLINE = "ffmpeg -i {} -filter_complex [0:v]setpts=0.85*PTS[v];[0:a]atempo=0.85[a] -map [v] -map [a] -max_muxing_queue_size 400 -preset superfast {} -y";
    String CONVERT_DECELERATE_NOT_EXIST_AUDIO_COMMANDLINE = "ffmpeg -i {} -filter_complex [0:v]setpts=0.85*PTS[v] -map [v] -max_muxing_queue_size 400 -preset superfast {} -y";

    boolean isExitAudio = cmdParamsHashMap.containsKey(CMD_REMOVE_VOICE) || !FileUtil.isVideoHaveAudioTrack(videoSource);
    cmdExcString = StringUtil.format(isExitAudio ? CONVERT_DECELERATE_NOT_EXIST_AUDIO_COMMANDLINE : CONVERT_DECELERATE_COMMANDLINE
      , videoSource, outPath);
  }

  public static void decelerate() {
    cmdParamsHashMap.put(CMD_DECELERATE, null);
  }

  public static void unDecelerate() {
    cmdParamsHashMap.remove(CMD_DECELERATE);
  }

  private static void removeVoice(String videoSource, String outPath) {
    String CONVERT_REMOVE_VOICE_COMMANDLINE = "ffmpeg -i {} -vcodec copy -an {}";
    cmdExcString = StringUtil.format(CONVERT_REMOVE_VOICE_COMMANDLINE, videoSource, outPath);
  }

  public static void removeVoice() {
    cmdParamsHashMap.put(CMD_REMOVE_VOICE, null);
  }

  public static void unRemoveVoice() {
    cmdParamsHashMap.remove(CMD_REMOVE_VOICE);
  }

  private static void upEnd(String videoSource, String outPath) {
    String CONVERT_UPEND_COMMANDLINE = "ffmpeg -i {} -vf reverse -af areverse -max_muxing_queue_size 400 -preset superfast {}";
    cmdExcString = StringUtil.format(CONVERT_UPEND_COMMANDLINE, videoSource, outPath);

  }

  public static void upEnd() {
    cmdParamsHashMap.put(CMD_UP_END, null);
  }

  public static void unUpEnd() {
    cmdParamsHashMap.remove(CMD_UP_END);
  }

  private static void mirrorImage(String videoSource, String outPath) {
    String CONVERT_MIRROR_IMAGE_COMMANDLINE = "ffmpeg -i {} -vf hflip -preset superfast {}";
    cmdExcString = StringUtil.format(CONVERT_MIRROR_IMAGE_COMMANDLINE, videoSource, outPath);
  }

  public static void mirrorImage() {
    cmdParamsHashMap.put(CMD_MIRROR_IMAGE, null);
  }

  public static void unMirrorImage() {
    cmdParamsHashMap.remove(CMD_MIRROR_IMAGE);
  }


  private static void convert(String videoSource, String targetSource) {
    //ffmpeg -i out.ogv -vcodec h264 out.mp4
    String CONVERT_MIRROR_IMAGE_COMMANDLINE = "ffmpeg -i {} -vcodec mpeg4 {}";
    cmdExcString = StringUtil.format(CONVERT_MIRROR_IMAGE_COMMANDLINE, videoSource, targetSource);
  }

  private static void removeBorder() {

  }

  public static void convert() {
    cmdParamsHashMap.put(CMD_CONVERT_VIDEO, null);
  }

  public static void unConvert() {
    cmdParamsHashMap.remove(CMD_CONVERT_VIDEO);
  }


  public static void start(ProcessListener processListener) {
    processListener.onStart();
    pos = 0;
    for (Map.Entry<String, Object> entry : cmdParamsHashMap.entrySet()) {
      TinySyncExecutor.getInstance().enqueue(new BaseSyncTask() {
        @Override
        public void doTask() {
          Log.d("wjy task", "do key" + entry.getKey());
          startConvert(entry.getKey(), entry.getValue());
          startProcess(entry.getKey(), processListener);
        }
      });


    }

  }

  private static void startConvert(String key, Object object) {
    switch (key) {
      case CMD_CLEAR:
        clear(mVideoSource, (Double) object, changePath(mVideoSource, CMD_CLEAR));
        break;
      case CMD_CHANGE_BACKGROUND:
        String params = (String) object;
        String[] value = params.split("&");
        Log.d("wjy", "CMD_CHANGE_BACKGROUND" + object);
        changeBackGround(value[0], mVideoSource, value[1], value[2], changePath(mVideoSource, CMD_CHANGE_BACKGROUND));
        break;
      case CMD_ACCELERATE:
        accelerate(mVideoSource, changePath(mVideoSource, CMD_ACCELERATE));
        break;
      case CMD_DECELERATE:
        decelerate(mVideoSource, changePath(mVideoSource, CMD_DECELERATE));
        break;
      case CMD_COMPRESS:
        compress(mVideoSource, changePath(mVideoSource, CMD_COMPRESS));
        break;
      case CMD_UP_END:
        upEnd(mVideoSource, changePath(mVideoSource, CMD_UP_END));
        break;
      case CMD_MIRROR_IMAGE:
        mirrorImage(mVideoSource, changePath(mVideoSource, CMD_MIRROR_IMAGE));
        break;
      case CMD_REMOVE_VOICE:
        removeVoice(mVideoSource, changePath(mVideoSource, CMD_REMOVE_VOICE));
        break;
      case CMD_DIM_BACKGROUND:
        String params1 = (String) object;
        String[] value1 = params1.split("&");
        blurBackGround(mVideoSource, changePath(mVideoSource, CMD_DIM_BACKGROUND), value1[1], value1[2], value1[0]);
        break;
      case CMD_CONVERT_VIDEO:
        convert(mVideoSource, changePath(mVideoSource, CMD_CONVERT_VIDEO));
        break;
      case CMD_REMOVE_BORDER:
        removeBorder();
        break;

    }
  }

  private static int pos = 0;

  private static void startProcess(String key, ProcessListener processListener) {
    try {
      Log.d(TAG, "startProcess cmdExcString: " + cmdExcString);
      RxFFmpegInvoke.getInstance()
        .runCommandRxJava(cmdExcString.split(" "))
        .subscribe(new CollectRxFFmpegSubscriber() {
          @Override
          public void onFinish() {
            super.onFinish();
            pos++;
            if (TinySyncExecutor.getInstance().isOver()) {
              processListener.onProcess(key, cmdParamsHashMap.size(), pos, 100, 100);
              processListener.onAllFinish(mTargetVideoSource);
            } else {
              processListener.onSingleFinish(key);
            }
            mVideoSource = mTargetVideoSource;
            Log.d("wjy task", "startProcess success" + key);
            handler.postDelayed(new Runnable() {
              @Override
              public void run() {
                TinySyncExecutor.getInstance().finish();
              }
            }, 300);


          }

          @Override
          public void onError(String message) {
            super.onError(message);
            Log.d("wjy task", "startProcess error" + key);
            processListener.onError(key);
            TinySyncExecutor.getInstance().finish();
          }

          @Override
          public void onProgress(int progress, long progressTime) {
            super.onProgress(progress, progressTime);
            if (progress > 100) {
              progress = progress / 1000;
            }
            processListener.onProcess(key, cmdParamsHashMap.size(), pos, progress, progressTime);

          }
        });

    } catch (Exception e) {
      Log.d("wjy task", "startProcess exception" + e);
      ToastUtils.show("exception" + e);
    }


  }

  public static boolean isContainBack() {
    return cmdParamsHashMap.containsKey(CMD_UP_END);
  }


  private static String changePath(String source, String key) {
    Log.d("wjy do source", source);
    if (!TextUtils.isEmpty(source)) {
      String[] temp = source.split(".mp4");
      mTargetVideoSource = temp[0] + "_" + key + ".mp4";
    }
    Log.d("wjy change source", mTargetVideoSource);
    return mTargetVideoSource;
  }

  public static void clearAll() {
    cmdExcString = "";
    cmdParamsHashMap.clear();
    pos = 0;
  }

  public static void release() {
    RxFFmpegInvoke.getInstance().exit();
    TinySyncExecutor.getInstance().exit();
  }

  public static boolean isEmpty() {
    return cmdParamsHashMap.isEmpty();
  }

  public static boolean isContainBlur() {
    return cmdParamsHashMap.containsKey(CMD_DIM_BACKGROUND);
  }


  public interface ProcessListener {
    void onStart();

    void onProcess(String cmd, int total, int position, int process, long processTime);

    void onSingleFinish(String cmd);

    void onAllFinish(String videoSource);

    void onError(String cmd);
  }


}
