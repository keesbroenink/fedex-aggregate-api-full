package com.fedex.aggregate_api.domain;

import java.util.List;

public record TrackingInfo(TrackingOrderNumber trackingOrderNumber, String status) {
    public static List<TrackingOrderNumber> fromListString(List<String> listString) {
        return listString.stream().map(TrackingOrderNumber::new).toList();
    }
}
