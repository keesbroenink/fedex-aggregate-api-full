package com.fedex.aggregate_api.domain;

import com.fedex.aggregate_api.outbound.FedexApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAggregatedInfoService {

    @Test
    void getInfoNoInput() {
        FedexApi fedexApi = new FedexApiClient(WebClient.builder(),"http://localhost");
        AggregatedInfoService service = new AggregatedInfoService(fedexApi);
        Mono<AggregatedInfo> result = service.getInfo(emptyList(), emptyList(), emptyList());
        AggregatedInfo info = result.block();
        assertEquals(emptyMap(),info.pricing);
        assertEquals(emptyMap(),info.track);
        assertEquals(emptyMap(),info.shipments);
    }

    // only pricing

    //
}
