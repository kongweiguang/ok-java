package io.github.kongweiguang.ok.core;

import static java.util.Objects.isNull;

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

    if (!url.startsWith(Const._http) && !url.startsWith(Const._https)) {
      if (url.startsWith("/")) {
        url = Const._http + Const.localhost + url;
      } else {
        url = Const._http + url;
      }
    }

    return url;
  }

  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignored) {

    }
  }

}
