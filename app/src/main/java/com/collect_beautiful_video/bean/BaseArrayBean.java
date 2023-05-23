package com.collect_beautiful_video.bean;

import java.util.List;

public class BaseArrayBean<T> {

  /**
   * status : 1
   * msg : 获取成功
   * result : [] 数组
   */

  private int code;
  private String msg;
  private List<T> data;

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

  public List<T> getData() {
    return data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }
}

