//package io.github.kongweiguang.ok.sse.core;
//
//import okhttp3.sse.EventSources;
//import org.dromara.hutool.http.client.Request;
//import org.dromara.hutool.http.client.engine.okhttp.OkHttpEngine;
//
///**
// * sse请求发送引擎
// *
// * @author kongweiguang
// * @since 6.0.0
// */
//public final class OkHttpSSEEngine extends OkHttpEngine {
//
//	private final Request request;
//
//	/**
//	 * 构造sse发送的引擎
//	 *
//	 * @param request 请求参数
//	 */
//
//	public OkHttpSSEEngine(final Request request) {
//		this.request = request;
//	}
//
//	/**
//	 * 构造sse发送的引擎
//	 *
//	 * @param request 请求参数
//	 * @return this
//	 */
//	public static OkHttpSSEEngine of(Request request) {
//		return new OkHttpSSEEngine(request);
//	}
//
//	/**
//	 * 监听sse结果返回
//	 *
//	 * @param listener 监听器
//	 */
//	public void listen(OkHttpSseListener listener) {
//		initEngine();
//
//		listener.client = client;
//
//		EventSources.createFactory(client).newEventSource(buildRequest(request), listener);
//	}
//
//
//}
