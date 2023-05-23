package com.collect_beautiful_video.util;

import android.text.TextUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

  public static String format(CharSequence template, Object... params) {
    if (null == template) {
      return null;
    } else {
      return params != null && !TextUtils.isEmpty(template) ? format(template.toString(), params) : template.toString();
    }
  }

  public static String format(String strPattern, Object... argArray) {
    if (!TextUtils.isEmpty(strPattern) && argArray != null) {
      int strPatternLength = strPattern.length();
      StringBuilder sbuf = new StringBuilder(strPatternLength + 50);
      int handledPosition = 0;

      for (int argIndex = 0; argIndex < argArray.length; ++argIndex) {
        int delimIndex = strPattern.indexOf("{}", handledPosition);
        if (delimIndex == -1) {
          if (handledPosition == 0) {
            return strPattern;
          }

          sbuf.append(strPattern, handledPosition, strPatternLength);
          return sbuf.toString();
        }

        if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == '\\') {
          if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == '\\') {
            sbuf.append(strPattern, handledPosition, delimIndex - 1);
            sbuf.append(utf8Str(argArray[argIndex]));
            handledPosition = delimIndex + 2;
          } else {
            --argIndex;
            sbuf.append(strPattern, handledPosition, delimIndex - 1);
            sbuf.append('{');
            handledPosition = delimIndex + 1;
          }
        } else {
          sbuf.append(strPattern, handledPosition, delimIndex);
          sbuf.append(utf8Str(argArray[argIndex]));
          handledPosition = delimIndex + 2;
        }
      }

      sbuf.append(strPattern, handledPosition, strPattern.length());
      return sbuf.toString();
    } else {
      return strPattern;
    }
  }

  public static String utf8Str(Object obj) {
    return str(obj, Charset.forName("UTF-8"));
  }

  public static String str(Object obj, Charset charset) {
    if (null == obj) {
      return null;
    } else if (obj instanceof String) {
      return (String) obj;
    } else if (obj instanceof byte[]) {
      return str((byte[]) ((byte[]) obj), charset);
    } else if (obj instanceof Byte[]) {
      return str((Byte[]) ((Byte[]) obj), charset);
    } else if (obj instanceof ByteBuffer) {
      return str((ByteBuffer) obj, charset);
    } else {
      return isArray(obj) ? toString(obj) : obj.toString();
    }
  }

  public static boolean isArray(Object obj) {
    return null == obj ? false : obj.getClass().isArray();
  }

  public static String toString(Object obj) {
    if (null == obj) {
      return null;
    } else if (obj instanceof long[]) {
      return Arrays.toString((long[]) ((long[]) obj));
    } else if (obj instanceof int[]) {
      return Arrays.toString((int[]) ((int[]) obj));
    } else if (obj instanceof short[]) {
      return Arrays.toString((short[]) ((short[]) obj));
    } else if (obj instanceof char[]) {
      return Arrays.toString((char[]) ((char[]) obj));
    } else if (obj instanceof byte[]) {
      return Arrays.toString((byte[]) ((byte[]) obj));
    } else if (obj instanceof boolean[]) {
      return Arrays.toString((boolean[]) ((boolean[]) obj));
    } else if (obj instanceof float[]) {
      return Arrays.toString((float[]) ((float[]) obj));
    } else if (obj instanceof double[]) {
      return Arrays.toString((double[]) ((double[]) obj));
    } else {
      if (isArray(obj)) {
        try {
          return Arrays.deepToString((Object[]) ((Object[]) obj));
        } catch (Exception var2) {
        }
      }

      return obj.toString();
    }
  }

  public static boolean verifyPhone(String mobile) {
    String regex = "^((13[0-9])|(14[0,1,4-9])|(15[0-3,5-9])|(16[2,5,6,7])|(17[0-8])|(18[0-9])|(19[0-3,5-9]))\\d{8}$";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(mobile);
    return m.matches();
  }
}
