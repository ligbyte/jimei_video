package com.collect_beautiful_video.activity;

import static android.media.MediaPlayer.SEEK_CLOSEST;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.collect_beautiful_video.R;
import com.collect_beautiful_video.media.MyGLSurfaceView;
import com.collect_beautiful_video.util.FileSizeUtil;
import com.collect_beautiful_video.util.ImageUtils;
import com.yhd.mediaplayer.MediaPlayerHelper;

import java.io.File;

public class ResultVideoActivity extends FragmentActivity {

    private String mVideoPath;
    private float mVideoWidth;
    private float mVideoHeight;
    private MyGLSurfaceView myGLSurfaceView;
    private ImageView imageView;
    private SeekBar sbOne;
    private long totalTime;
    private boolean isClick = false;
    private boolean isAuto = false;


    @Override
    protected void onStop() {
        super.onStop();
        if(MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
          MediaPlayerHelper.getInstance().getMediaPlayer().stop();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_video);
        mVideoPath = getIntent().getStringExtra("videoPath");
        myGLSurfaceView = findViewById(R.id.surface_view);
        sbOne = findViewById(R.id.sb_one);
        MediaPlayerHelper.getInstance().reCreateMediaPlayer();
        MediaPlayerHelper.getInstance().setSurfaceView(myGLSurfaceView).playLocal(this, mVideoPath);
        MediaPlayerHelper.getInstance().getMediaPlayer().start();
        totalTime = FileSizeUtil.getVideoDuration(mVideoPath);
        imageView = findViewById(R.id.iv_result);
        sbOne.setProgress(0);

        MediaPlayerHelper.getInstance().setOnStatusCallbackListener(new MediaPlayerHelper.OnStatusCallbackListener() {
            @Override
            public void onStatusonStatusCallbackNext(MediaPlayerHelper.CallBackState status, Object... args) {

                Log.d("wjy", status.toString());
                if (status == MediaPlayerHelper.CallBackState.PREPARE) {
                    onDecoderReady();
                }
                if (status == MediaPlayerHelper.CallBackState.INFO) {
                    imageView.setImageResource(R.mipmap.new_all_pause);
                    imageView.setVisibility(View.GONE);
                    sbOne.setVisibility(View.GONE);
                }
                if (status == MediaPlayerHelper.CallBackState.COMPLETE) {
                    imageView.setImageResource(R.mipmap.new_all_play);
                    imageView.setVisibility(View.VISIBLE);
                    sbOne.setVisibility(View.VISIBLE);
                }
                if (status == MediaPlayerHelper.CallBackState.PROGRESS) {
                    if (!isClick) {
                        isAuto = true;
                        sbOne.setProgress((int) args[0]);
                    }
                }
            }
        });


        sbOne.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!isAuto) {
                    if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
                        long time = (long) (((totalTime * 1.0f) / 100000f) * i * 1.0f);
                        MediaPlayerHelper.getInstance().getMediaPlayer().seekTo(time, SEEK_CLOSEST);
                        if(!MediaPlayerHelper.getInstance().getMediaPlayer().isPlaying()){
                            MediaPlayerHelper.getInstance().getMediaPlayer().start();
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isClick = true;
                isAuto = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isClick = false;
                isAuto = true;
            }
        });

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.tv_save_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              File file = new File(mVideoPath);
              if(file.exists()) {
                String tempFile = ImageUtils.getSDCardDCIMFile(".mp4");
                ImageUtils.copyFileUsingFileChannels(mVideoPath, tempFile);
                ImageUtils.saveVideoToSystemAlbum(tempFile, ResultVideoActivity.this);
              }
            }
        });


        myGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    imageView.setVisibility(View.VISIBLE);
                    sbOne.setVisibility(View.VISIBLE);
                    if (MediaPlayerHelper.getInstance().getMediaPlayer() != null) {
                        if (MediaPlayerHelper.getInstance().getMediaPlayer().isPlaying()) {
                            imageView.setImageResource(R.mipmap.new_all_play);
                            MediaPlayerHelper.getInstance().getMediaPlayer().pause();
                        } else {
                            imageView.setImageResource(R.mipmap.new_all_pause);
                            MediaPlayerHelper.getInstance().getMediaPlayer().start();
                        }
                    }
                }
                return false;
            }
        });


    }

    private void onDecoderReady() {
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            return;
        }

        mVideoWidth = MediaPlayerHelper.getInstance().getMediaPlayer().getVideoWidth();
        mVideoHeight = MediaPlayerHelper.getInstance().getMediaPlayer().getVideoHeight();


        if (mVideoHeight * mVideoWidth != 0) {
            myGLSurfaceView.setAspectRatio((int) mVideoWidth, (int) mVideoHeight);
        }
    }
}
