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

  private final Response raw;
  private byte[] bytes;

  private Res(final Response resp) {
    this.raw = resp;
  }

  public static Res of(final Response resp) {
    return new Res(resp);
  }

  public Response raw() {
    return raw;
  }

  public int code() {
    return raw().code();
  }

  public boolean isOk() {
    return raw().isSuccessful();
  }

  public String header(String name) {
    return raw().header(name);
  }

  public Map<String, List<String>> headers() {
    final Headers headers = raw().headers();

    final Map<String, List<String>> fr = new LinkedHashMap<>(headers.size(), 1);

    for (final Pair<? extends String, ? extends String> hd : headers) {
      fr.computeIfAbsent(hd.getFirst(), k -> new ArrayList<>()).add(hd.getSecond());
    }

    return fr;
  }

  public String contentType() {
    return raw().body().contentType().toString();
  }

  public Charset charset() {
    return raw().body().contentType().charset(StandardCharsets.UTF_8);
  }

  public String contentEncoding() {
    return header(Header.content_encoding.v());
  }

  public long contentLength() {
    return raw().body().contentLength();
  }

  public String cookieStr() {
    return header(Header.set_cookie.v());
  }

  public long useMillis() {
    return raw().receivedResponseAtMillis() - raw().sentRequestAtMillis();
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
    if (isNull(bytes)) {
      try {
        this.bytes = raw().body().bytes();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return bytes;
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

    return null;
  }

  public JSONArray jsonArray() {
    if (isJsonAry()) {
      return JSON.parseArray(str());
    }

    return null;
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
    raw().close();
    this.bytes = null;
  }

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
