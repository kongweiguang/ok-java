package io.github.kongweiguang.ok.core;

import io.github.kongweiguang.ok.OK;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DowTest {
    @Test
    void testDow() {
        final Res ok = OK.of()
                .get()
                .url("http://localhost:80/get_file")
                .ok();

        ok.file("d:\\k.txt");
    }

    @Test
    void testUp() throws IOException {
        final Res ok = OK.of()
                .post()
                .url("http://localhost:80/post_upload_file")
                .multipart()
                .file("introduce", "introduce.txt", Files.readAllBytes(Paths.get("d:\\k.txt")))
                .form("a", "b")
                .ok();
    }
}
