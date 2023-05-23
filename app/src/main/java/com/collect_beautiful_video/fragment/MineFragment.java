package com.collect_beautiful_video.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.collect_beautiful_video.R;
import com.collect_beautiful_video.activity.LoginActivity;
import com.collect_beautiful_video.bean.BaseObjectBean;
import com.collect_beautiful_video.bean.LoginResultBean;
import com.collect_beautiful_video.http.RetrofitClient;
import com.collect_beautiful_video.util.UserInfoUtils;
import com.google.gson.Gson;
import com.hjq.toast.ToastUtils;

import java.util.HashMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;

public class MineFragment extends Fragment {

    private TextView mTvUserInfoName;
    private ImageView mAvatar;
    private TextView mTvLogin;
    private TextView mTvStatus;
    private TextView mTvStatusDesc;
    private TextView mTvRegister;
    private AlertDialog alertDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mine, container, false);
        rootView.findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });
        mTvUserInfoName = rootView.findViewById(R.id.tv_userinfo_name);
        mAvatar = rootView.findViewById(R.id.iv_avatar);
        mTvLogin = rootView.findViewById(R.id.tv_login);
        mTvStatus = rootView.findViewById(R.id.tv_status);
        mTvStatusDesc = rootView.findViewById(R.id.tv_status_desc);
        mTvRegister = rootView.findViewById(R.id.tv_register);
        mTvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserInfoUtils.isLogin(getActivity())) {
                    logout();
                } else {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            }
        });

        mTvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserInfoUtils.isLogin(getActivity())) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                } else {
                    showInPutDialog();
                }
            }
        });

        registerReceiver();

        return rootView;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.refresh.userinfo");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getUserInfo();
            }
        }, intentFilter);
    }

    private void showInPutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("激活！");
        EditText editText = new EditText(getActivity());
        builder.setView(editText);
        builder.setPositiveButton("激活", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    ToastUtils.show("请输入激活码");
                    return;
                }
                invalidCode(editText.getText().toString());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void invalidCode(String code) {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("invitationCode", code);

        String strEntity = new Gson().toJson(paramsMap);

        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), strEntity);

        RetrofitClient.getInstance().getApi().invitationCode(body, UserInfoUtils.getToken(getActivity())).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseObjectBean<Object>>() {
                    @Override
                    public void accept(BaseObjectBean<Object> loginResultBeanBaseObjectBean) throws Throwable {
                        if (loginResultBeanBaseObjectBean.getCode() == 0) {
                            ToastUtils.show("激活成功");
                            getUserInfo();
                        } else {
                            if (!TextUtils.isEmpty(loginResultBeanBaseObjectBean.getMsg())) {
                                ToastUtils.show(loginResultBeanBaseObjectBean.getMsg());
                            } else {
                                ToastUtils.show("激活失败");
                            }
                        }
                    }
                });
    }

    public void getUserInfo() {
        if (UserInfoUtils.isLogin(getActivity())) {
            RetrofitClient.getInstance().getApi().getUserInfo(UserInfoUtils.getToken(getActivity())).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<BaseObjectBean<LoginResultBean>>() {
                        @Override
                        public void accept(BaseObjectBean<LoginResultBean> loginResultBeanBaseObjectBean) throws Throwable {
                            if (loginResultBeanBaseObjectBean.getCode() == 0) {
                                refreshUserInfo(loginResultBeanBaseObjectBean.getData());
                            } else {
                                refreshUserInfo(null);
                            }
                        }
                    });
        } else {
            refreshUserInfo(null);
        }
    }

    private void logout() {
        UserInfoUtils.saveUserInfo(getActivity(), "");
        UserInfoUtils.saveToken(getActivity(), "");
        mTvLogin.setText("去登录");
        mTvUserInfoName.setText("未登录");
        mTvStatus.setText("未激活");
        mTvRegister.setVisibility(View.VISIBLE);
        mTvStatusDesc.setText("激活后方可以使用");
        mAvatar.setImageResource(R.mipmap.avatar_default);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getUserInfo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();

    }

    private void refreshUserInfo(LoginResultBean loginResultBean) {
        if (loginResultBean == null) {
            loginResultBean = UserInfoUtils.getUserInfo(getActivity());
        }
        if (loginResultBean != null) {
            if (mTvUserInfoName != null) {
                mTvUserInfoName.setText(loginResultBean.nickName);
            }
            if (!TextUtils.isEmpty(loginResultBean.avatarUrl)) {
                Glide.with(this).load(loginResultBean.avatarUrl).apply(RequestOptions.bitmapTransform(new RoundedCorners(600)))
                        .into(mAvatar);
            }
            mTvLogin.setText("退出登录");
            if (mTvStatus != null  && !TextUtils.isEmpty(loginResultBean.expireTime)) {
                mTvStatus.setText("已激活");
                mTvRegister.setVisibility(View.GONE);
                if (mTvStatusDesc != null) {
                    mTvStatusDesc.setText("有效期至: " + loginResultBean.expireTime + "   " + "今日剩余 " +
                            loginResultBean.surplusCount + "次");
                }
            } else {
                mTvStatus.setText("未激活");
                mTvRegister.setVisibility(View.VISIBLE);
                mTvStatusDesc.setText("激活后方可以使用");
            }
        } else {
            logout();
        }


    }

}
