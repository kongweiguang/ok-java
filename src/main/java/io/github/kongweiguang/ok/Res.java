package io.github.kongweiguang.ok;

import static java.util.Objects.isNull;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.kongweiguang.ok.core.Header;
import io.github.kongweiguang.ok.core.TypeRef;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * http的响应
 *
 * @author kongweiguang
 */
public final class Res implements AutoCloseable {

  //原始res对象
  private final Response raw;
  //res的body
  private byte[] bytes;

  private Res(final Response resp) {
    this.raw = resp;
  }

  /**
   * 工厂方法
   *
   * @param resp 原始响应对象
   * @return Res对象 {@link Res}
   */
  public static Res of(final Response resp) {
    return new Res(resp);
  }

  /**
   * 获得响应的原始对象
   *
   * @return 原始对象 {@link Response}
   */
  public Response raw() {
    return raw;
  }

  /**
   * 响应code
   *
   * @return code
   */
  public int code() {
    return raw().code();
  }

  /**
   * 请求是否成功
   *
   * @return 是否成功
   */
  public boolean isOk() {
    return raw().isSuccessful();
  }

  /**
   * 根据名称获得响应头
   *
   * @param name 响应头名称
   * @return 响应头值
   */
  public String header(String name) {
    return raw().header(name);
  }

  /**
   * 获得响应头
   *
   * @return 响应头集合
   */
  public Map<String, List<String>> headers() {
    final Headers headers = raw().headers();

    final Map<String, List<String>> fr = new LinkedHashMap<>(headers.size(), 1);

    for (final Pair<? extends String, ? extends String> hd : headers) {
      fr.computeIfAbsent(hd.getFirst(), k -> new ArrayList<>()).add(hd.getSecond());
    }

    return fr;
  }

  /**
   * 获得响应的contentType
   *
   * @return contentType
   */
  public String contentType() {
    return raw().body().contentType().toString();
  }

  /**
   * 获得响应的charset
   *
   * @return charset
   */
  public Charset charset() {
    return raw().body().contentType().charset(StandardCharsets.UTF_8);
  }

  /**
   * 获得响应的contentEncoding
   *
   * @return contentEncoding
   */
  public String contentEncoding() {
    return header(Header.content_encoding.v());
  }

  /**
   * 获得响应的contentLength
   *
   * @return contentLength
   */
  public long contentLength() {
    return raw().body().contentLength();
  }

  /**
   * 获得响应的cookie
   *
   * @return cookie
   */
  public String cookieStr() {
    return header(Header.set_cookie.v());
  }

  /**
   * 获得请求响应的耗时，单位ms
   *
   * @return useMillis
   */
  public long useMillis() {
    return raw().receivedResponseAtMillis() - raw().sentRequestAtMillis();
  }

  /**
   * 判断是否是json对象
   *
   * @return 是否是json对象
   */
  public boolean isJsonObj() {
    return JSON.isValidObject(bytes());
  }

  /**
   * 判断是否是json数组
   *
   * @return 是否是json数组
   */
  public boolean isJsonAry() {
    return JSON.isValidArray(bytes());
  }

  /**
   * 判断是否是json
   *
   * @return 是否是json
   */
  public boolean isJson() {
    return JSON.isValid(bytes());
  }

  /**
   * 获得响应的body，byte
   *
   * @return body
   */
  public byte[] bytes() {
    if (isNull(bytes)) {
      try {
        this.bytes = raw().body().bytes();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return bytes;
  }

  /**
   * 获得响应的body，String
   *
   * @return body
   */
  public String str() {
    return str(StandardCharsets.UTF_8);
  }

  /**
   * 获得响应的body，String
   *
   * @param charset charset
   * @return body
   */
  public String str(final Charset charset) {
    return new String(bytes(), charset);
  }

  /**
   * 获得响应的body，InputStream
   *
   * @return body
   */
  public InputStream stream() {
    return new ByteArrayInputStream(bytes());
  }

  /**
   * 获得响应的body，JSONObject
   *
   * @return body
   */
  public JSONObject jsonObj() {
    if (isJsonObj()) {
      return JSON.parseObject(str());
    }

    return null;
  }

  /**
   * 获得响应的body，JSONArray
   *
   * @return body
   */
  public JSONArray jsonArray() {
    if (isJsonAry()) {
      return JSON.parseArray(str());
    }

    return null;
  }

  /**
   * 获得响应对象，根据据类型转换
   *
   * @param clazz 目标类型class
   * @param <R>   目标类型
   * @return 响应对象
   */
  public <R> R obj(Class<R> clazz) {
    if (isJson()) {
      return JSON.parseObject(bytes(), clazz);
    }

    return null;
  }

  /**
   * 获得响应对象，根据据类型转换
   *
   * @param type 类型
   * @param <R>  目标类型
   * @return 响应对象
   */
  public <R> R obj(Type type) {
    if (isJson()) {
      return JSON.parseObject(bytes(), type);
    }

    return null;
  }

  /**
   * 获得int类型结果
   *
   * @return 响应对象
   */
  public Integer rInt() {
    return obj(Integer.class);
  }

  /**
   * 获得long类型结果
   *
   * @return 响应对象
   */
  public Long rLong() {
    return obj(Long.class);
  }

  /**
   * 获得bool类型响应对象
   *
   * @return 响应对象
   */
  public Boolean rBool() {
    return obj(Boolean.class);
  }

  /**
   * 返回list类型结果
   *
   * @param <E>
   * @return 响应对象
   */
  public <E> List<E> list() {
    return obj(new TypeRef<List<E>>() {
    }.type());
  }

  /**
   * 返回map类型结果
   *
   * @param <K> Map的key
   * @param <V> Map的Value
   * @return 响应对象
   */
  public <K, V> Map<K, V> map() {
    return obj(new TypeRef<Map<K, V>>() {
    }.type());
  }

  /**
   * 保存文件
   *
   * @param path    文件路径
   * @param options 文件选项
   * @throws IOException IOException
   */
  public void file(String path, OpenOption... options) throws IOException {
    Files.write(Paths.get(path), bytes(), options);
  }

  /**
   * 关闭res对象
   */
  @Override
  public void close() {
    raw().close();
    this.bytes = null;
  }

  /**
   * 打印res对象信息
   *
   * @return 请求响应信息
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder()
        .append("---ok-req---").append('\n')
        .append("method: ").append(raw().request().method()).append(' ')
        .append(raw().request().url()).append('\n')
        .append("headers: ").append('\n')
        .append(raw().request().headers()).append('\n')
        .append("body: ").append('\n');
    Object body = raw().request().tag(Req.class).strBody();

    if (isNull(body)) {
      body = raw().request().body();
    }

    return sb.append(body).append("\n\n")
        .append("---ok-res---").append('\n')
        .append("headers: ").append('\n')
        .append(raw().headers()).append('\n')
        .append("body: ").append('\n')
        .append(str()).append('\n')
        .append("------------").append('\n').toString();
  }

}
