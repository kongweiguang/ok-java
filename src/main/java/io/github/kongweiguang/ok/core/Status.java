package io.github.kongweiguang.ok.core;


/**
 * http中相应涉及到的常量
 *
 * @author kongweiguang
 */
public interface Status {
    int ok = 200;
    int internal_error = 500;
    int bad_gateway = 502;
    int unavailable = 503;
}
