package com.example.entity;

import lombok.Data;

@Data
public class RestBean<T> {
    private String status;
    private T message;
    private boolean success;

    private RestBean(String status, T message, boolean success) {
        this.status = status;
        this.message = message;
        this.success = success;
    }


    public  static <T> RestBean<T> success(){
        return new RestBean<>("200",null,true);
    }
    public  static <T> RestBean<T> success(T data){
        return new RestBean<>("200",data,true);
    }
    public  static <T> RestBean<T> failure(){
        return new RestBean<>("666",null,false);
    }
    public  static <T> RestBean<T> failure(T data){
        return new RestBean<>("666",data,false);
    }
}
