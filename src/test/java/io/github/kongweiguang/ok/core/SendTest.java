package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.OK;
import org.junit.jupiter.api.Test;

public class SendTest {
    @Test
    void test() {
        final Res send = OK.of()
                .url("http://localhost:8000/hello")
                .post()
                .json("{\n" +
                        "    \"key\":\"i am key\"\n" +
                        "}")
                .ok();
        System.out.println("send = " + send.str());

    }
}
