package com.fedex.aggregate_api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

/**
 * This class serves as response object for the JSON REST API having three fields with a Map structure.
 * But it also holds the requested data id's (JsonIgnore).
 */
public class AggregatedInfo {
    public final Map<String, Double> pricing = new TreeMap();
    public final Map<String, String> track = new TreeMap();
    public final Map<String, List<String>> shipments = new TreeMap();
    @JsonIgnore
    public final List<String> pricingIso2CountryCodes = new ArrayList();
    @JsonIgnore
    public final List<String> trackOrderNumbers = new ArrayList();
    @JsonIgnore
    public final List<String> shipmentsOrderNumbers = new ArrayList();

    public AggregatedInfo() {
    }
    public AggregatedInfo(Map<String, Double> pricing,
                          Map<String, String> track,
                          Map<String, List<String>> shipments) {
        this.pricing.putAll(pricing);
        this.track.putAll(track);
        this.shipments.putAll(shipments);
    }
    public AggregatedInfo(List<String> pricingIso2CountryCodes,
                          List<String> trackOrderNumbers,
                          List<String> shipmentsOrderNumbers) {
        this.pricingIso2CountryCodes.addAll(pricingIso2CountryCodes);
        this.trackOrderNumbers.addAll(trackOrderNumbers);
        this.shipmentsOrderNumbers.addAll(shipmentsOrderNumbers);
    }

    // Convenience constructor to create the object with the requested lists (so NOT with the resulted maps)
    public AggregatedInfo(AggregatedInfo requestedInfo) {
        this.pricingIso2CountryCodes.addAll(requestedInfo.pricingIso2CountryCodes);
        this.trackOrderNumbers.addAll(requestedInfo.trackOrderNumbers);
        this.shipmentsOrderNumbers.addAll(requestedInfo.shipmentsOrderNumbers);
    }

    public void addPricing(List<PricingInfo> pricingList) {
        pricingList.forEach(entry -> pricing.put(entry.isoCountryCode(), entry.price()));
    }

    public void addTracking(List<TrackingInfo> trackingList) {
        trackingList.forEach(entry -> track.put(entry.orderNumber(), entry.status()));
    }

    public void addShipments(List<ShipmentInfo> shippingList) {
        shippingList.forEach(entry -> shipments.put(entry.orderNumber(), entry.shipments()));
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
