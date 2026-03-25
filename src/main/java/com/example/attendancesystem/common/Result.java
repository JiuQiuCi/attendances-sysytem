package com.example.attendancesystem.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result<T> {
    //统一响应类
    private Integer code;
    private String message;
    private T data;

    private Result(Integer code,String message,T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(){
        return new Result<>(200,"操作成功",null);
    }

    public static <T> Result<T> success(T data){
        return new Result<>(200,"操作成功",data);
    }

    public static <T> Result<T> success(String message, T data){
        return new Result<>(200,message,data);
    }

    public static <T> Result<T> error(Integer code, String message){
        return new Result<>(code,message,null);
    }

    public static  <T> Result<T> error(String message){
        return new Result<>(500,message,null);
    }

}
