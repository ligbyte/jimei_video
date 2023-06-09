package com.collect_beautiful_video.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.collect_beautiful_video.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.lxj.xpopup.animator.PopupAnimator;
import com.lxj.xpopup.core.CenterPopupView;

public class ProgressPopup extends CenterPopupView {

    //注意：自定义弹窗本质是一个自定义View，但是只需重写一个参数的构造，其他的不要重写，所有的自定义弹窗都是这样。
    public ProgressPopup(@NonNull Context context) {
        super(context);
    }

    private TextView title;
    private TextView status;
    private long totalTime;
    private LinearProgressIndicator linearProgressIndicator;

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.progress_popup;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        title = findViewById(R.id.tv_title_progress);
        status = findViewById(R.id.tv_progress);
        linearProgressIndicator = findViewById(R.id.progress);
        linearProgressIndicator.setMax(100);


    }

    public void setData(String progress, int pos, int total, int process, long processTime) {
        if (title != null) {
            title.setText("正在执行" + progress);
        }
        if (status != null) {
            status.setText(pos + "/" + total);
        }
        if (linearProgressIndicator != null) {
            if (totalTime != 0) {
                double time = (processTime * 1.0 / totalTime) * 100;
                if (time > 100) {
                    time = 100;
                }
                if (time < 0) {
                    time = 0;
                }
                linearProgressIndicator.setProgress((int) time);

            } else {
                linearProgressIndicator.setProgress(process);
            }
        }
    }

    public void setTotalTime(long time) {
        totalTime = time;
    }

    // 设置最大宽度，看需要而定，
    @Override
    protected int getMaxWidth() {
        return super.getMaxWidth();
    }

    // 设置最大高度，看需要而定
    @Override
    protected int getMaxHeight() {
        return super.getMaxHeight();
    }

    // 设置自定义动画器，看需要而定
    @Override
    protected PopupAnimator getPopupAnimator() {
        return super.getPopupAnimator();
    }

    /**
     * 弹窗的宽度，用来动态设定当前弹窗的宽度，受getMaxWidth()限制
     *
     * @return
     */
    protected int getPopupWidth() {
        return 0;
    }

    /**
     * 弹窗的高度，用来动态设定当前弹窗的高度，受getMaxHeight()限制
     *
     * @return
     */
    protected int getPopupHeight() {
        return 0;
    }
}
