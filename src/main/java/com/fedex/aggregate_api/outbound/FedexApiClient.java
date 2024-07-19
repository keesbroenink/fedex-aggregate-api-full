package com.fedex.aggregate_api.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fedex.aggregate_api.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fedex.aggregate_api.util.StringUtil.listToCommaSeparated;
import static java.util.Collections.emptyList;


// we cannot use OpenFeign because it does not support non-blocking IO yet
@Component
public class FedexApiClient implements FedexApi {
    private static final Logger logger = LoggerFactory.getLogger(FedexApiClient.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;
    private final WebClient webClient;
    public FedexApiClient(WebClient.Builder webClientBuilder,
                          @Value("${fedexapi.client.baseurl}") String baseUrl) {
        this.baseUrl = baseUrl;
        webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter(logResponse())
                .build();
    }

    @Override
    public Mono<List<PricingInfo>> getPricing(List<String> iso2CountryCodes) {
        if (iso2CountryCodes.isEmpty()) return Mono.just(emptyList());
        return getPricingInfo(iso2CountryCodes).flatMap(
                genericInfoList->Mono.just(genericInfoList.stream()
                        .map( e->new PricingInfo(new CountryCode(e.code), (Double) e.data))
                        .collect(Collectors.toList())));
    }

    @Override
    public Mono<List<TrackingInfo>> getTrackingStatus(List<String> orderNumbers) {
        if (orderNumbers.isEmpty()) return Mono.just(emptyList());
        return getTrackingStatusInfo(orderNumbers).flatMap(
                genericInfoList->Mono.just(genericInfoList.stream()
                        .map( e->new TrackingInfo(new TrackingOrderNumber(e.code), (String) e.data))
                        .collect(Collectors.toList())));
    }

    @Override
    public Mono<List<ShipmentInfo>> getShipments(List<String> orderNumbers) {
        if (orderNumbers.isEmpty()) return Mono.just(emptyList());
        return getShipmentInfo(orderNumbers).flatMap(
                genericInfoList->Mono.just(genericInfoList.stream()
                        .map( e->new ShipmentInfo(new ShipmentOrderNumber(e.code), (List<String>) e.data))
                        .collect(Collectors.toList())));
    }

    private Mono<List<GenericInfo>> getPricingInfo(List<String> iso2CountryCodes) {
        return callApi("pricing", iso2CountryCodes);
    }
    private Mono<List<GenericInfo>> getTrackingStatusInfo(List<String> orderNumbers) {
        return callApi("track", orderNumbers);
    }
    private Mono<List<GenericInfo>> getShipmentInfo(List<String> orderNumbers) {
        return callApi("shipments", orderNumbers);
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

    private Mono<List<GenericInfo>> callApi(String path, List<String> queryList) {
        String uri = UriComponentsBuilder.newInstance().path( path)
                .queryParam("q", listToCommaSeparated( queryList))
                .build().toUriString();
        logger.info("Calling {}/{}", baseUrl, uri);

        return webClient.get()
                .uri( uri)
                .retrieve()
                .bodyToMono( String.class)
                .onErrorReturn( createEmptyResult( queryList))
                .map(jsonString -> {
                    try {
                        logger.debug("received json {}",jsonString);
                        List<GenericInfo> result = new ArrayList<>();
                        Map<String, Object> data = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                        data.keySet().forEach( key -> result.add(new GenericInfo(key, data.get(key))));
                        return result;
                    } catch (Exception e) {
                        throw new FedexApiClientException(e);
                    }
                });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor( clientResponse -> {
            logger.info("Response status: {}", clientResponse.statusCode());
            return Mono.just( clientResponse);
        });
    }

}
