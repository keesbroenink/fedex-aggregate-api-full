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
    void getInfoNoInput() {
        FedexApi fedexApi = mock();
        AggregatedInfoService service = new AggregatedInfoService(fedexApi,5);
        AggregatedInfo info = service.getInfo(new AggregatedInfo(emptyList(), emptyList(), emptyList()));
        assertEquals(emptyMap(),info.pricing);
        assertEquals(emptyMap(),info.track);
        assertEquals(emptyMap(),info.shipments);
    }

    @Test
    void testCacheNotEnoughRequests() {
        FedexApi fedexApi = mock();
        AggregatedInfoService service = new AggregatedInfoService(fedexApi,2);
        AggregatedInfo info = service.getInfo(new AggregatedInfo(List.of("NL"), List.of("1"), List.of("2")));
        assertEquals(emptyMap(),info.pricing);
        assertEquals(emptyMap(),info.track);
        assertEquals(emptyMap(),info.shipments);
    }
    @Test
    void testCacheEnoughRequests() {
        List<String> orderNumbers = List.of("1");
        Mono<List<TrackingInfo>> answer = Mono.just(List.of(new TrackingInfo("1","NEW")));
        FedexApi fedexApi = mock();
        given( fedexApi.getTrackingStatus(orderNumbers)).willReturn(answer);
        AggregatedInfoService service = new AggregatedInfoService(fedexApi,1);
        AggregatedInfo info = service.getInfo(new AggregatedInfo(emptyList(), orderNumbers, emptyList()));
        assertEquals(emptyMap(),info.pricing);
        assertEquals(emptyMap(),info.shipments);
        assertEquals(Map.of("1","NEW"),info.track);

    }
}
