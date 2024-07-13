package com.fedex.aggregate_api.domain;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AggregatedInfo {
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
}
