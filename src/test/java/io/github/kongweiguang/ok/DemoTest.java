package io.github.kongweiguang.ok;

import com.alibaba.fastjson2.JSON;

public class DemoTest {

  public static void main(String[] args) {

    final Res ok = Req.post("https://open.feishu.cn/open-apis/bot/v2/hook/409771ec-d1cf-4dba-8c98-2b64eee8328f")
        .header("k", "v")
        .header("q", "wer")
        .json("{\"content\":{\"text\":\"123\"},\"msg_type\":\"text\"}")
        .ok();
    System.out.println(ok);
    System.out.println(123);
//    final HttpUrl parse = HttpUrl.parse("http://www.baidu.com");
//    System.out.println("parse = " + parse.host());
  }

}
