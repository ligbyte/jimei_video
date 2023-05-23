package com.collect_beautiful_video.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.collect_beautiful_video.bean.LoginResultBean;
import com.google.gson.Gson;

public class UserInfoUtils {

  public static void saveToken(Context context, String token) {
    if (context != null) {
      SharedPreferences sharedPreferences = context.getSharedPreferences("collect_beautiful_vide0", Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString("token", token);
      editor.apply();
    }
  }

  public static String getToken(Context context) {
    if (context != null) {
      SharedPreferences sharedPreferences = context.getSharedPreferences("collect_beautiful_vide0", Context.MODE_PRIVATE);
      return "Bearer " + sharedPreferences.getString("token", "");
    }
    return "";
  }

  public static boolean isLogin(Context context) {
    if (context != null) {
      SharedPreferences sharedPreferences = context.getSharedPreferences("collect_beautiful_vide0", Context.MODE_PRIVATE);
      return !TextUtils.isEmpty(sharedPreferences.getString("token", ""));
    }
    return false;
  }

  public static void saveUserInfo(Context context, String userJson) {
    if (context != null) {
      SharedPreferences sharedPreferences = context.getSharedPreferences("collect_beautiful_vide0", Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString("userInfo", userJson);
      editor.apply();
    }
  }

  public static LoginResultBean getUserInfo(Context context) {
    if (context != null) {
      SharedPreferences sharedPreferences = context.getSharedPreferences("collect_beautiful_vide0", Context.MODE_PRIVATE);
      String user = sharedPreferences.getString("userInfo", "");

      return new Gson().fromJson(user, LoginResultBean.class);
    }
    return new LoginResultBean();
  }
}
