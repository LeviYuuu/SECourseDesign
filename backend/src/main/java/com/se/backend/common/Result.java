package com.se.backend.common;
import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 1:成功, 0:失败
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 1; r.msg = "success"; r.data = data;
        return r;
    }
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 0; r.msg = msg;
        return r;
    }
}
