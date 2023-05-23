package com.collect_beautiful_video.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.collect_beautiful_video.R;
import com.collect_beautiful_video.bean.BaseObjectBean;
import com.collect_beautiful_video.bean.LoginResultBean;
import com.collect_beautiful_video.bean.SendMessageBean;
import com.collect_beautiful_video.http.RetrofitClient;
import com.collect_beautiful_video.util.StringUtil;
import com.collect_beautiful_video.util.UserInfoUtils;
import com.google.gson.Gson;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;

import java.util.HashMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;

public class LoginActivity extends Activity {

    private CountDownTimer countDownTimer;

    private EditText mEtPhone;
    private EditText mEtPassword;
    private TextView mTvTimer;
    private ImageView mIvIcon;
    private boolean isSelect = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEtPhone = findViewById(R.id.et_telephone);
        mEtPassword = findViewById(R.id.et_password);
        mIvIcon = findViewById(R.id.iv_select);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        mTvTimer = findViewById(R.id.tv_timer);
        mTvTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageCode();
            }
        });

        mIvIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelect) {
                    mIvIcon.setImageResource(R.mipmap.un_select);
                } else {
                    mIvIcon.setImageResource(R.mipmap.icon_select);
                }
                isSelect = !isSelect;
            }
        });
        findViewById(R.id.tv_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, WebViewActivity.class);
                intent.putExtra("title","用户服务协议");
                intent.putExtra("url","https://jm.changyangdt.com/agreement.html");
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_user_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, WebViewActivity.class);
                intent.putExtra("title","隐私政策");
                intent.putExtra("url","https://jm.changyangdt.com/privacy.html");
                startActivity(intent);

            }
        });

    }

    private LoadingPopupView loadingPopupView;

    private void login() {
        String telephone = mEtPhone.getEditableText().toString();
        String password = mEtPassword.getEditableText().toString();
        if (!StringUtil.verifyPhone(telephone)) {
            ToastUtils.show("请输入正确的手机号码");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtils.show("请输入验证码");
            return;
        }
        if (!isSelect) {
            ToastUtils.show("请勾选用户协议");
            return;
        }
        loadingPopupView = (LoadingPopupView) new XPopup.Builder(this)
                .dismissOnTouchOutside(false)
                .asLoading("正在登录...")
                .show();

        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("phone", telephone);
        paramsMap.put("code", password);

        String strEntity = new Gson().toJson(paramsMap);

        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), strEntity);

        RetrofitClient.getInstance().getApi().login(body).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseObjectBean<LoginResultBean>>() {
                    @Override
                    public void accept(BaseObjectBean<LoginResultBean> loginResultBeanBaseObjectBean) throws Throwable {
                        if (loginResultBeanBaseObjectBean.getCode() == 0) {
                            UserInfoUtils.saveToken(LoginActivity.this, loginResultBeanBaseObjectBean.getData().token);
                            UserInfoUtils.saveUserInfo(LoginActivity.this, new Gson().toJson(loginResultBeanBaseObjectBean.getData()));
                            finish();
                        } else {
                            if (!TextUtils.isEmpty(loginResultBeanBaseObjectBean.getMsg())) {
                                ToastUtils.show(loginResultBeanBaseObjectBean.getMsg());
                            } else {
                                ToastUtils.show("登录失败");
                            }
                        }
                        loadingPopupView.dismiss();
                    }

                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        Log.d("wjy", throwable.toString());
                    }
                });

    }

    private void sendMessageCode() {
        String telephone = mEtPhone.getEditableText().toString();
        if (StringUtil.verifyPhone(telephone)) {
            HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("phone", telephone);

            String strEntity = new Gson().toJson(paramsMap);

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), strEntity);

            RetrofitClient.getInstance().getApi().sendPhoneMessage(body).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<BaseObjectBean<SendMessageBean>>() {
                        @Override
                        public void accept(BaseObjectBean<SendMessageBean> sendMessageBeanBaseObjectBean) throws Throwable {
                            if (sendMessageBeanBaseObjectBean.getCode() == 0) {
                                startTimer();
                            } else {
                                ToastUtils.show("获取验证码失败");
                            }
                        }
                    });
        } else {
            ToastUtils.show("请输入正确的手机号码");
        }
    }

    private void startTimer() {
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(60 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mTvTimer.setEnabled(false);
                    if (millisUntilFinished > 0) {
                        mTvTimer.setText((millisUntilFinished / 1000) + "秒重发发送");
                    }
                }

                @Override
                public void onFinish() {
                    mTvTimer.setEnabled(true);
                    mTvTimer.setText("重新发送验证码");
                }
            };
        }
        countDownTimer.start();
    }
}
