package io.github.kongweiguang.ok.core;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * http请求体
 *
 * @author kongweiguang
 */
public final class ReqBody extends RequestBody {
    private final MediaType mt;
    private final Charset cs;
    private final byte[] bt;

    public ReqBody(String contentType, Charset charset, byte[] bytes) {
        this.cs = charset;
        this.mt = MediaType.parse(contentType);
        this.bt = bytes;
    }

    @Override
    public MediaType contentType() {
        this.mt.charset(this.cs);
        return this.mt;
    }

    @Override
    public long contentLength() throws IOException {
        return this.bt.length;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        sink.write(this.bt, 0, this.bt.length);
    }
}
