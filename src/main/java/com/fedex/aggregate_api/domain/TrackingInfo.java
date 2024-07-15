package com.fedex.aggregate_api.domain;

public class TrackingInfo {
    public TrackingInfo(String orderNumber, String status) {
        this.orderNumber = orderNumber;
        this.status = status;
    }
    public final String orderNumber;
    public final String status;
}
