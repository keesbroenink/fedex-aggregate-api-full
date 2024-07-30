package com.fedex.aggregate_api.domain;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class TestAggregatedInfoService {

    @Test
    void testGetInfoNoInput() {
        FedexApi fedexApi = mock();
        AggregatedInfoService service = new AggregatedInfoService(fedexApi,5);
        AggregatedInfo info = service.getInfo(new AggregatedInfo(emptyList(), emptyList(), emptyList()));

        assertEquals(emptyMap(),info.getPricing());
        assertEquals(emptyMap(),info.getTrack());
        assertEquals(emptyMap(),info.getShipments());
    }

    @Test
    void testCacheNotEnoughRequests() {
        List<String> orderNumbers = List.of("1");
        List<TrackingOrderNumber> trackingOrderNumbers = TrackingInfo.fromListString(orderNumbers);
        Mono<List<TrackingInfo>> answer = Mono.just(List.of(new TrackingInfo(new TrackingOrderNumber("1"),"NEW")));
        FedexApi fedexApi = mock();
        given( fedexApi.getTrackingStatus(orderNumbers)).willReturn(answer);
        AggregatedInfoService service = new AggregatedInfoService(fedexApi,2);
        AggregatedInfo info = service.getInfo(new AggregatedInfo(emptyList(), trackingOrderNumbers, emptyList()));

        assertEquals(emptyMap(),info.getPricing());
        assertEquals(emptyMap(),info.getTrack());
        assertEquals(emptyMap(),info.getShipments());
    }
    @Test
    void testCacheEnoughRequests() {
        List<String> orderNumbers = List.of("1");
        List<TrackingOrderNumber> trackingOrderNumbers = TrackingInfo.fromListString(orderNumbers);
        Mono<List<TrackingInfo>> answer = Mono.just(List.of(new TrackingInfo(new TrackingOrderNumber("1"),"NEW")));
        FedexApi fedexApi = mock();
        given( fedexApi.getTrackingStatus(orderNumbers)).willReturn(answer);
        AggregatedInfoService service = new AggregatedInfoService(fedexApi,1);
        AggregatedInfo info = service.getInfo(new AggregatedInfo(emptyList(), trackingOrderNumbers, emptyList()));

        assertEquals(emptyMap(),info.getPricing());
        assertEquals(Map.of("1","NEW"),info.getTrack());
        assertEquals(emptyMap(),info.getShipments());
    }

    @Test
    void testNoLimit() {
        List<String> orderNumbers = List.of("1");
        List<TrackingOrderNumber> trackingOrderNumbers = TrackingInfo.fromListString(orderNumbers);
        Mono<List<TrackingInfo>> answer = Mono.just(List.of(new TrackingInfo(new TrackingOrderNumber("1"),"NEW")));
        FedexApi fedexApi = mock();
        given( fedexApi.getTrackingStatus(orderNumbers)).willReturn(answer);
        AggregatedInfoService service = new AggregatedInfoService(fedexApi,2);
        AggregatedInfo info = service.getInfoNoLimit(new AggregatedInfo(emptyList(), trackingOrderNumbers, emptyList()));

        assertEquals(emptyMap(),info.getPricing());
        assertEquals(Map.of("1","NEW"),info.getTrack());
        assertEquals(emptyMap(),info.getShipments());
    }
}
