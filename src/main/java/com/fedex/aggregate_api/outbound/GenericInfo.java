package com.fedex.aggregate_api.outbound;

public class GenericInfo {
    public GenericInfo(String code, Object data) {
        this.code = code;
        this.data = data;
    }
    public final String code;
    public final Object data;
}
