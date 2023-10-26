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

  private final Response res;
  private byte[] bt;

  private Res(final Response response) {
    this.res = response;
  }

  public static Res of(final Response response) {
    return new Res(response);
  }

  public Response res() {
    return res;
  }

  public int code() {
    return res().code();
  }

  public boolean isOk() {
    return res().isSuccessful();
  }

  public String header(String name) {
    return res().header(name);
  }

  public Map<String, List<String>> headers() {
    final Headers headers = res().headers();

    final Map<String, List<String>> fr = new LinkedHashMap<>(headers.size(), 1);

    for (final Pair<? extends String, ? extends String> hd : headers) {
      fr.computeIfAbsent(hd.getFirst(), k -> new ArrayList<>()).add(hd.getSecond());
    }

    return fr;
  }

  public String contentType() {
    return res().body().contentType().toString();
  }

  public Charset charset() {
    return res().body().contentType().charset(StandardCharsets.UTF_8);
  }

  public String contentEncoding() {
    return header(Header.content_encoding.v());
  }

  public long contentLength() {
    return res().body().contentLength();
  }

  public String cookieStr() {
    return header(Header.set_cookie.v());
  }

  public long useMillis() {
    return res().receivedResponseAtMillis() - res().sentRequestAtMillis();
  }

  public boolean isJsonObj() {
    return JSON.isValidObject(bytes());
  }

  public boolean isJsonAry() {
    return JSON.isValidArray(bytes());
  }

  public boolean isJson() {
    return JSON.isValid(bytes());
  }

  public byte[] bytes() {
    if (isNull(bt)) {
      try {
        this.bt = res().body().bytes();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return bt;
  }

  public String str() {
    return str(StandardCharsets.UTF_8);
  }

  public String str(final Charset charset) {
    return new String(bytes(), charset);
  }

  public InputStream stream() {
    return new ByteArrayInputStream(bytes());
  }

  public JSONObject jsonObj() {
    if (isJsonObj()) {
      return JSON.parseObject(str());
    }

    return JSONObject.of();
  }

  public JSONArray jsonArray() {
    if (isJsonAry()) {
      return JSON.parseArray(str());
    }

    return JSONArray.of();
  }


  public <R> R obj(Class<R> clazz) {
    if (isJson()) {
      return JSON.parseObject(bytes(), clazz);
    }

    return null;
  }

  public <R> R obj(Type type) {
    if (isJson()) {
      return JSON.parseObject(bytes(), type);
    }
    return null;
  }

  public Integer rInt() {
    return obj(Integer.class);
  }

  public Long rLong() {
    return obj(Long.class);
  }

  public Boolean rBool() {
    return obj(Boolean.class);
  }

  public <E> List<E> list() {
    return obj(new TypeRef<List<E>>() {
    }.type());
  }

  public <K, V> Map<K, V> map() {
    return obj(new TypeRef<Map<K, V>>() {
    }.type());
  }

  public void file(String path, OpenOption... options) throws IOException {
    Files.write(Paths.get(path), bytes(), options);
  }

  @Override
  public void close() {
    res().close();
    this.bt = null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder()
        .append("---ok-req---").append('\n')
        .append("method: ").append(res().request().method()).append(' ')
        .append(res().request().url()).append('\n')
        .append("headers: ").append('\n')
        .append(res().request().headers()).append('\n')
        .append("body: ").append('\n');
    Object body = res().request().tag(Req.class).strBody();

    if (isNull(body)) {
      body = res().request().body();
    }

    return sb.append(body).append("\n\n")
        .append("---ok-res---").append('\n')
        .append("headers: ").append('\n')
        .append(res().headers()).append('\n')
        .append("body: ").append('\n')
        .append(str()).append('\n')
        .append("------------").append('\n').toString();
  }

}
