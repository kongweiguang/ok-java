package io.github.kongweiguang.ok.core;

import static io.github.kongweiguang.ok.core.Const._http;
import static io.github.kongweiguang.ok.core.Const._https;
import static io.github.kongweiguang.ok.core.Const._ws;
import static io.github.kongweiguang.ok.core.Const._wss;
import static io.github.kongweiguang.ok.core.Const.localhost;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * ok-java内部使用工具
 */
public final class Util {

  private Util() {
    throw new RuntimeException("util not be constructor");
  }

  //移除第一个斜杠
  public static String removeFirstSlash(String path) {
    if (isNull(path)) {
      return "";
    }

    if (path.startsWith("/")) {
      path = path.replaceFirst("/", "");
    }
    return path;
  }

  //url校验
  public static String fixUrl(String url, boolean isWs) {
    if (isNull(url) || Objects.equals("", url)) {
      url = "/";
    }

    if (isWs) {
      if (!isWs(url) && !isWss(url)) {
        if (url.startsWith("/")) {
          url = _http + localhost + url;
        } else {
          url = _http + url;
        }
      }
      if (isWs(url)) {
        url = url.replaceFirst(_ws, _http);
      }

      if (isWss(url)) {
        url = url.replaceFirst(_wss, _https);

      }
      return url;
    }

    if (!isHttp(url) && !isHttps(url)) {
      if (url.startsWith("/")) {
        url = _http + localhost + url;
      } else {
        url = _http + url;
      }
    }

    return url;
  }

  public static void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignored) {

    }
  }

  public static boolean isHttp(final String url) {
    if (nonNull(url)) {
      return url.toLowerCase().startsWith(_http);
    }

    return false;
  }

  public static boolean isHttps(final String url) {
    if (nonNull(url)) {
      return url.toLowerCase().startsWith(_https);
    }

    return false;
  }

  public static boolean isWs(final String url) {
    if (nonNull(url)) {
      return url.toLowerCase().startsWith(_ws);
    }

    return false;
  }

  public static boolean isWss(final String url) {
    if (nonNull(url)) {
      return url.toLowerCase().startsWith(_wss);
    }

    return false;
  }

  public static void sync(final Object obj) {
    synchronized (obj) {
      try {
        obj.wait();
      } catch (InterruptedException ignored) {
      }
    }
  }

  public static void notNull(final Object obj, final String msg) {
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }

  public static void isTure(final boolean bool, final String msg) {
    if (!bool) {
      throw new IllegalArgumentException(msg);
    }
  }


  //cookie转字符串
  public static String cookie2Str(final Map<String, String> cookies) {
    StringBuilder sb = new StringBuilder();

    cookies.forEach((k, v) -> sb.append(k).append('=').append(v).append("; "));

    return sb.toString();
  }

  public static String encode(final String str) {
    return Base64.getEncoder().encodeToString(str.getBytes());
  }

}
