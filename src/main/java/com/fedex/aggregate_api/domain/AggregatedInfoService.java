package com.fedex.aggregate_api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
public class AggregatedInfoService {
    private final Logger logger = LoggerFactory.getLogger(AggregatedInfoService.class);
    private final FedexApi fedexApi;
    private final int minimalRequests;

    private final LinkedBlockingQueue<String> pricingIso2CountryCodesCache = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<String> trackOrderNumbersCache = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<String> shipmentsOrderNumbersCache = new LinkedBlockingQueue<>();

    public AggregatedInfoService(FedexApi fedexApi,
                                 @Value("${fedexapi.service.minimal.requests}")int minimalRequests) {
        this.fedexApi = fedexApi;
        this.minimalRequests = minimalRequests;
    }
    // note that the input AggregatedInfo will not be changed by this method!
    private AggregatedInfo getInfoInternal(AggregatedInfo requestedInfo, int minimalRequests) {
        logger.info("getInfo() pricingIso2CountryCodes {}, trackOrderNumbers {}, shipmentsOrderNumbers {}",
                requestedInfo.pricingIso2CountryCodes,
                requestedInfo.trackOrderNumbers,
                requestedInfo.shipmentsOrderNumbers);

        Mono<List<GenericInfo>> pricing = Mono.just(emptyList());
        pricingIso2CountryCodesCache.addAll(requestedInfo.pricingIso2CountryCodes);
        if (pricingIso2CountryCodesCache.size() >= minimalRequests) {
            List<String> isoCountryCodes = new ArrayList<>();
            pricingIso2CountryCodesCache.drainTo(isoCountryCodes, minimalRequests);
            pricing = fedexApi.getPricing(isoCountryCodes);
        }
        Mono<List<GenericInfo>> trackStatus = Mono.just(emptyList());
        trackOrderNumbersCache.addAll(requestedInfo.trackOrderNumbers);
        if (trackOrderNumbersCache.size() >= minimalRequests) {
            List<String> orderNumbers = new ArrayList<>();
            trackOrderNumbersCache.drainTo(orderNumbers, minimalRequests);
            trackStatus = fedexApi.getTrackStatus(orderNumbers);
        }
        Mono<List<GenericInfo>> shipments = Mono.just(emptyList());
        shipmentsOrderNumbersCache.addAll(requestedInfo.shipmentsOrderNumbers);
        if (shipmentsOrderNumbersCache.size() >= minimalRequests) {
            List<String> orderNumbers = new ArrayList<>();
            shipmentsOrderNumbersCache.drainTo(orderNumbers, minimalRequests);
            shipments = fedexApi.getShipments(orderNumbers);
        }
        // set up a new AggregatedInfo as output of this function
        AggregatedInfo result = new AggregatedInfo(requestedInfo);

        // call in parallel
        return Mono
                .zip( pricing, trackStatus, shipments)
                .map( data -> {
                    result.addPricing(data.getT1().stream().map(PricingInfo::new).collect(Collectors.toList()));
                    result.addTracking(data.getT2().stream().map(TrackingInfo::new).collect(Collectors.toList()));
                    result.addShipments(data.getT3().stream().map(ShipmentInfo::new).collect(Collectors.toList()));
                    return result;
                })
                .block();
    }

    public AggregatedInfo getInfoNoLimit(AggregatedInfo requestedInfo) {
        return getInfoInternal(requestedInfo, 1);
    }

    public AggregatedInfo getInfo(AggregatedInfo requestedInfo) {
        return getInfoInternal(requestedInfo, this.minimalRequests);
    }
}
