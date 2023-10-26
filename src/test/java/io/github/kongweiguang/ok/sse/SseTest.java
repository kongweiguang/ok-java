package io.github.kongweiguang.ok.sse;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.util.Objects;
import okhttp3.Request;
import okhttp3.Request.Builder;
import org.junit.jupiter.api.Test;

public class SseTest {


  @Test
  void test() throws InterruptedException {

    Req.sse("localhost:8080/sse")
        .sseListener(new SSEListener() {
          @Override
          public void event(Req req, SseEvent msg) {
            System.out.println("sse -> " + msg.id());
            System.out.println("sse -> " + msg.type());
            System.out.println("sse -> " + msg.data());
            if (Objects.equals(msg.data(), "done")) {
              close();
            }
          }

          @Override
          public void open(final Req req, final Res res) {
            super.open(req, res);
          }

          @Override
          public void fail(final Req req, final Res res, final Throwable t) {
            super.fail(req, res, t);
          }

          @Override
          public void closed(final Req req) {
            super.closed(req);
          }
        })
        .ok();

    wait();
  }

  public static void main(String[] args) {
    final Builder builder = new Builder();
    builder.url("http://www.baidu.com");
    builder.addHeader("k","v");
    final Request build = builder.build();
    System.out.println(build.headers());
    builder.addHeader("v","vvv");
    final Request build1 = builder.build();
    System.out.println("build1.headers() = " + build1.headers());
  }
}
