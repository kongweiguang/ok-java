package io.github.kongweiguang.ok;


import io.github.kongweiguang.ok.core.Method;
import io.github.kongweiguang.ok.core.ReqType;

/**
 * 基于okhttp封装的http请求工具
 *
 * @author kongweiguang
 */
public final class Req {

  //工厂方法
  public static ReqBuilder of() {
    return new ReqBuilder();
  }

  public static ReqBuilder of(final String url) {
    return of().url(url);
  }

  public static ReqBuilder get(final String url) {
    return of(url).method(Method.GET);
  }

  public static ReqBuilder post(final String url) {
    return of(url).method(Method.POST);
  }

  public static ReqBuilder delete(final String url) {
    return of(url).method(Method.DELETE);
  }

  public static ReqBuilder put(final String url) {
    return of(url).method(Method.PUT);
  }

  public static ReqBuilder patch(final String url) {
    return of(url).method(Method.PATCH);
  }

  public static ReqBuilder head(final String url) {
    return of(url).method(Method.HEAD);
  }

  public static ReqBuilder options(final String url) {
    return of(url).method(Method.OPTIONS);
  }

  public static ReqBuilder trace(final String url) {
    return of(url).method(Method.TRACE);
  }

  public static ReqBuilder connect(final String url) {
    return of(url).method(Method.CONNECT);
  }

  public static ReqBuilder formUrlencoded(final String url) {
    return of(url).formUrlencoded();
  }

  public static ReqBuilder multipart(final String url) {
    return of(url).multipart();
  }

  //ws
  public static ReqBuilder ws(final String url) {
    return of().reqType(ReqType.ws).url(url);
  }

  //sse
  public static ReqBuilder sse(final String url) {
    return of().reqType(ReqType.sse).url(url);
  }


}
