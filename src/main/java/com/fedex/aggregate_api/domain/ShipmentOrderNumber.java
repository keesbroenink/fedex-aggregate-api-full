package com.fedex.aggregate_api.domain;

public record ShipmentOrderNumber(String orderNumber) implements Comparable{
    @Override
    public int compareTo(Object o) {
        return orderNumber.compareTo( ((ShipmentOrderNumber)o).orderNumber());
    }
}
