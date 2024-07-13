package com.fedex.aggregate_api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AggregatedInfoService {
    private final Logger logger = LoggerFactory.getLogger(AggregatedInfoService.class);
    private final FedexApi fedexApi;

    public AggregatedInfoService(FedexApi fedexApi) {
        this.fedexApi = fedexApi;
    }
    // note that the input AggregatedInfo will not be changed by this method!
    public Mono<AggregatedInfo> getInfo(AggregatedInfo requestedInfo) {
        logger.info("getInfo() pricingIso2CountryCodes {}, trackOrderNumbers {}, shipmentsOrderNumbers {}",
                requestedInfo.pricingIso2CountryCodes, requestedInfo.trackOrderNumbers, requestedInfo.shipmentsOrderNumbers);

        Mono<List<GenericInfo>> pricing = fedexApi.getPricing(requestedInfo.pricingIso2CountryCodes);
        Mono<List<GenericInfo>> trackStatus = fedexApi.getTrackStatus(requestedInfo.trackOrderNumbers);
        Mono<List<GenericInfo>> shipments = fedexApi.getShipments(requestedInfo.shipmentsOrderNumbers);

        // setup a new AggregatedInfo as output of this function
        AggregatedInfo result = new AggregatedInfo(requestedInfo);

        // call in parallel
        Mono<AggregatedInfo> answer = Mono.zip( pricing, trackStatus, shipments).map( data -> {
            result.addPricing(data.getT1().stream().map(PricingInfo::new).collect(Collectors.toList()));
            result.addTracking(data.getT2().stream().map(TrackingInfo::new).collect(Collectors.toList()));
            result.addShipments(data.getT3().stream().map(ShipmentInfo::new).collect(Collectors.toList()));
            return result;
        });
        return answer;
    }


}
