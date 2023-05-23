package com.collect_beautiful_video.activity;


import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_ACCELERATE;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_CHANGE_BACKGROUND;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_CLEAR;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_COMPRESS;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_DECELERATE;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_DIM_BACKGROUND;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_MIRROR_IMAGE;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_REMOVE_VOICE;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.CMD_UP_END;
import static com.collect_beautiful_video.util.FFmpegCmdUtil.clearAll;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.collect_beautiful_video.R;
import com.collect_beautiful_video.bean.AvailableCount;
import com.collect_beautiful_video.bean.BaseObjectBean;
import com.collect_beautiful_video.bean.PicBean;
import com.collect_beautiful_video.http.RetrofitClient;
import com.collect_beautiful_video.util.FFmpegCmdUtil;
import com.collect_beautiful_video.util.FileSizeUtil;
import com.collect_beautiful_video.util.ImageUtils;
import com.collect_beautiful_video.util.ScreenUtils;
import com.collect_beautiful_video.util.UserInfoUtils;
import com.collect_beautiful_video.view.BottomOutDialog;
import com.collect_beautiful_video.view.ProgressPopup;
import com.collect_beautiful_video.view.TextStatusView;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.yhd.mediaplayer.MediaPlayerHelper;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ManagerVideoActivity extends FragmentActivity {
  private ImageView mImageView = null;
  private float mVideoWidth;
  private float mVideoHeight;
  private float mViewWidth;
  private float mViewHeight;
  private ImageView mViewBack;
  private String mVideoPath;
  private String mPicPath;
  private ProgressPopup progressPopup;
  private String mChangePicPath;
  private AvailableCount mAvailableCount;
  private int mPicWidth;
  private int mPicHeight;
  private float mSourceWidth;
  private float mSourceHeight;
  private float x;
  private float y;
  private boolean isRemoveBorder = false;
  private TextStatusView mLowTextStatusView;
  private TextStatusView mAccTextStatusView;
  private TextStatusView mDimTextStatusView;
  private TextStatusView mBackTextStatusView;
  private TextStatusView mRemoveVoiceTextStatusView;
  private TextStatusView mAiClearTextStatusView;
  private TextStatusView mRemoveBorderTextStatusView;
  private TextStatusView mZipTextStatusView;
  private TextStatusView mAiReverseTextStatusView;
  private boolean isClickVoiceBySelf = false;
  private RelativeLayout mBackLayout;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_manager_video);
    mImageView = findViewById(R.id.image_view);
    mViewBack = findViewById(R.id.fl_back);
    mBackLayout = findViewById(R.id.rl_back);
    findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });
    mVideoPath = getIntent().getStringExtra("VideoPath");
    mVideoWidth = getIntent().getFloatExtra("VideoWidth", 0);
    mVideoHeight = getIntent().getFloatExtra("VideoHeight", 0);
    mViewWidth = getIntent().getFloatExtra("ViewWidth", 0);
    mViewHeight = getIntent().getFloatExtra("ViewHeight", 0);
    mSourceWidth = getIntent().getFloatExtra("SourceWidth", 0);
    mSourceHeight = getIntent().getFloatExtra("SourceHeight", 0);
    mPicPath = getIntent().getStringExtra("PicPath");
    x = getIntent().getIntExtra("x", 0);
    y = getIntent().getIntExtra("y", 0);
    setViewLayout();
    FFmpegCmdUtil.setVideoSource(mVideoPath);
    onDecoderReady();
    initListener();
    getPicBack();
    getAvailableCount();
  }

  private void setViewLayout() {
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
    int lefttopx = (int) getIntent().getFloatExtra("lefttopx", 0);
    int lefttopy = (int) getIntent().getFloatExtra("lefttopy", 0);
    layoutParams.leftMargin = lefttopx;
    layoutParams.topMargin = lefttopy;
  }

  @Override
  protected void onResume() {
    super.onResume();
    //执行完成之后 重置source
    FFmpegCmdUtil.setVideoSource(mVideoPath);
  }

  private void getAvailableCount() {
    RetrofitClient.getInstance().getApi().availableCount(UserInfoUtils.getToken(this)).subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new Consumer<BaseObjectBean<AvailableCount>>() {
        @Override
        public void accept(BaseObjectBean<AvailableCount> availableCountBaseObjectBean) throws Throwable {
          if (availableCountBaseObjectBean.getCode() == 0) {
            mAvailableCount = availableCountBaseObjectBean.getData();
          }
        }
      });
  }

  private void initListener() {
    findViewById(R.id.tv_change_random).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getPicBack();
      }
    });
    findViewById(R.id.tv_change_select).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        choicePic();
      }
    });
    mAiClearTextStatusView = ((TextStatusView) findViewById(R.id.tv_ai_clear));
    mAiClearTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (status == 1) {
          FFmpegCmdUtil.clear(60d);
        } else {
          FFmpegCmdUtil.unClear();
        }
      }
    });
    mDimTextStatusView = ((TextStatusView) findViewById(R.id.tv_background_clear));
    mDimTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (status == 1) {
          FFmpegCmdUtil.blurBackGround(ManagerVideoActivity.this, mChangePicPath, (int) x, (int) y);
        } else {
          FFmpegCmdUtil.unBlurBackGround();
        }
        isRemoveBorder = false;
        mRemoveBorderTextStatusView.setStatus(0);
      }
    });

    mRemoveBorderTextStatusView = ((TextStatusView) findViewById(R.id.tv_remove_border));
    mRemoveBorderTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (status == 1) {
          isRemoveBorder = true;
          FFmpegCmdUtil.unChangeBackGround();
          FFmpegCmdUtil.unBlurBackGround();
          mDimTextStatusView.setStatus(0);
        } else {
          isRemoveBorder = false;
        }
      }
    });
    mLowTextStatusView = findViewById(R.id.tv_ai_slow);
    mLowTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (status == 1) {
          FFmpegCmdUtil.decelerate();
          FFmpegCmdUtil.unAccelerate();
          mAccTextStatusView.setStatus(0);
        } else {
          FFmpegCmdUtil.unDecelerate();
        }
      }
    });

    mAccTextStatusView = findViewById(R.id.tv_ai_acce);
    mAccTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (status == 1) {
          FFmpegCmdUtil.accelerate();
          FFmpegCmdUtil.unDecelerate();
          mLowTextStatusView.setStatus(0);
        } else {
          FFmpegCmdUtil.unAccelerate();
        }
      }
    });
    mZipTextStatusView = ((TextStatusView) findViewById(R.id.tv_ai_zip));
    mZipTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (status == 1) {
          FFmpegCmdUtil.compress();
        } else {
          FFmpegCmdUtil.unCompress();
        }
      }
    });
    mBackTextStatusView = ((TextStatusView) findViewById(R.id.tv_ai_back));
    mBackTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (status == 1) {
          FFmpegCmdUtil.upEnd();
          FFmpegCmdUtil.removeVoice();
          isClickVoiceBySelf = false;
          mRemoveVoiceTextStatusView.setStatus(1);
        } else {
          if (!isClickVoiceBySelf) {
            FFmpegCmdUtil.unRemoveVoice();
            mRemoveVoiceTextStatusView.setStatus(0);
          }
          FFmpegCmdUtil.unUpEnd();

        }
      }
    });
    mAiReverseTextStatusView = ((TextStatusView) findViewById(R.id.tv_ai_reverse));
    mAiReverseTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (status == 1) {
          FFmpegCmdUtil.mirrorImage();
        } else {
          FFmpegCmdUtil.unMirrorImage();
        }
      }
    });
    mRemoveVoiceTextStatusView = ((TextStatusView) findViewById(R.id.tv_remove_audio));
    mRemoveVoiceTextStatusView.setStatusListener(new TextStatusView.StatusListener() {
      @Override
      public void onClick(int status) {
        if (!FFmpegCmdUtil.isContainBack()) {
          if (status == 1) {
            isClickVoiceBySelf = true;
            FFmpegCmdUtil.removeVoice();
          } else {
            FFmpegCmdUtil.unRemoveVoice();
          }
        } else {
          mRemoveVoiceTextStatusView.setStatus(1);
        }
      }
    });


    findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mAvailableCount == null) {
          ToastUtils.show("用户未激活,请激活后使用");
          return;
        }


        if (mAvailableCount.usedCount == 0) {
          ToastUtils.show("当天可用次数已使用完，请明日再来");
          return;
        }


        if (FFmpegCmdUtil.isEmpty() && isRemoveBorder) {
          startResult(mVideoPath);
          return;
        }

        if (FFmpegCmdUtil.isEmpty()) {
          ToastUtils.show("请选择要进行的操作");
          return;
        }

        if (!TextUtils.isEmpty(mVideoPath) && !FFmpegCmdUtil.isContainBlur() && !isRemoveBorder
          && fileIsExists(mChangePicPath)) {

          Log.d("wjy", "change pic" + mChangePicPath + "x" + x + "y" + y);
          FFmpegCmdUtil.changeBackGround(mChangePicPath, (int) x, (int) y);
        }


        FFmpegCmdUtil.start(new FFmpegCmdUtil.ProcessListener() {
          @Override
          public void onStart() {
            progressPopup = (ProgressPopup) new XPopup.Builder(ManagerVideoActivity.this)
              .setPopupCallback(new SimpleCallback() {
                @Override
                public void onDismiss(BasePopupView popupView) {
                  super.onDismiss(popupView);
                  FFmpegCmdUtil.release();
                }
              })
              .dismissOnTouchOutside(false)
              .asCustom(new ProgressPopup(ManagerVideoActivity.this))
              .show();
            progressPopup.setTotalTime(FileSizeUtil.getVideoDuration(mVideoPath));
          }

          @Override
          public void onProcess(String cmd, int total, int pos, int process, long processTime) {
            progressPopup.setData(getText(cmd), pos, total, process, processTime);
          }

          @Override
          public void onSingleFinish(String cmd) {

          }

          @Override
          public void onAllFinish(String videoSource) {
            progressPopup.dismiss();
            startResult(videoSource);
          }

          @Override
          public void onError(String cmd) {
            progressPopup.dismiss();
            ToastUtils.show("执行失败，请重试");
          }
        });
      }
    });

    findViewById(R.id.tv_set_out).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new XPopup.Builder(v.getContext())
          .asCustom(new BottomOutDialog(v.getContext()))
          .show();
      }
    });

  }

  private void startResult(String mVideoPath) {
    minusCount();
    // FFmpegCmdUtil.clearAll();
    // clearTextStatus();
    // isRemoveBorder = false;
    Intent intent = new Intent(ManagerVideoActivity.this, ResultVideoActivity.class);
    intent.putExtra("videoPath", mVideoPath);
    startActivity(intent);
  }

  private void clearTextStatus() {
    mAccTextStatusView.setStatus(0);
    mBackTextStatusView.setStatus(0);
    mDimTextStatusView.setStatus(0);
    mRemoveVoiceTextStatusView.setStatus(0);
    mLowTextStatusView.setStatus(0);
    mZipTextStatusView.setStatus(0);
    mAiClearTextStatusView.setStatus(0);
    mRemoveBorderTextStatusView.setStatus(0);
    mAiReverseTextStatusView.setStatus(0);
  }

  private void minusCount() {
    RetrofitClient.getInstance().getApi().minusCount(UserInfoUtils.getToken(this)).subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new Consumer<BaseObjectBean<Object>>() {
        @Override
        public void accept(BaseObjectBean<Object> availableCountBaseObjectBean) throws Throwable {
        }
      });
  }

  private static String getText(String key) {
    switch (key) {
      case CMD_CLEAR:
        return "智能清洗";
      case CMD_ACCELERATE:
        return "智能加速";
      case CMD_DECELERATE:
        return "智能减速";
      case CMD_REMOVE_VOICE:
        return "去除声音";
      case CMD_CHANGE_BACKGROUND:
        return "更改背景";
      case CMD_UP_END:
        return "智能倒放";
      case CMD_MIRROR_IMAGE:
        return "智能翻转";
      case CMD_COMPRESS:
        return "智能压缩";
      case CMD_DIM_BACKGROUND:
        return "背景模糊";
    }
    return "准备环境";
  }


  private void onDecoderReady() {

    Log.d("wjy", "onDecoderReady mVideoWidth" + mVideoWidth);
    Log.d("wjy", "onDecoderReady mVideoHeight" + mVideoHeight);

    ViewGroup.LayoutParams surfaceViewLayoutParams = mImageView.getLayoutParams();
    surfaceViewLayoutParams.width = (int) mVideoWidth;
    surfaceViewLayoutParams.height = (int) mVideoHeight;

    ViewGroup.LayoutParams layoutParams = mViewBack.getLayoutParams();
    layoutParams.width = (int) mViewWidth;
    layoutParams.height = (int) mViewHeight;

    ViewGroup.LayoutParams layoutParams1 = mBackLayout.getLayoutParams();
    layoutParams1.width = (int) mViewWidth;
    layoutParams1.height = (int) mViewHeight;


    Glide.with(this).load(mPicPath).into(mImageView);
  }

  public void getPicBack() {
    String token = UserInfoUtils.getToken(this);
    RetrofitClient.getInstance().getApi().getPic(mSourceWidth > mSourceHeight ? 0 : 1, token).subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new Consumer<BaseObjectBean<PicBean>>() {
        @Override
        public void accept(BaseObjectBean<PicBean> picBeanBaseObjectBean) throws Throwable {
          if (picBeanBaseObjectBean.getCode() == 0 && picBeanBaseObjectBean.getData() != null) {
            long time = SystemClock.elapsedRealtime();
            String picPath = ContextCompat.getExternalFilesDirs(ManagerVideoActivity.this,
              Environment.DIRECTORY_DCIM)[0].getAbsolutePath() + "/back_" + time + ".png";
            ImageUtils.saveImgToLocal(ManagerVideoActivity.this, picBeanBaseObjectBean.getData().url, picPath, (int) mSourceWidth, (int) mSourceHeight);
            mChangePicPath = picPath;
            loadBackPic(picBeanBaseObjectBean.getData().url);
            Log.d("wjy", "mChangePicPath" + mChangePicPath);
            clearAll();
            clearTextStatus();
          }
        }
      });
  }

  private void choicePic() {
    Intent intent = new Intent(Intent.ACTION_PICK, null);
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
    startActivityForResult(intent, 2);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 2 && resultCode == RESULT_OK) {
      if (data != null) {
        // 得到图片的全路径
        Uri uri = data.getData();
        String path = ImageUtils.getRealPathFromUri(ManagerVideoActivity.this, uri);
        String picPath = ContextCompat.getExternalFilesDirs(ManagerVideoActivity.this,
          Environment.DIRECTORY_DCIM)[0].getAbsolutePath() + "/back_" + SystemClock.elapsedRealtime() + ".png";
        ImageUtils.saveImgToLocal(ManagerVideoActivity.this, path, picPath, (int) mSourceWidth, (int) mSourceHeight);

        mChangePicPath = picPath;
        loadBackPic(path);
        clearAll();
        clearTextStatus();
      }
    }
  }

  public void loadBackPic(String url) {
    Glide.with(ManagerVideoActivity.this)
      .asBitmap()
      .load(url)
      .skipMemoryCache(true)
      .into(new SimpleTarget<Bitmap>() {

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
          mPicWidth = resource.getWidth();  //图片高度
          mPicHeight = resource.getHeight();  //图片宽度
          mViewBack.setImageBitmap(resource);
        }

      });
  }

  @Override
  protected void onStop() {
    super.onStop();
    FFmpegCmdUtil.release();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    FFmpegCmdUtil.clearAll();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (progressPopup != null) {
      progressPopup.dismiss();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      FFmpegCmdUtil.release();
    }
    return super.onKeyDown(keyCode, event);
  }

  public boolean fileIsExists(String fileName) {
    try {
      File f = new File(fileName);
      if (f.exists()) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

}
