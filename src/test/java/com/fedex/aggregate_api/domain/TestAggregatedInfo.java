package com.fedex.aggregate_api.domain;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class TestAggregatedInfo {
    ObjectMapper mapper = new ObjectMapper();
    @Test
    void testAddPricing() throws JsonProcessingException {
        AggregatedInfo info = new AggregatedInfo();
        Map<String, Double> pricingData = new TreeMap<>();
        pricingData.put("CN", 20.503467806384);
        pricingData.put("NL", 14.242090605778);
        Map<String, List<String>> shipmentsData = new TreeMap<>();
        shipmentsData.put("117347282", List.of("box","pallet"));
        shipmentsData.put("109347263", List.of("box","box"));
        Map<String, String> trackData = new TreeMap<>();
        trackData.put("117347282", "COLLECTING");
        trackData.put("109347263", "NEW");
        info.pricing = pricingData;
        info.shipments = shipmentsData;
        info.track = trackData;
        String expected = "{\"pricing\":{\"CN\":20.503467806384,\"NL\":14.242090605778},\"track\":{\"109347263\":\"NEW\",\"117347282\":\"COLLECTING\"},\"shipments\":{\"109347263\":[\"box\",\"box\"],\"117347282\":[\"box\",\"pallet\"]}}";
        assertEquals(expected, mapper.writeValueAsString(info));
    }
}
