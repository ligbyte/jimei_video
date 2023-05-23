package com.collect_beautiful_video.fragment;

import static android.app.Activity.RESULT_OK;
import static android.media.MediaPlayer.SEEK_CLOSEST;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.collect_beautiful_video.R;
import com.collect_beautiful_video.activity.LoginActivity;
import com.collect_beautiful_video.activity.ManagerVideoActivity;
import com.collect_beautiful_video.eventbus.MessageEvent;
import com.collect_beautiful_video.media.MyGLSurfaceView;
import com.collect_beautiful_video.util.FFmpegCmdUtil;
import com.collect_beautiful_video.util.FileSizeUtil;
import com.collect_beautiful_video.util.FileUtil;
import com.collect_beautiful_video.util.ScreenUtils;
import com.collect_beautiful_video.util.UserInfoUtils;
import com.collect_beautiful_video.view.VideoCropView;
import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.util.GalleryFinal;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.yhd.mediaplayer.MediaPlayerHelper;
import com.lcw.library.imagepicker.ImagePicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

public class MainFragment extends Fragment {
  private static final String[] REQUEST_PERMISSIONS = {
    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
  };
  private final static int REQUEST_SELECT_IMAGES_CODE = 77;
  private static final int PERMISSION_REQUEST_CODE = 1;
  private MyGLSurfaceView mGLSurfaceView = null;
  private LinearLayout mAddVideoLayout;
  private String mVideoPath = "";
  private VideoCropView mVideoCropView;
  private String mOutVideoPath = "";
  private TextView mTvNextStep;
  private float mVideoWidth;
  private float mVideoHeight;
  private float mCropWidth;
  private float mCropHeight;
  private ImageView mIvPlay;
  private ImageView mIvVoice;
  private int mPlayStatus = 0;
  private int mVoiceStatus = 1;
  private LoadingPopupView loadingPopupView;
  private TextView mErrorView;


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mOutVideoPath = FileUtil.getVideoPath(getContext());
    EventBus.getDefault().register(this);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    initView(rootView);
    return rootView;
  }

  public void createDialog() {
    loadingPopupView = (LoadingPopupView) new XPopup.Builder(getContext())
      .setPopupCallback(new SimpleCallback() {
        @Override
        public void onDismiss(BasePopupView popupView) {
          super.onDismiss(popupView);
          FFmpegCmdUtil.release();
        }
      })
      .hasNavigationBar(false)
      .dismissOnTouchOutside(false)
      .asLoading("正在裁剪视频...")
      .show();

  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  private void initView(View rootView) {
    mGLSurfaceView = rootView.findViewById(R.id.surface_view);
    mAddVideoLayout = rootView.findViewById(R.id.ll_add_video);
    mVideoCropView = rootView.findViewById(R.id.video_crop_view);
    mTvNextStep = rootView.findViewById(R.id.tv_next_step);
    mErrorView = rootView.findViewById(R.id.error_view);
    mAddVideoLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addVideo();
      }
    });

    rootView.findViewById(R.id.tv_select_video).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addVideo();
      }
    });

    mTvNextStep.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (TextUtils.isEmpty(mVideoPath)) {
          return;
        }
        if (!UserInfoUtils.isLogin(getActivity())) {
          startActivity(new Intent(getActivity(), LoginActivity.class));
          return;
        }
        cropVideo();
        pausePlay();
        createDialog();

      }
    });
    mIvPlay = rootView.findViewById(R.id.iv_play);
    mIvPlay.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mPlayStatus == 0) {
          startPlay();
        } else {
          pausePlay();
        }
      }
    });

    mIvVoice = rootView.findViewById(R.id.voice_open_icon);
    mIvVoice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mVoiceStatus == 0) {
          startVoice();
        } else {
          pauseVoice();
        }
      }
    });

    rootView.findViewById(R.id.iv_left).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
          int duration = MediaPlayerHelper.getInstance().getMediaPlayer().getCurrentPosition() - 3000;
          rootView.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
              MediaPlayerHelper.getInstance().getMediaPlayer().seekTo(duration < 0 ? 0 : duration, SEEK_CLOSEST);
            }
          }, 100);
        }
      }
    });

    rootView.findViewById(R.id.right_icon).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
          int duration = MediaPlayerHelper.getInstance().getMediaPlayer().getCurrentPosition() + 3000;
          rootView.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
              MediaPlayerHelper.getInstance().getMediaPlayer().seekTo(duration, SEEK_CLOSEST);
            }
          }, 100);
        }

      }
    });

    rootView.findViewById(R.id.iv_reset).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
          if (MediaPlayerHelper.getInstance().getMediaPlayer().isPlaying()) {
            MediaPlayerHelper.getInstance().getMediaPlayer().seekTo(0);
          } else {
            MediaPlayerHelper.getInstance().getMediaPlayer().start();
          }
        }

      }
    });

  }

  private int x;
  private int y;

  private void cropVideo() {
    float[] position = mVideoCropView.getCutArr();
    Log.d("wjy", position[0] + "," + position[1] + "," + position[2] + "," + position[3]);
    double avgWidth = mVideoWidth / mCropWidth;
    double avgHeight = mVideoHeight / mCropHeight;

    float sourceCropWidth = (mVideoCropView.getCutArr()[2] - mVideoCropView.getCutArr()[0]);
    float sourceCropHeight = (mVideoCropView.getCutArr()[3] - mVideoCropView.getCutArr()[1]);
    float sourceLeftX = mVideoCropView.getCutArr()[0];
    float sourceLeftY = mVideoCropView.getCutArr()[1];


    int width = (int) (sourceCropWidth * avgWidth);
    int height = (int) (sourceCropHeight * avgHeight);
    x = (int) ((mVideoWidth - width) / 2);
    y = (int) ((mVideoHeight - height) / 2);

    int topx = (int) (mVideoCropView.getCutArr()[0] * avgWidth);
    int topy = (int) (mVideoCropView.getCutArr()[1] * avgWidth);

    long time = SystemClock.elapsedRealtime();

    Log.d("wjy", "width。。。。" + width);
    Log.d("wjy", "height。。。。" + height);
    Log.d("wjy", "x。。。。" + x);
    Log.d("wjy", "y。。。。" + y);

    String outPath = mOutVideoPath + "/" + "temp_crop_" + time + ".mp4";
    FFmpegCmdUtil.crop(mVideoPath, outPath, width - 1, height - 1, topx, topy, new CollectRxFFmpegSubscriber() {
      @Override
      public void onFinish() {
        long time = SystemClock.elapsedRealtime();
        String picPath = mOutVideoPath + "/" + "temp_crop_" + time + ".img";
        FFmpegCmdUtil.generatePic(outPath, picPath, new CollectRxFFmpegSubscriber() {
          @Override
          public void onFinish() {
            Log.i("wjy", "Async command execution completed successfully.");
            Intent intent = new Intent(getActivity(), ManagerVideoActivity.class);
            intent.putExtra("VideoPath", outPath);
            intent.putExtra("PicPath", picPath);
            intent.putExtra("VideoWidth", sourceCropWidth);
            intent.putExtra("VideoHeight", sourceCropHeight);
            intent.putExtra("ViewWidth", mCropWidth);
            intent.putExtra("ViewHeight", mCropHeight);
            intent.putExtra("SourceWidth", mVideoWidth);
            intent.putExtra("SourceHeight", mVideoHeight);
            intent.putExtra("x", topx);
            intent.putExtra("y", topy);
            intent.putExtra("lefttopx", sourceLeftX);
            intent.putExtra("lefttopy", sourceLeftY);

            startActivity(intent);
            if (loadingPopupView != null) {
              loadingPopupView.dismiss();
            }
            if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
              MediaPlayerHelper.getInstance().getMediaPlayer().stop();
            }
          }

          @Override
          public void onProgress(int progress, long progressTime) {

          }

          @Override
          public void onCancel() {

          }

          @Override
          public void onError(String message) {
            Log.d("wjy generate", message);
          }

        });
      }

      @Override
      public void onProgress(int progress, long progressTime) {

      }

      @Override
      public void onCancel() {

      }

      @Override
      public void onError(String message) {
        Log.d("wjy crop", message);
      }


    });
  }

  private void addVideo() {
    if (!hasPermissionsGranted(REQUEST_PERMISSIONS)) {
      requestPermissions(REQUEST_PERMISSIONS, PERMISSION_REQUEST_CODE);
    } else {
      choiceVideo();
    }
  }

  protected boolean hasPermissionsGranted(String[] permissions) {
    for (String permission : permissions) {
      if (checkSelfPermission(getActivity(), permission)
        != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  private void choiceVideo() {
    if (UserInfoUtils.isLogin(getContext())) {

      GalleryFinal.selectMedias(getActivity(), GalleryFinal.TYPE_VIDEO,1, new GalleryFinal.OnSelectMediaListener() {
        @Override
        public void onSelected(ArrayList<Photo> photoArrayList) {
          if (photoArrayList.size() > 0) {
            mVideoPath = photoArrayList.get(0).getPath();
            if (FileSizeUtil.getFileOrFilesSize(mVideoPath, FileSizeUtil.SIZETYPE_MB) > 200) {
              ToastUtils.show("视频大小不能超过200m");
              mVideoPath = "";
              return;
            }

            mTvNextStep.setBackgroundResource(R.drawable.select_text);
            startPlay();
            if (mAddVideoLayout != null) {
              mAddVideoLayout.setVisibility(View.GONE);
            }
          }
        }
      });
    } else {
      startActivity(new Intent(getContext(), LoginActivity.class));
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSION_REQUEST_CODE) {
      if (!hasPermissionsGranted(REQUEST_PERMISSIONS)) {
        Toast.makeText(getActivity(), "需求获取目录权限才可以使用哦", Toast.LENGTH_SHORT).show();
      } else {
        choiceVideo();
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);



  }



  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MessageEvent messageEvent) {
        if (messageEvent.getMessage().equals(MessageEvent.EVENT_BUS_VIDEO_PATH)){
          mVideoPath = messageEvent.getPath();
          mTvNextStep.setBackgroundResource(R.drawable.select_text);
          startPlay();
          if (mAddVideoLayout != null) {
            mAddVideoLayout.setVisibility(View.GONE);
          }
        }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (MediaPlayerHelper.getInstance().getMediaPlayer() != null && MediaPlayerHelper.getInstance().getMediaPlayer().isPlaying()) {
      MediaPlayerHelper.getInstance().getMediaPlayer().stop();
    }
    MediaPlayerHelper.getInstance().reCreateMediaPlayer();
    if (!TextUtils.isEmpty(mVideoPath)) {
      MediaPlayerHelper.getInstance().playLocal(getActivity(), mVideoPath);
      startPlay();
    }
    if (mErrorView != null && mErrorView.getVisibility() == View.VISIBLE) {
      if (mAddVideoLayout != null) {
        mAddVideoLayout.setVisibility(View.VISIBLE);
      }
      if (mErrorView != null) {
        mErrorView.setVisibility(View.GONE);
      }
      if (mVideoCropView != null) {
        mVideoCropView.setVisibility(View.VISIBLE);
      }
    }
    if (mVoiceStatus == 1) {
      startVoice();
    } else {
      pauseVoice();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    try {
      if (MediaPlayerHelper.getInstance().getMediaPlayer() != null && MediaPlayerHelper.getInstance().getMediaPlayer().isPlaying()) {
        pausePlay();
      }

      if (loadingPopupView != null) {
        loadingPopupView.dismiss();
      }
    } catch (Exception e) {
      Log.e("wjy", e.getLocalizedMessage());
    }


  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    MediaPlayerHelper.getInstance().release();
  }

  public void startPlay() {
    mPlayStatus = 1;
    if (mIvPlay != null) {
      mIvPlay.setImageResource(R.mipmap.suspend_icon);
    }
    if (mErrorView != null) {
      mErrorView.setVisibility(View.GONE);

    }
    if (mVideoCropView != null) {
      mVideoCropView.setVisibility(View.VISIBLE);
    }
    Log.d("wjy", mVideoPath);
    MediaPlayerHelper.getInstance().setSurfaceView(mGLSurfaceView).playLocal(getActivity(), mVideoPath);
    MediaPlayerHelper.getInstance().setOnStatusCallbackListener(new MediaPlayerHelper.OnStatusCallbackListener() {
      @Override
      public void onStatusonStatusCallbackNext(MediaPlayerHelper.CallBackState status, Object... args) {
        Log.d("wjy", status.toString());
        if (status == MediaPlayerHelper.CallBackState.PROGRESS) {
        }
        if (status == MediaPlayerHelper.CallBackState.PREPARE) {
          onDecoderReady();
        }


        if (status == MediaPlayerHelper.CallBackState.COMPLETE) {
          mPlayStatus = 0;
          if (mIvPlay != null) {
            mIvPlay.setImageResource(R.mipmap.play_icon);
          }
        }

        if (status == MediaPlayerHelper.CallBackState.INFO) {
          mPlayStatus = 1;
          if (mIvPlay != null) {
            mIvPlay.setImageResource(R.mipmap.suspend_icon);
          }
        }
        if (status == MediaPlayerHelper.CallBackState.ERROR || status == MediaPlayerHelper.CallBackState.EXCEPTION) {
//                    StringBuilder error = new StringBuilder();
//                    for (int i = 0; i < args.length; i++) {
//                        error.append(args[i]);
//                    }
//                    if (mErrorView != null) {
//                        mErrorView.setVisibility(View.VISIBLE);
//                        mErrorView.setText("播放错误:" + error);
//                    }
//                    if (mVideoCropView != null) {
//                        mVideoCropView.setVisibility(View.GONE);
//                    }
        }
      }
    });


  }


  public void pausePlay() {
    mPlayStatus = 0;
    if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
      MediaPlayerHelper.getInstance().getMediaPlayer().pause();
    }
    if (mIvPlay != null) {
      mIvPlay.setImageResource(R.mipmap.play_icon);
    }
  }

  public void startVoice() {
    mVoiceStatus = 1;
    if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
      MediaPlayerHelper.getInstance().getMediaPlayer().setVolume(1.0f, 1.0f);
    }
    if (mIvVoice != null) {
      mIvVoice.setImageResource(R.mipmap.voice_open_icon);
    }
  }

  public void pauseVoice() {
    mVoiceStatus = 0;
    if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
      MediaPlayerHelper.getInstance().getMediaPlayer().setVolume(0.0f, 0.0f);
    }

    if (mIvVoice != null) {
      mIvVoice.setImageResource(R.mipmap.no_voice);
    }
  }


  private void onDecoderReady() {

    if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
      mVideoWidth = MediaPlayerHelper.getInstance().getMediaPlayer().getVideoWidth();
      mVideoHeight = MediaPlayerHelper.getInstance().getMediaPlayer().getVideoHeight();
    }


    if (mVideoHeight * mVideoWidth != 0) {
      mGLSurfaceView.setAspectRatio((int) mVideoWidth, (int) mVideoHeight);
    }


    DisplayMetrics dm = new DisplayMetrics();
    getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
    float screenWidth = dm.widthPixels;
    float height = ScreenUtils.dip2px(getActivity(), 280);

    float ratio = 0;

    //横屏
    if (mVideoWidth > mVideoHeight) {
      ratio = mVideoWidth / mVideoHeight;

      //横屏
      mCropWidth = screenWidth;
      mCropHeight = (screenWidth / ratio);
      mCropHeight = Math.min(mCropHeight, height);


    } else {
      ratio = mVideoHeight / mVideoWidth;
      //竖屏
      mCropHeight = height;
      mCropWidth = (height / ratio);
      mCropWidth = Math.min(screenWidth, mCropWidth);
    }


    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVideoCropView.getLayoutParams();
    layoutParams.width = (int) mCropWidth;
    layoutParams.height = (int) mCropHeight;

    Log.d("wjy radio", ratio + "");
    Log.d("wjy screenWidth", screenWidth + "");

    Log.d("wjy videowidth", mVideoWidth + "");
    Log.d("wjy videoheight", mVideoHeight + "");

    Log.d("wjy width", mCropWidth + "");
    Log.d("wjy height", mCropHeight + "");

  }

  @Override
  public void onStop() {
    super.onStop();
    FFmpegCmdUtil.release();
  }


}
