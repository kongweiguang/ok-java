package io.github.kongweiguang.ok.sse.core;

import okhttp3.OkHttpClient;
import okhttp3.sse.EventSourceListener;

/**
 * sse请求监听
 *
 * @author kongweiguang
 * @since 6.0.0
 */
public abstract class OkHttpSseListener extends EventSourceListener {

	public OkHttpClient client;

}
