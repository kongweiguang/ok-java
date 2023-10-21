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
    final Res ok = Req.post()
        .url("http://localhost:80/post_form")
        //application/x-www-form-urlencoded
        .form("a", "1")
        .form(new HashMap<String, String>() {{
          put("b", "2");
        }})
        //multipart/form-data
        .file("k", "k.txt", Files.readAllBytes(Paths.get("")))
        .ok();
  }
}
