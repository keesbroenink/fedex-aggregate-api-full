package com.fedex.aggregate_api.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class TestAggregatedInfo {
    ObjectMapper mapper = new ObjectMapper();
    @Test
    void testJsonMapping() throws JsonProcessingException {
        List<String> countryCodes = List.of("NL", "CN");
        List<String> trackOrderNumbers = List.of("117347282","109347263");
        AggregatedInfo info = new AggregatedInfo(PricingInfo.fromListString(countryCodes),
                TrackingInfo.fromListString(trackOrderNumbers),
                ShipmentInfo.fromListString(trackOrderNumbers));
        info.addPricing(List.of(new PricingInfo(new CountryCode("CN"),20.503467806384),
                        new PricingInfo(new CountryCode("NL"),null)));
        info.addTracking(List.of(new TrackingInfo(new TrackingOrderNumber("117347282"),"COLLECTING"),
                        new TrackingInfo(new TrackingOrderNumber("109347263"),"NEW")));
        info.addShipments(List.of(new ShipmentInfo(new ShipmentOrderNumber("117347282"), List.of("box","pallet")),
                        new ShipmentInfo(new ShipmentOrderNumber("109347263"), List.of("box","box"))));
        String expected = "{\"pricing\":{\"CN\":20.503467806384,\"NL\":null},\"track\":{\"109347263\":\"NEW\",\"117347282\":\"COLLECTING\"},\"shipments\":{\"109347263\":[\"box\",\"box\"],\"117347282\":[\"box\",\"pallet\"]}}";
        assertEquals(expected, mapper.writeValueAsString(info));
    }

    @Test
    void testBuildRequestNotResolved() throws JsonProcessingException {
        List<String> countryCodes = List.of("NL", "CN", "GB");
        AggregatedInfo info = new AggregatedInfo(PricingInfo.fromListString(countryCodes),emptyList(),emptyList());
        info.addPricing(List.of(new PricingInfo(new CountryCode("CN"),20.503467806384),
                new PricingInfo(new CountryCode("NL"),null)));
        AggregatedInfo result = info.buildRequestNotResolved();
        List<String> expectedCountryCodes = List.of("GB");
        AggregatedInfo expected = new AggregatedInfo(PricingInfo.fromListString(expectedCountryCodes),emptyList(),emptyList());
        assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(result));
    }
    @Test
    void testBuildChuncks() {
        List<String> countryCodes = List.of("NL", "CN", "GB");
        List<List<String>> result = AggregatedInfo.buildChunks(countryCodes, 1);
        assertEquals(3,result.size());
    }

    @Test
    void testMerge() throws JsonProcessingException {
        List<String> countryCodes = List.of("NL", "CN","GB");
        AggregatedInfo info1 = new AggregatedInfo(PricingInfo.fromListString(countryCodes),emptyList(),emptyList());
        List<PricingInfo> pricing1 = List.of(new PricingInfo(new CountryCode("CN"), 20.503467806384),
                new PricingInfo(new CountryCode("NL"), null));
        info1.addPricing(pricing1);
        List<String> countryCodes2 = List.of("GB");
        AggregatedInfo info2 = new AggregatedInfo(PricingInfo.fromListString(countryCodes2),emptyList(),emptyList());
        List<PricingInfo> pricing2 = List.of(new PricingInfo(new CountryCode("GB"),10.503467806384));
        info2.addPricing(pricing2);
        AggregatedInfo info3 = info1.merge(info2);

        assertEquals(mapper.writeValueAsString(info1), mapper.writeValueAsString(info3));

        List<String> allCountryCodes = List.of("NL", "CN", "GB");
        AggregatedInfo expected = new AggregatedInfo(PricingInfo.fromListString(allCountryCodes),emptyList(),emptyList());
        expected.addPricing(pricing1);
        expected.addPricing(pricing2);

        assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(info3));
        assertEquals(true, info3.isComplete());
    }

    @Test
    void testAddPricing() throws JsonProcessingException {
        AggregatedInfo info = new AggregatedInfo(emptyList(),emptyList(),emptyList());
        List<PricingInfo> pricing = List.of(new PricingInfo(new CountryCode("CN"), 20.503467806384),
                new PricingInfo(new CountryCode("NL"), null));
        info.addPricing(pricing);
        // we can only add if we have the key in there
        AggregatedInfo expected = new AggregatedInfo(emptyList(),emptyList(),emptyList());
        assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(info));

        List<String> countryCodes = List.of("NL", "CN");
        AggregatedInfo info2 = new AggregatedInfo(PricingInfo.fromListString(countryCodes),emptyList(),emptyList());
        info2.addPricing(pricing);
        // we can only add if we have the key in there
        String expected2 = "{\"pricing\":{\"CN\":20.503467806384,\"NL\":null},\"track\":{},\"shipments\":{}}";
        assertEquals(expected2, mapper.writeValueAsString(info2));
    }
}

