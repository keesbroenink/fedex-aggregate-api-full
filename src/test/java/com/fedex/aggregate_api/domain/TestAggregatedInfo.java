package com.fedex.aggregate_api.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class TestAggregatedInfo {
    ObjectMapper mapper = new ObjectMapper();
    @Test
    void testJsonMapping() throws JsonProcessingException {
        AggregatedInfo info = new AggregatedInfo();
        info.addPricing(List.of(new PricingInfo("CN",20.503467806384),
                        new PricingInfo("NL",14.242090605778)));
        info.addTracking(List.of(new TrackingInfo("117347282","COLLECTING"),
                        new TrackingInfo("109347263","NEW")));
        info.addShipments(List.of(new ShipmentInfo("117347282", List.of("box","pallet")),
                        new ShipmentInfo("109347263", List.of("box","box"))));
        String expected = "{\"pricing\":{\"CN\":20.503467806384,\"NL\":14.242090605778},\"track\":{\"109347263\":\"NEW\",\"117347282\":\"COLLECTING\"},\"shipments\":{\"109347263\":[\"box\",\"box\"],\"117347282\":[\"box\",\"pallet\"]}}";
        assertEquals(expected, mapper.writeValueAsString(info));
    }
}
