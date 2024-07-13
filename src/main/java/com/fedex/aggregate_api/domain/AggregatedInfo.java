package com.fedex.aggregate_api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    // Convenience constructor to create the object with the requested lists (so NOT with the resulted maps)
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
    public void merge( AggregatedInfo successAggregatedInfo) {
        this.pricing.putAll( successAggregatedInfo.pricing);
        this.track.putAll( successAggregatedInfo.track);
        this.shipments.putAll( successAggregatedInfo.shipments);
    }

    // we are using this object as key in a Map so we must define what makes this map unique
    // the default Java implementation of the three maps together will be the right one
    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(pricing.hashCode(), track.hashCode(), shipments.hashCode());
    }
}
