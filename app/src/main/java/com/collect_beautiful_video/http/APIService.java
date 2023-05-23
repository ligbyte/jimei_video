package com.collect_beautiful_video.http;

import com.collect_beautiful_video.bean.AvailableCount;
import com.collect_beautiful_video.bean.BaseObjectBean;
import com.collect_beautiful_video.bean.LoginResultBean;
import com.collect_beautiful_video.bean.PicBean;
import com.collect_beautiful_video.bean.SendMessageBean;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface APIService {


  @POST("jimei/auth/message")
  @Headers({"Content-Type:application/json"})
  Observable<BaseObjectBean<SendMessageBean>> sendPhoneMessage(@Body RequestBody route);

  @POST("jimei/auth/login")
  @Headers({"Content-Type:application/json"})
  Observable<BaseObjectBean<LoginResultBean>> login(@Body RequestBody body);


  @GET("jimei/vc-background")
  @Headers({"Content-Type:application/json"})
  Observable<BaseObjectBean<PicBean>> getPic(@Query("type") int type,@Header("Authorization") String authorization);


  @GET("jimei/vc-user/detail")
  @Headers({"Content-Type:application/json"})
  Observable<BaseObjectBean<LoginResultBean>> getUserInfo(@Header("Authorization") String authorization);


  @PUT("jimei/vc-invitation-code")
  @Headers({"Content-Type:application/json"})
  Observable<BaseObjectBean<Object>> invitationCode(@Body RequestBody invitationCode, @Header("Authorization") String authorization);


  @GET("jimei/vc-video/availableCount")
  @Headers({"Content-Type:application/json"})
  Observable<BaseObjectBean<AvailableCount>> availableCount(@Header("Authorization") String authorization);


  @POST("jimei/vc-video/minusCount")
  @Headers({"Content-Type:application/json"})
  Observable<BaseObjectBean<Object>> minusCount(@Header("Authorization") String authorization);

}
