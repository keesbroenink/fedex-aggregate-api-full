package com.fedex.aggregate_api.domain;

import java.util.List;

public record ShipmentInfo(ShipmentOrderNumber shipmentOrderNumber, List<String> shipments) {
    public static List<ShipmentOrderNumber> fromListString(List<String> listString) {
        return listString.stream().map(s -> new ShipmentOrderNumber(s)).toList();
    }

}
