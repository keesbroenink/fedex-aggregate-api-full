package com.fedex.aggregate_api.domain;

import com.fedex.aggregate_api.outbound.FedexApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAggregatedInfoService {

    @Test
    void getInfoNoInput() {
        FedexApi fedexApi = new FedexApiClient(WebClient.builder(),"http://localhost");
        AggregatedInfoService service = new AggregatedInfoService(fedexApi,5);
        AggregatedInfo info = service.getInfo(new AggregatedInfo(emptyList(), emptyList(), emptyList()));
        assertEquals(emptyMap(),info.pricing);
        assertEquals(emptyMap(),info.track);
        assertEquals(emptyMap(),info.shipments);
    }


}
