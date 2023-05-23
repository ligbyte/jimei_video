package com.collect_beautiful_video.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.collect_beautiful_video.R;
import com.lxj.xpopup.core.BottomPopupView;

public class BottomOutDialog extends BottomPopupView {

    private SeekBar sbOne;
    private SeekBar sbTwo;
    private Context mContext;

    public BottomOutDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.custom_bottom_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        sbOne = findViewById(R.id.sb_one);
        sbTwo = findViewById(R.id.sb_two);

        sbOne.setProgress(getOne());
        sbTwo.setProgress(getTwo());

        sbOne.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                saveOne(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbTwo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                saveTwo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void saveOne(int one) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("collect_beautiful", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("FPS_ONE", one);
        editor.commit();
    }

    public void saveTwo(int two) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("collect_beautiful", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("FPS_TWO", two);
        editor.commit();
    }

    public int getOne() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("collect_beautiful", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("FPS_ONE", 2);
    }

    public int getTwo() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("collect_beautiful", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("FPS_TWO", 3);
    }


}
