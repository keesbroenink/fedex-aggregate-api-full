package com.fedex.aggregate_api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.math.NumberUtils.max;

/**
 * This service will call out to three different FedEx REST API's in parallel and combine the data.
 * Note that we only call-out when the specified <code>minimalRequests</code> is reached. If not we will cache
 * the request id's until we reach the minimal number.
 */
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
        logger.info("getInfo() minimalRequests {}, pricingIso2CountryCodes {}, trackOrderNumbers {}, shipmentsOrderNumbers {}",
                minimalRequests,
                requestedInfo.pricingIso2CountryCodes,
                requestedInfo.trackOrderNumbers,
                requestedInfo.shipmentsOrderNumbers);

        Mono<List<PricingInfo>> pricing = callOrCache(
                pricingIso2CountryCodesCache, minimalRequests,
                requestedInfo.pricingIso2CountryCodes,
                fedexApi::getPricing);
        Mono<List<TrackingInfo>> trackStatus = callOrCache(
                trackOrderNumbersCache, minimalRequests,
                requestedInfo.trackOrderNumbers,
                fedexApi::getTrackingStatus);
        Mono<List<ShipmentInfo>> shipments = callOrCache(
                shipmentsOrderNumbersCache, minimalRequests,
                requestedInfo.shipmentsOrderNumbers,
                fedexApi::getShipments);

        // set up a new AggregatedInfo as output of this function
        AggregatedInfo result = new AggregatedInfo(requestedInfo);

        // call in parallel
        return Mono
                .zip( pricing, trackStatus, shipments)
                .map( data -> {
                    result.addPricing(data.getT1());
                    result.addTracking(data.getT2());
                    result.addShipments(data.getT3());
                    return result;
                })
                .block();
    }
    // when we don't have the minimal number of requests to call-out, we will cache them and
    // return a mono empty list
    private <T> Mono<List<T>> callOrCache(BlockingQueue<String> cache,
                                          final int minimalRequests,
                                          List<String> keys,
                                          Function<List<String>, Mono<List<T>>> theCall) {
        Mono<List<T>> result = Mono.just(emptyList());
        cache.addAll(keys);
        if (cache.size() >= minimalRequests) {
            List<String> minimalKeys = new ArrayList<>();
            if (minimalRequests <= 0) {//timeout scenario
                cache.drainTo(minimalKeys, cache.size());//TODO we could request more than the maximum now
            } else {
                cache.drainTo(minimalKeys, minimalRequests);
            }
            result = theCall.apply(minimalKeys);
        }
        return result;
    }
    public AggregatedInfo getInfoNoLimit(AggregatedInfo requestedInfo) {
        return getInfoInternal(requestedInfo, 0);
    }

    public AggregatedInfo getInfo(AggregatedInfo requestedInfo) {
        return getInfoInternal(requestedInfo, this.minimalRequests);
    }
}
