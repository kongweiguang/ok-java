package cn.kpp.core;

import cn.kpp.core.util.EmptyIS;
import cn.kpp.core.util.TypeRef;
import com.alibaba.fastjson2.JSON;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public final class Res {
    private final Response res;
    private final byte[] b;
    private final InputStream is;

    private Res(final Response response) {

        if (Objects.isNull(response.body())) {
            this.is = EmptyIS.of;
            this.b = new byte[]{};
        } else {
            this.is = response.body().byteStream();
            try {
                this.b = response.body().bytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        return b;
    }

    public String str() {
        return str(StandardCharsets.UTF_8);
    }

    public String str(Charset charset) {
        return new String(b, charset);
    }

    public InputStream stream() {
        return this.is;
    }

    public <R> R obj(Class<R> clazz) {
        return JSON.parseObject(b, clazz);
    }

    public <R> R obj(Type type) {
        return JSON.parseObject(b, type);
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
        }.getType());
    }

    public <K, V> Map<K, V> map() {
        return obj(new TypeRef<Map<K, V>>() {
        }.getType());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Res.class.getSimpleName() + "[", "]")
                .add("res=" + res)
                .toString();
    }
}
