package io.github.kongweiguang.ok;

import io.github.kongweiguang.khttp.KHTTP;
import io.github.kongweiguang.khttp.core.MultiValueMap;

public class ServerTest {

  public static void main(String[] args) {
    KHTTP.of()
        .get("/get_query", (req, res) -> {
          final MultiValueMap<String, String> params = req.params();
          System.out.println("params = " + params);
          res.send("ok");
        })

        .post("/post_query", (req, res) -> {
          final MultiValueMap<String, String> params = req.params();
          System.out.println("params = " + params);
          res.send("ok");
        })
        .post("/post_query/a/b/c", (req, res) -> {
          final MultiValueMap<String, String> params = req.params();
          System.out.println("params = " + params);
          res.send("ok");
        })
        .ok(8080);
  }
}
