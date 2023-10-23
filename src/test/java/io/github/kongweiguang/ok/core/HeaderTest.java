package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class HeaderTest {

  @Test
  void test1() throws Exception {
    final Res res = Req.get()
        .url("http://localhost:8080/header")
        //contentype
        .contentType(ContentType.json)
        //charset
        .charset(StandardCharsets.UTF_8)
        //user-agent
        .ua("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 OPR/26.0.1656.60")
        //authorization
        .auth("auth qwe")
        //authorization bearer
        .bearer("qqq")
        //header
        .header("name", "value")
        //headers
        .headers(new HashMap<String, String>() {{
          put("name1", "value1");
          put("name2", "value2");
        }})
        //cookie
        .cookie("k", "v")
        //cookies
        .cookies(new HashMap<String, String>() {{
          put("k1", "v1");
          put("k2", "v2");
        }})
        .ok();
    System.out.println("res.str() = " + res.str());
  }

}
