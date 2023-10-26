package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.Req;
import io.github.kongweiguang.ok.Res;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class UrlQueryTest {

  @Test
  void test1() throws Exception {
    //http://localhost:8080/get/one/two?k1=v1&k2=1&k2=2&k3=v3&k4=v4
    final Res res = Req.get("http://localhost:8080/get/one/two")
        .query("k1", "v1")
        .query("k2", Arrays.asList("1", "2"))
        .query(new HashMap<String, String>() {{
          put("k3", "v3");
          put("k4", "v4");
        }})
        .ok();
  }

}
