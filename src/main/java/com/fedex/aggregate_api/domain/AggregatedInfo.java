package com.fedex.aggregate_api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class serves as response object for the JSON REST API having three fields with a Map structure.
 * But it also holds the requested data id's (JsonIgnore).
 */
public class AggregatedInfo {
    public final Map<String, Double> pricing = new ConcurrentSkipListMap<>();
    public final Map<String, String> track = new ConcurrentSkipListMap<>();
    public final Map<String, List<String>> shipments = new ConcurrentSkipListMap<>();
    @JsonIgnore
    public final List<String> pricingIso2CountryCodes = new CopyOnWriteArrayList<>();
    @JsonIgnore
    public final List<String> trackOrderNumbers = new CopyOnWriteArrayList();
    @JsonIgnore
    public final List<String> shipmentsOrderNumbers = new CopyOnWriteArrayList();

    public AggregatedInfo() {
    }

    public AggregatedInfo(List<String> pricingIso2CountryCodes,
                          List<String> trackOrderNumbers,
                          List<String> shipmentsOrderNumbers) {
        this.pricingIso2CountryCodes.addAll(pricingIso2CountryCodes);
        this.trackOrderNumbers.addAll(trackOrderNumbers);
        this.shipmentsOrderNumbers.addAll(shipmentsOrderNumbers);
    }

    /**
     * onvenience constructor to create the object with the requested lists (so NOT with the resulted maps).
     * Note that we only add the request-id if it is not already present.
      */
    public AggregatedInfo(AggregatedInfo requestedInfo) {
        addAllUnique(this.pricingIso2CountryCodes, requestedInfo.pricingIso2CountryCodes);
        addAllUnique(this.trackOrderNumbers, requestedInfo.trackOrderNumbers);
        addAllUnique(this.shipmentsOrderNumbers, requestedInfo.shipmentsOrderNumbers);
    }

    private void addAllUnique(List<String> list, List<String> newList) {
        newList.stream().filter(e -> !list.contains(e)).forEach(list::add);
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

    /**
     * We merge the data in the maps but only if the supplied data corresponds with a request-id
     * that is also in the original.
     * @param data
     */
    public void merge( AggregatedInfo data) {
        copyMapIfKeyInList(pricing, data.pricing, pricingIso2CountryCodes);
        copyMapIfKeyInList(track, data.track, trackOrderNumbers);
        copyMapIfKeyInList(shipments, data.shipments, shipmentsOrderNumbers);
    }

    private <V> void copyMapIfKeyInList(Map<String,V> orgMap, Map<String,V> map, List<String> list) {
        map.keySet().stream().filter(list::contains).forEach(key -> {
            V val = map.get(key);
            if (val != null) orgMap.put(key, val);
        });
    }

    /**
     * Return a new AggregateInfo that only has request-ids that did not have data yet
     * @return
     */
    public AggregatedInfo buildRequestNotResolved() {
        return new AggregatedInfo(
                buildListIfNoData(this.pricing, new ArrayList(this.pricingIso2CountryCodes)),
                buildListIfNoData(this.track, new ArrayList(this.trackOrderNumbers)),
                buildListIfNoData(this.shipments, new ArrayList(this.shipmentsOrderNumbers))
        );
    }
    private <V> List<String> buildListIfNoData(Map<String,V> map, List<String> list) {
        List<String> result = new ArrayList();
        list.stream().filter(key -> map.get(key) == null).forEach(result::add);
        return result;
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
