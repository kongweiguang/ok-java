package io.github.kongweiguang.ok;

import com.sun.net.httpserver.Headers;
import io.github.kongweiguang.khttp.KHTTP;
import io.github.kongweiguang.khttp.core.MultiValueMap;
import io.github.kongweiguang.khttp.core.Req;
import io.github.kongweiguang.khttp.core.Res;
import io.github.kongweiguang.khttp.core.UpFile;
import io.github.kongweiguang.khttp.sse.SSEHandler;
import io.github.kongweiguang.khttp.sse.SseEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

public class ServerTest {


  public static void main(String[] args) {

    KHTTP.of()
        .executor(Executors.newCachedThreadPool())
        //设置静态web地址，默认寻找index.html
        .web("/Users/kongweiguang/Desktop/hegui/xm/gs")
        .get("/get", (req, res) -> {
          System.out.println("req = " + req.params());
          res.send("ok");
        })
        .get("/get_string", (req, res) -> {
          System.out.println("req = " + req.query());
          System.out.println("req = " + req.params());
          res.send("ok");
        })
        .post("/post_json", (req, res) -> {
          final MultiValueMap<String, String> params = req.params();
          System.out.println("params = " + params);

          System.out.println("req.str() = " + req.str());

          res.send("\"{\"key\":\"i am post res\"}\"");
        })
        .get("/get/one/two", (req, res) -> {
          System.out.println("req = " + req.path());
          res.send("ok");
        })
        .get("/header", (req, res) -> {
          final Headers headers = req.headers();
          System.out.println("headers = " + headers);
          res.send("ok");
        })
        //接受post请求
        .post("/post_body", ((req, res) -> {
          final String str = req.str();
          System.out.println("str = " + str);

          res.send("{\"key\":\"i am post res\"}");
        }))
        .post("/post_form", ((req, res) -> {
          System.out.println(req.params());
          res.send("ok");
        }))
        .get("/timeout", ((req, res) -> {
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          res.send("ok");
        }))
        .get("/error", ((req, res) -> {
          System.out.println("req.str() = " + req.str());
          res.write(500, "error_123456".getBytes());
        }))
        //上传
        .post("/post_mul_form", (req, res) -> {

          final MultiValueMap<String, String> params = req.params();
          System.out.println("params = " + params);
          final Map<String, List<UpFile>> files = req.fileMap();
          System.out.println("files = " + files);
          res.send("ok");
        })
        //下载文件
        .get("/xz", (req, res) ->
            res.file("k.txt", Files.readAllBytes(Paths.get("D:\\k\\k.txt"))))
        //sse响应
        .get("/sse", new SSEHandler() {
          @Override
          public void handler(final Req req, final Res res) {
            for (int i = 0; i < 3; i++) {
              try {
                Thread.sleep(300);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              send(res,
                  SseEvent.of()
                      .id(UUID.randomUUID().toString())
                      .type("eventType")
                      .data(new Date().toString())
              );
            }

            //完成
            send(res,
                SseEvent.of()
                    .id(UUID.randomUUID().toString())
                    .type("eventType")
                    .data("done")
            );

            //关闭
            close(res);
          }
        })
        .ok(8080);

  }
}
