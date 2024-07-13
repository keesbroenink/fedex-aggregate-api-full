package com.fedex.aggregate_api.domain;

public class GenericInfo {
    public GenericInfo(String code, Object data) {
        this.code = code;
        this.data = data;
    }
    public final String code;
    public final Object data;
}
