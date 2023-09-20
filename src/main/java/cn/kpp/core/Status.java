package cn.kpp.core;

public interface Status {
    int ok = 200;
    int internal_error = 500;
    int bad_gateway = 502;
    int unavailable = 503;
}
