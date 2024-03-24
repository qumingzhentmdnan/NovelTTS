package com.fjut.noveltts.utils;

import java.util.HashMap;
import java.util.Map;



public class Result {
    private Boolean success;

    private Integer code;

    private String message;

    public static final Integer SUCCESS = 20000;

    public static final Integer ERROR = 20001;

    public static final Integer NOLOGIN = 20002;

    private Map<String, Object> data = new HashMap<String, Object>();

    private Result(){}//构造器私有化，使外界无法调用

    public static Result ok(){
        Result r = new Result();
        r.setSuccess(true);
        r.setCode(SUCCESS);
        r.setMessage("成功");
        return r;
    }

    public static Result error(){
        Result r = new Result();
        r.setSuccess(false);
        r.setCode(ERROR);
        r.setMessage("失败");
        return r;
    }

    public Result success(Boolean success){
        this.setSuccess(success);
        return this;
    }

    public Result message(String message){
        this.setMessage(message);
        return this;
    }

    public Result code(Integer code){
        this.setCode(code);
        return this;
    }

    public Result data(String key, Object value){
        this.data.put(key, value);
        return this;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Result data(Map<String, Object> map){
        this.setData(map);
        return this;
    }
}