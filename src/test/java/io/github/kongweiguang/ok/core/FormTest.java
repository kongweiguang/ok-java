package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class FormTest {

  @Test
  void testForm() throws IOException {
    //application/x-www-form-urlencoded
    final Res ok = Req.formUrlencoded()
        .url("http://localhost:80/post_form")
        .form("a", "1")
        .form(new HashMap<String, String>() {{
          put("b", "2");
        }})
        .ok();
  }

  @Test
  void test2() throws Exception {
    //multipart/form-data
    final Res ok = Req.multipart()
        .url("http://localhost:80/post_form")
        .file("k", "k.txt", Files.readAllBytes(Paths.get("")))
        .form("a", "1")
        .form(new HashMap<String, String>() {{
          put("b", "2");
        }})
        .ok();
  }
}
