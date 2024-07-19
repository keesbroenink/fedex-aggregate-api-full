package com.fedex.aggregate_api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

/**
 * This class serves as response object for the JSON REST API having three fields with a Map structure.
 * But it also holds the requested data id's (JsonIgnore).
 */
public class AggregatedInfo {
    public final Map<String, Double> pricing = new TreeMap<>();//not threadsafe
    public final Map<String, String> track = new TreeMap<>();
    public final Map<String, List<String>> shipments = new TreeMap<>();
    @JsonIgnore
    public final List<CountryCode> pricingIso2CountryCodes = new CopyOnWriteArrayList<>();
    @JsonIgnore
    public final List<TrackingOrderNumber> trackOrderNumbers = new CopyOnWriteArrayList();
    @JsonIgnore
    public final List<ShipmentOrderNumber> shipmentsOrderNumbers = new CopyOnWriteArrayList();

    public AggregatedInfo() {
    }

    public AggregatedInfo(List<CountryCode> pricingIso2CountryCodes,
                          List<TrackingOrderNumber> trackOrderNumbers,
                          List<ShipmentOrderNumber> shipmentsOrderNumbers) {
        this.pricingIso2CountryCodes.addAll(pricingIso2CountryCodes);
        this.trackOrderNumbers.addAll(trackOrderNumbers);
        this.shipmentsOrderNumbers.addAll(shipmentsOrderNumbers);
    }
    @JsonIgnore
    public List<String> getPricingIso2CountryCodes() {
        return this.pricingIso2CountryCodes.stream().map(c -> c.code()).toList();
    }
    @JsonIgnore
    public List<String> getTrackingOrderNumbers() {
        return this.trackOrderNumbers.stream().map(c -> c.orderNumber()).toList();
    }
    @JsonIgnore
    public List<String> getShipmentsOrderNumbers() {
        return this.shipmentsOrderNumbers.stream().map(c -> c.orderNumber()).toList();
    }
    public synchronized void addPricing(List<PricingInfo> pricingList) {
        pricingList.forEach(entry -> pricing.put(entry.isoCountryCode().code(), entry.price()));
    }

    public synchronized void addTracking(List<TrackingInfo> trackingList) {
        trackingList.forEach(entry -> track.put(entry.trackingOrderNumber().orderNumber(), entry.status()));
    }

    public synchronized void addShipments(List<ShipmentInfo> shippingList) {
        shippingList.forEach(entry -> shipments.put(entry.shipmentOrderNumber().orderNumber(), entry.shipments()));
    }


    @JsonIgnore
    public boolean isComplete() {
        return pricing.keySet().size() == pricingIso2CountryCodes.size() &&
                track.keySet().size() == trackOrderNumbers.size() &&
                shipments.keySet().size() == shipmentsOrderNumbers.size();
    }

    /**
     * We merge the data in the maps but only if the supplied data corresponds with a request-id
     * that is also in the original.
     * @param data
     */
    public AggregatedInfo merge( AggregatedInfo data) {
        copyMapIfKeyInList(pricing, data.pricing, getPricingIso2CountryCodes());
        copyMapIfKeyInList(track, data.track, getTrackingOrderNumbers());
        copyMapIfKeyInList(shipments, data.shipments, getShipmentsOrderNumbers());
        return this;
    }

    private <K,V> void copyMapIfKeyInList(Map<K,V> orgMap, Map<K,V> map, List<K> list) {
        map.keySet().stream().filter(list::contains).forEach(key -> {
            V val = map.get(key);
            orgMap.put(key, val);
        });
    }

    /**
     * Return a new AggregateInfo that only has request-ids that did not have data yet
     * @return
     */
    public AggregatedInfo buildRequestNotResolved() {
        return new AggregatedInfo(
                buildPricingListIfNoData(this.pricing, getPricingIso2CountryCodes()),
                buildTrackingListIfNoData(this.track, getTrackingOrderNumbers()),
                buildShipmentListIfNoData(this.shipments, getShipmentsOrderNumbers())
        );
    }
    private List<CountryCode> buildPricingListIfNoData(Map<?,?> map, List<String> list) {
        List<CountryCode> result = new ArrayList();
        list.stream().filter(key -> map.get(key) == null).forEach(key->result.add(new CountryCode(key)));
        return result;
    }
    private List<TrackingOrderNumber> buildTrackingListIfNoData(Map<?,?> map, List<String> list) {
        List<TrackingOrderNumber> result = new ArrayList();
        list.stream().filter(key -> map.get(key) == null).forEach(key-> result.add(new TrackingOrderNumber(key)));
        return result;
    }
    private List<ShipmentOrderNumber> buildShipmentListIfNoData(Map<?,?> map, List<String> list) {
        List<ShipmentOrderNumber> result = new ArrayList();
        list.stream().filter(key -> map.get(key) == null).forEach(key-> result.add(new ShipmentOrderNumber(key)));
        return result;
    }
    public List<List<String>> buildChunks(List<String> keys, int minimalRequests) {
        return IntStream.range(0, keys.size())
                .filter(i -> i % minimalRequests == 0)
                .mapToObj(i -> keys.subList(i, Math.min(i + minimalRequests, keys.size())))
                .toList();
    }

    // we are using this object as key in a Map, so we must define what makes this map unique
    // for our purpose the default Java implementation is the right choice; every instance will be unique

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
