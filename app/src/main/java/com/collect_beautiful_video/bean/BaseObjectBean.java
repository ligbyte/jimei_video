package com.collect_beautiful_video.bean;


public class BaseObjectBean<T> {

  /**
   * status : 1
   * msg : 获取成功
   * result : {} 对象
   */

  private int code;
  private String msg;
  private T data;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
