package com.fedex.aggregate_api.domain;

import java.util.List;

public class ShipmentInfo {
    public ShipmentInfo(String orderNumber, List<String> shipments) {
        this.orderNumber = orderNumber;
        this.shipments = shipments;
    }
    public ShipmentInfo(GenericInfo genericInfo) {
        this.orderNumber = genericInfo.code;
        this.shipments = (List<String>) genericInfo.data;
    }
    public final String orderNumber;
    public final List<String> shipments;
}
