package com.fedex.aggregate_api.domain;

public class TrackingInfo {
    public TrackingInfo(String orderNumber, String status) {
        this.orderNumber = orderNumber;
        this.status = status;
    }
    public TrackingInfo(GenericInfo genericInfo) {
        this.orderNumber = genericInfo.code;
        this.status = (String) genericInfo.data;
    }
    public final String orderNumber;
    public final String status;
}
