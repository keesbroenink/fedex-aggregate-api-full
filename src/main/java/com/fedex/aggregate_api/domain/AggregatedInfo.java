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
        copyMapIfKeyInList(pricing, successAggregatedInfo.pricing, pricingIso2CountryCodes);
        copyMapIfKeyInList(track, successAggregatedInfo.track, trackOrderNumbers);
        copyMapIfKeyInList(shipments, successAggregatedInfo.shipments, shipmentsOrderNumbers);
    }

    private <V> void copyMapIfKeyInList(Map<String,V> orgMap, Map<String,V> map, List<String> list) {
        map.keySet().forEach( key -> {
            if (list.contains(key)) {
                V val = map.get(key);
                if (val != null) {
                    orgMap.put(key, val);
                }
            }
        });
    }

    // we are using this object as key in a Map, so we must define what makes this map unique
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregatedInfo that = (AggregatedInfo) o;
        return Objects.equals(pricingIso2CountryCodes, that.pricingIso2CountryCodes) && Objects.equals(trackOrderNumbers, that.trackOrderNumbers) && Objects.equals(shipmentsOrderNumbers, that.shipmentsOrderNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pricingIso2CountryCodes, trackOrderNumbers, shipmentsOrderNumbers);
    }

    @Override
    public String toString() {
        return "AggregatedInfo{" +
                "pricingIso2CountryCodes=" + pricingIso2CountryCodes +
                ", trackOrderNumbers=" + trackOrderNumbers +
                ", shipmentsOrderNumbers=" + shipmentsOrderNumbers +
                ", pricing=" + pricing +
                ", track=" + track +
                ", shipments=" + shipments +
                '}';
    }
}
