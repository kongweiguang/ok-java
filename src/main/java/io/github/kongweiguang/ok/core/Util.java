package io.github.kongweiguang.ok.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Objects;

public final class Util {

  private Util() {
  }

  public static String replacePath(String path) {
    if (isNull(path)) {
      return "";
    }

    if (path.startsWith("/")) {
      path = path.replaceFirst("/", "");
    }
    return path;
  }

  public static String urlRegex(String url) {
    if (isNull(url) || Objects.equals("", url)) {
      url = "/";
    }

    if (!isHttp(url) && !isHttps(url)) {
      if (url.startsWith("/")) {
        url = Const._http + Const.localhost + url;
      } else {
        url = Const._http + url;
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
      return url.toLowerCase().startsWith(Const._http);
    }

    return false;
  }

  public static boolean isHttps(final String url) {
    if (nonNull(url)) {
      return url.toLowerCase().startsWith(Const._https);
    }

    return false;
  }
}
