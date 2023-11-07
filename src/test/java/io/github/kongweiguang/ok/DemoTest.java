package io.github.kongweiguang.ok;

import io.github.kongweiguang.ok.core.UA.Win;

public class DemoTest {

    public static void main(String[] args) {
//        Config.ssl(true);
        final Res ok = Req.post("https://open.feishu.cn/open-apis/bot/v2/hook/409771ec-d1cf-4dba-8c98-2b64eee8328f")
                .header("k", "v")
                .header("q", "wer")
                .query("q1", "v1")
                .query("q2", "我是vvv")
                .fragment("123")
                .ua(Win.chrome.v())
                .json("{\"content\":{\"text\":\"123\"},\"msg_type\":\"text\"}")
                .ok();
        System.out.println(ok);
        System.out.println(123);
//    final HttpUrl parse = HttpUrl.parse("http://www.baidu.com");
//    System.out.println("parse = " + parse.host());
    }

}
