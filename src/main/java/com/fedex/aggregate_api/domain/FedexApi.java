package com.fedex.aggregate_api.domain;

import reactor.core.publisher.Mono;

import java.util.List;

public interface FedexApi {
    Mono<List<GenericInfo>> getPricing(List<String> iso2CountryCodes);

    Mono<List<GenericInfo>> getTrackStatus(List<String> orderNumbers);

    Mono<List<GenericInfo>> getShipments(List<String> orderNumbers);
}
