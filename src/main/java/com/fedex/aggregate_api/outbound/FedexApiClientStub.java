package com.fedex.aggregate_api.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fedex.aggregate_api.domain.FedexApi;
import com.fedex.aggregate_api.domain.GenericInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

//@Component
public class FedexApiClientStub implements FedexApi {
    private static final Logger logger = LoggerFactory.getLogger(FedexApiClientStub.class);
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public Mono<List<GenericInfo>> getPricing(List<String> iso2CountryCodes) {
        logger.info("getting pricing for {}", iso2CountryCodes);
        return Mono.just(emptyList());
    }

    @Override
    public Mono<List<GenericInfo>> getTrackStatus(List<String> orderNumbers) {
        logger.info("getting trackstatus for {}", orderNumbers);
        return Mono.just(emptyList());
    }

    @Override
    public Mono<List<GenericInfo>> getShipments(List<String> orderNumbers) {
        logger.info("getting shipments for {}", orderNumbers);
        return Mono.just(emptyList());
    }

    private String createEmptyResult(List<String> keys) {
        Map<String,Object> emptyInfo = new HashMap();
        keys.forEach( key -> emptyInfo.put(key, emptyInfo.get(key)));
        try {
            return mapper.writeValueAsString(emptyInfo);
        } catch (JsonProcessingException e) {
            return "";
        }
    }


}
