package com.yinyu.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String status;

    private Integer code;

    private String info;

    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", 200, "request success", data);
    }

    public static <T> ApiResponse<T> success(String info, T data) {
        return new ApiResponse<>("success", 200, info, data);
    }

    public static <T> ApiResponse<T> fail(Integer code, String info) {
        return new ApiResponse<>("fail", code, info, null);
    }
}
