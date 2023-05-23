package com.collect_beautiful_video.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.collect_beautiful_video.R;

@SuppressLint("AppCompatCustomView")
public class TextStatusView extends TextView {
  public int status = 0;
  public StatusListener mStatusListener;

  public TextStatusView(Context context) {
    super(context);
  }

  public TextStatusView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public TextStatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public TextStatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }


  public void setStatusListener(StatusListener mStatusListener) {
    this.mStatusListener = mStatusListener;
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (status == 0) {
          setBackgroundResource(R.drawable.select_text);
          status = 1;
          mStatusListener.onClick(1);
        } else {
          setBackgroundResource(R.drawable.un_select_text);
          status = 0;
          mStatusListener.onClick(0);
        }
      }
    });
  }

  public void setStatus(int temp){
    if (temp == 1) {
      setBackgroundResource(R.drawable.select_text);
      status = 1;
    } else {
      setBackgroundResource(R.drawable.un_select_text);
      status = 0;
    }
  }

  public interface StatusListener {
    void onClick(int status);
  }
}
