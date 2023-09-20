package cn.kpp.core;

import org.junit.jupiter.api.Test;

public class SendTest {
    @Test
    void test() {
        final Res send = OK.of()
                .url("http://localhost:8000/hello")
                .post()
                .jsonBody("{\n" +
                        "    \"key\":\"i am key\"\n" +
                        "}")
                .ok();
        System.out.println("send = " + send.str());

    }
}
