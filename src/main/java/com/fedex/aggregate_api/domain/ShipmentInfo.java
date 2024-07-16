package com.fedex.aggregate_api.domain;

import java.util.List;

public record ShipmentInfo(String orderNumber, List<String> shipments) {
}
