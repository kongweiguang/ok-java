package io.github.kongweiguang.ok.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.github.kongweiguang.ok.util.TypeRef;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
import java.util.StringJoiner;

import static java.util.Objects.isNull;

/**
 * http的响应
 *
 * @author kongweiguang
 */
public final class Res {
    private final Response res;
    private final byte[] bt;

    private Res(final Response response) {

        try {
            final ResponseBody body = response.body();

            if (isNull(body)) {
                this.bt = new byte[]{};
            } else {
                this.bt = body.bytes();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.res = response;
    }

    public static Res of(final Response response) {
        return new Res(response);
    }

    public Response res() {
        return res;
    }

    public int status() {
        return this.res.code();
    }

    public String header(String name) {
        return this.res.header(name);
    }

    public Map<String, List<String>> headers() {
        final Headers headers = this.res.headers();
        final Map<String, List<String>> fr = new LinkedHashMap<>(headers.size(), 1);
        for (final Pair<? extends String, ? extends String> header : headers) {
            final List<String> l = fr.computeIfAbsent(header.getFirst(), k -> new ArrayList<>());
            l.add(header.getSecond());
        }
        return fr;

    }

    public byte[] bytes() {
        return bt;
    }

    public String str() {
        return str(StandardCharsets.UTF_8);
    }

    public String str(Charset charset) {
        return new String(bytes(), charset);
    }

    public JSONObject jsonObj() {
        return JSON.parseObject(str());
    }

    public InputStream stream() {
        return new ByteArrayInputStream(bytes());
    }

    public <R> R obj(Class<R> clazz) {
        return JSON.parseObject(bytes(), clazz);
    }

    public <R> R obj(Type type) {
        return JSON.parseObject(bytes(), type);
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
    public String toString() {
        return new StringJoiner(", ", Res.class.getSimpleName() + "[", "]")
                .add("res=" + res)
                .toString();
    }
}
