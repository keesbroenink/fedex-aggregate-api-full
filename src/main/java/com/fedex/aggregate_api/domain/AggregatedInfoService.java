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
    public Mono<AggregatedInfo> getInfo(List<String> pricingIso2CountryCodes,
                                        List<String> trackOrderNumbers,
                                        List<String> shipmentsOrderNumbers) {
        logger.info("getInfo() pricingIso2CountryCodes {}, trackOrderNumbers {}, shipmentsOrderNumbers {}",
                pricingIso2CountryCodes, trackOrderNumbers, shipmentsOrderNumbers);

        Mono<List<GenericInfo>> pricing = fedexApi.getPricing(pricingIso2CountryCodes);
        Mono<List<GenericInfo>> trackStatus = fedexApi.getTrackStatus(trackOrderNumbers);
        Mono<List<GenericInfo>> shipments = fedexApi.getShipments(shipmentsOrderNumbers);

        // call in parallel
        Mono<AggregatedInfo> answer = Mono.zip( pricing, trackStatus, shipments).map( data -> {
            AggregatedInfo result = new AggregatedInfo();
            result.addPricing(data.getT1().stream().map(PricingInfo::new).collect(Collectors.toList()));
            result.addTracking(data.getT2().stream().map(TrackingInfo::new).collect(Collectors.toList()));
            result.addShipments(data.getT3().stream().map(ShipmentInfo::new).collect(Collectors.toList()));
            return result;
        });
        return answer;
    }


}
