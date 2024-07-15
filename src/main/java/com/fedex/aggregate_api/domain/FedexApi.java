package com.fedex.aggregate_api.domain;

import reactor.core.publisher.Mono;

import java.util.List;

public interface FedexApi {
    Mono<List<PricingInfo>> getPricing(List<String> iso2CountryCodes);

    Mono<List<TrackingInfo>> getTrackingStatus(List<String> orderNumbers);

    Mono<List<ShipmentInfo>> getShipments(List<String> orderNumbers);
}
