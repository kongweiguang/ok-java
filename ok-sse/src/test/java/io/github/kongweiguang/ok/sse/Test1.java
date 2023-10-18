//package io.github.kongweiguang.ok.sse;
//
//import io.github.kongweiguang.khttp.KHTTP;
//import io.github.kongweiguang.ok.core.Method;
//import io.github.kongweiguang.ok.sse.core.EventMessage;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.Date;
//import java.util.concurrent.atomic.AtomicReference;
//
//public class Test1 {
//
//    @BeforeEach
//    void before() throws IOException {
//        KHTTP.of()
//                .addAction("/sse", new SSEAction() {
//                    @Override
//                    public void action(final HttpServerRequest request, final HttpServerResponse response) {
//                        for (int i = 0; i < 3; i++) {
//                            ThreadUtil.sleep(300);
//
//                            send(response, new EventMessage(IdUtil.fastUUID(), "eventType", new Date().toString()));
//                        }
//
//                        //完成
//                        send(response, new EventMessage(IdUtil.fastUUID(), "eventType", "done"));
//
//                        //关闭
//                        close(response);
//                    }
//
//                })
//                .ok(8080);
//    }
//
//    @Test
//    void test() {
//        final Request req = Request.of("http://localhost:8080/sse").method(Method.GET);
//
//        final AtomicReference<String> end = new AtomicReference<>("");
//
//        final SSE sse = SSE.of(req)
//                .onOpen((r, w) -> {
//                    Console.log("sse -> {}", "open");
//                })
//                .onEvent(event -> {
//                    Console.log("sse -> {}", event.toString());
//                    end.set(event.data());
//                })
//                .onClosed(r -> {
//                    Console.log("sse -> {}", "close");
//                })
//                .ok();
//
//        while (!StrUtil.equals(end.get(), "done")) {
//        }
//
//        sse.close();
//
//    }
//}
