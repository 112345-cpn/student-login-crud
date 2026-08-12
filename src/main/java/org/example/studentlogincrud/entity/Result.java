package org.example.studentlogincrud.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;    // 状态码：200 成功，400 参数错误，404 没找到，500 服务器错误
    private String message;
    private T data;
    // 成功，带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }


    public static <T> Result<T> success() {
        return success(null);
    }


    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
