package com.fedex.aggregate_api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.emptyList;

public class AggregatedInfo {
    @JsonIgnore
    public final List<String> pricingIso2CountryCodes;
    @JsonIgnore
    public final List<String> trackOrderNumbers;
    @JsonIgnore
    public final List<String> shipmentsOrderNumbers;

    public AggregatedInfo() {
        this.pricingIso2CountryCodes = emptyList();
        this.trackOrderNumbers = emptyList();
        this.shipmentsOrderNumbers = emptyList();
    }

    public AggregatedInfo(List<String> pricingIso2CountryCodes,
                          List<String> trackOrderNumbers,
                          List<String> shipmentsOrderNumbers) {
        this.pricingIso2CountryCodes = pricingIso2CountryCodes;
        this.trackOrderNumbers = trackOrderNumbers;
        this.shipmentsOrderNumbers = shipmentsOrderNumbers;
    }

    // Convenience constructor to create the object with the requestes lists (so NOT with the resulted maps)
    public AggregatedInfo(AggregatedInfo requestedInfo) {
        this.pricingIso2CountryCodes = requestedInfo.pricingIso2CountryCodes;
        this.trackOrderNumbers = requestedInfo.trackOrderNumbers;
        this.shipmentsOrderNumbers = requestedInfo.shipmentsOrderNumbers;
    }

    public Map<String, Double> pricing = new TreeMap();
    public Map<String, String> track = new TreeMap();
    public Map<String, List<String>> shipments = new TreeMap();



    public void addPricing(List<PricingInfo> pricingList) {
        pricingList.forEach(entry -> pricing.put(entry.isoCountryCode, entry.price));
    }

    public void addTracking(List<TrackingInfo> trackingList) {
        trackingList.forEach(entry -> track.put(entry.orderNumber, entry.status));
    }

    public void addShipments(List<ShipmentInfo> shippingList) {
        shippingList.forEach(entry -> shipments.put(entry.orderNumber, entry.shipments));
    }


    @JsonIgnore
    public boolean isComplete() {
        return pricing.keySet().size() == pricingIso2CountryCodes.size() &&
                track.keySet().size() == trackOrderNumbers.size() &&
                shipments.keySet().size() == shipmentsOrderNumbers.size();
    }

    // we merge the maps; the incoming data takes precedence
    public void merge(AggregatedInfo successAggregatedInfo) {
        this.pricing.putAll(successAggregatedInfo.pricing);
        this.track.putAll(successAggregatedInfo.track);
        this.shipments.putAll(successAggregatedInfo.shipments);
    }
}
