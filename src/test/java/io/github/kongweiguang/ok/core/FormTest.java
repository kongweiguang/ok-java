package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.OK;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class FormTest {
    @Test
    void testForm() {
        final Res ok = OK.of()
                .post()
                .form("a", "1")
                .form(new HashMap<String, String>() {{
                    put("b", "2");
                }})
                .ok();
    }
}
