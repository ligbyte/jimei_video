package com.collect_beautiful_video;

import android.app.Application;
import android.util.Log;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.BuildConfig;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tencent.bugly.Bugly;
import com.umeng.commonsdk.UMConfigure;

import java.io.IOException;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class CollectBeautifulApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    setRxJavaErrorHandler();
    ToastUtils.init(this);
    RxFFmpegInvoke.getInstance().setDebug(true);
    Bugly.init(this, "e77bf812f8",true);
    UMConfigure.init(this, "62d8c59488ccdf4b7ed98456", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
    /**
     *设置组件化的Log开关
     *参数: boolean 默认为false，如需查看LOG设置为true
     */
    UMConfigure.setLogEnabled(true);

    DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
//                .showImageOnLoading(new ColorDrawable(Color.parseColor("#EEEEEE")))
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .build();

    ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(this)
            .defaultDisplayImageOptions(displayImageOptions)
            .build();
    ImageLoader.getInstance().init(imageLoaderConfiguration);

  }

  private void setRxJavaErrorHandler() {
    RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
      @Override
      public void accept(Throwable e) {
        if (e instanceof UndeliverableException) {
          e = e.getCause();
          Log.d("wjy", "UndeliverableException=" + e);
          return;
        } else if ((e instanceof IOException)) {
          // fine, irrelevant network problem or API that throws on cancellation
          return;
        } else if (e instanceof InterruptedException) {
          // fine, some blocking code was interrupted by a dispose call
          return;
        } else if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
          // that's likely a bug in the application
          Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
          return;
        } else if (e instanceof IllegalStateException) {
          // that's a bug in RxJava or in a custom operator
          Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
          return;
        }
        Log.d("wjy", "unknown exception=" + e);
      }
    });

  }
}
